# GORM Hibernate 7 inheritance reproduction

This project reproduces two problems with table-per-hierarchy inheritance in `grails-data-hibernate7`.
It uses GORM standalone, Spock and an H2 in-memory database.
It needs no database server and no Grails application.

## Domain classes

- `Vehicle` is the root. It has one property, `String name`. It has no discriminator mapping.
- `Car extends Vehicle` is the intermediate class.
- `SportsCar extends Car` and `Sedan extends Car` are the leaves.
- `Boat` and `Sailboat extends Boat` form a second hierarchy. `Boat` maps only `discriminator column: 'class'`.

Each spec builds its own `HibernateDatastore` from a configuration map and a list of domain classes.

## Problem 1: a query on an intermediate class misses its subclasses

A query on `Car` should return the `Car`, `SportsCar` and `Sedan` rows.
On Hibernate 7 it returns only the `Car` rows.
`count()`, `list()` and HQL all show the problem.
Queries on the root and on the leaves are correct.

The cause is in `RootBinder.addSubclass`.
It adds every descendant to the root class.
Hibernate expects each subclass on its direct superclass.
The intermediate class therefore has no subclasses in the Hibernate mapping.

Spec: `IntermediateClassQuerySpec`.

## Problem 2: the default discriminator changed

With no discriminator mapping, `grails-data-hibernate5` uses a column named `class`.
It stores the fully qualified class name for every class.
`grails-data-hibernate7` uses a column named `DTYPE`.
It stores the fully qualified name for the root and the simple name for each subclass.

This breaks existing schemas:

- `dbCreate: 'validate'` against an existing table with a `class` column fails.
- Mapping only `discriminator column: 'class'`, with no `value`, fails when the datastore starts.

Specs: `DefaultDiscriminatorSpec`, `ValidateExistingTableSpec`, `DiscriminatorColumnOnlySpec`.

## Run

Use Gradle 9 and JDK 25.

```
gradle test
```

This runs the specs on `grails-data-hibernate7`.
To run the same specs on `grails-data-hibernate5`, use:

```
gradle test -Phibernate=5
```

To use a different Grails version, use `-PgrailsVersion=<version>`.

## Expected result

All specs pass. Each spec states the behavior of `grails-data-hibernate5`.

## Actual result

On `grails-data-hibernate7`, 7 of 8 tests fail.
On `grails-data-hibernate5`, all 8 tests pass.

| Test | Hibernate 7 result |
| --- | --- |
| `IntermediateClassQuerySpec`: `Car.count()` is 3 | Fails. `Car.count()` is 1. |
| `IntermediateClassQuerySpec`: HQL `select count(*) from Car` is `[3]` | Fails. The result is `[1]`. |
| `IntermediateClassQuerySpec`: `Car.list()` has 3 rows | Fails. The list has only the `Car` row. |
| `IntermediateClassQuerySpec`: root and leaf counts | Passes. |
| `DefaultDiscriminatorSpec`: the column is named `class` | Fails. The columns are `dtype, id, name, version`. |
| `DefaultDiscriminatorSpec`: the column stores fully qualified names | Fails. The values are `example.Vehicle`, `Car`, `SportsCar`. |
| `ValidateExistingTableSpec`: `dbCreate: 'validate'` succeeds | Fails with `SchemaManagementException: Schema validation: missing column [DTYPE] in table [vehicle]`. |
| `DiscriminatorColumnOnlySpec`: the datastore starts | Fails with `MappingException: Could not parse discriminator value 'null' as discriminator type 'string'`. |

## Versions

- Grails (GORM) 8.0.0-RC1. This is the latest version on Maven Central at the time of writing.
- Hibernate ORM 7.4.10.Final on the Hibernate 7 lane.
- Hibernate ORM 5.6.15.Final (`hibernate-core-jakarta`) on the Hibernate 5 lane.
- Groovy 5.1.3, Spock 2.4-groovy-5.0, H2 2.4.240.
- Gradle 9.7.1, JDK 25.

## License

Apache License 2.0. See `LICENSE`.

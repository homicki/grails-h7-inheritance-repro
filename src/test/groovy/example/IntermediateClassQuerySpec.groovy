package example

import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * A query on Car, the class between the root Vehicle and the leaves SportsCar and Sedan,
 * must return the Car rows and the rows of its subclasses.
 */
class IntermediateClassQuerySpec extends Specification {

    @Shared
    @AutoCleanup
    HibernateDatastore datastore = H2.datastore('queries', 'create-drop', Vehicle, Car, SportsCar, Sedan)

    void setupSpec() {
        Vehicle.withTransaction {
            new Vehicle(name: 'vehicle').save(failOnError: true)
            new Car(name: 'car').save(failOnError: true)
            new SportsCar(name: 'sports car').save(failOnError: true)
            new Sedan(name: 'sedan').save(failOnError: true, flush: true)
        }
    }

    void "count() on the intermediate class includes its subclasses"() {
        expect:
        Car.withTransaction { Car.count() } == 3
    }

    void "HQL count on the intermediate class includes its subclasses"() {
        expect:
        Car.withTransaction { Car.executeQuery('select count(*) from Car') } == [3L]
    }

    void "list() on the intermediate class includes its subclasses"() {
        expect:
        Car.withTransaction { Car.list()*.name.sort() } == ['car', 'sedan', 'sports car']
    }

    void "the root and the leaves return the expected rows"() {
        expect:
        Vehicle.withTransaction { [Vehicle.count(), SportsCar.count(), Sedan.count()] } == [4, 1, 1]
    }
}

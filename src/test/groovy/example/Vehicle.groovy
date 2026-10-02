package example

import grails.gorm.annotation.Entity

/** Root of a table-per-hierarchy tree with no discriminator mapping. */
@Entity
class Vehicle {
    String name
}

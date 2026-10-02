package example

import grails.gorm.annotation.Entity

/** Intermediate class: a subclass of Vehicle with subclasses of its own. */
@Entity
class Car extends Vehicle {
}

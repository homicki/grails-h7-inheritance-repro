package example

import grails.gorm.annotation.Entity

/** Root that maps only the discriminator column name, with no value. */
@Entity
class Boat {
    String name

    static mapping = {
        discriminator column: 'class'
    }
}

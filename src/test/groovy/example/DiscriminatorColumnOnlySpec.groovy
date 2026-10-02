package example

import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.Specification

/**
 * Mapping only the discriminator column name, with no value, must start the datastore.
 * Boat maps "discriminator column: 'class'".
 */
class DiscriminatorColumnOnlySpec extends Specification {

    HibernateDatastore datastore

    void cleanup() {
        datastore?.close()
    }

    void "a hierarchy that maps only the discriminator column starts and saves rows"() {
        when:
        datastore = H2.datastore('boats', 'create-drop', Boat, Sailboat)

        then:
        noExceptionThrown()

        when:
        Boat.withTransaction {
            new Boat(name: 'boat').save(failOnError: true)
            new Sailboat(name: 'sailboat').save(failOnError: true, flush: true)
        }

        then:
        Boat.withTransaction { [Boat.count(), Sailboat.count()] } == [2, 1]
    }
}

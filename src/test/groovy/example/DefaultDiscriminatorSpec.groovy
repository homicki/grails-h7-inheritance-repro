package example

import groovy.sql.Sql
import org.grails.orm.hibernate.HibernateDatastore
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

/**
 * With no discriminator mapping, GORM names the discriminator column "class"
 * and stores the fully qualified name of each class.
 */
class DefaultDiscriminatorSpec extends Specification {

    @Shared
    @AutoCleanup
    HibernateDatastore datastore = H2.datastore('defaults', 'create-drop', Vehicle, Car, SportsCar, Sedan)

    @Shared
    @AutoCleanup
    Sql sql = H2.sql('defaults')

    void setupSpec() {
        Vehicle.withTransaction {
            new Vehicle(name: 'vehicle').save(failOnError: true)
            new Car(name: 'car').save(failOnError: true)
            new SportsCar(name: 'sports car').save(failOnError: true, flush: true)
        }
    }

    List<String> columnNames() {
        List<String> columns = []
        def resultSet = sql.connection.metaData.getColumns(null, null, 'VEHICLE', null)
        try {
            while (resultSet.next()) {
                columns << resultSet.getString('COLUMN_NAME')
            }
        } finally {
            resultSet.close()
        }
        columns
    }

    void "the discriminator column is named class"() {
        expect:
        columnNames()*.toLowerCase().sort() == ['class', 'id', 'name', 'version']
    }

    void "the discriminator column stores fully qualified class names"() {
        when: 'the discriminator column is the one column that is not id, version or name'
        String discriminator = (columnNames() - ['ID', 'VERSION', 'NAME']).first()

        then:
        sql.rows("select \"${discriminator}\" as d from vehicle order by id".toString())*.d == [
                'example.Vehicle', 'example.Car', 'example.SportsCar']
    }
}

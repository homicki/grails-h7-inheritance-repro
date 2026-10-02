package example

import groovy.sql.Sql
import org.grails.orm.hibernate.HibernateDatastore

/** Builds a GORM datastore on a named H2 in-memory database. */
class H2 {

    static String url(String database) {
        "jdbc:h2:mem:${database};DB_CLOSE_DELAY=-1"
    }

    static HibernateDatastore datastore(String database, String dbCreate, Class... domainClasses) {
        Map<String, Object> config = [
                'dataSource.url'            : url(database),
                'dataSource.driverClassName': 'org.h2.Driver',
                'dataSource.username'       : 'sa',
                'dataSource.password'       : '',
                'dataSource.dbCreate'       : dbCreate,
        ]
        new HibernateDatastore(config, domainClasses)
    }

    static Sql sql(String database) {
        Sql.newInstance(url(database), 'sa', '', 'org.h2.Driver')
    }
}

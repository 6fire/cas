package org.apereo.cas.configuration.support

import java.sql.Connection
import java.sql.SQLException

import javax.sql.DataSource

import org.springframework.jdbc.datasource.AbstractDataSource

/**
 * This purpose of this class is to fix a class loading issue that occurs
 * in some application servers when using a datasource/pool from the
 * container @[ClassLoader]. By having this class at the app level,
 * proxying that occurs (e.g. [org.springframework.cloud.context.config.annotation.RefreshScope] annotation)
 * doesn't encounter
 * problems if another proxying library somewhere else uses the container
 * class loader and encounters other proxying classes in the application.
 *
 * Extends [AbstractDataSource] but it could probably
 * just delegate all methods to the wrapped datasource if anything
 * that AbstractDataSource is doing causes a problem.
 *
 * @author Hal Deadman
 * @since 5.1
 */
class DataSourceProxy(private val dataSource: DataSource) : AbstractDataSource() {

    @Throws(SQLException::class)
    override fun getConnection(): Connection {
        return dataSource.connection
    }

    @Throws(SQLException::class)
    override fun getConnection(username: String, password: String): Connection {
        return dataSource.getConnection(username, password)
    }

}

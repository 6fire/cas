package org.apereo.cas.configuration.model.support.jpa

import org.apache.commons.lang3.StringUtils
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties
import org.apereo.cas.configuration.support.Beans
import org.apereo.cas.configuration.support.RequiredProperty

import java.io.Serializable
import java.util.HashMap

/**
 * Common properties for all jpa configs.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
abstract class AbstractJpaProperties : Serializable {

    /**
     * The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc)
     * which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
     */
    var dialect = "org.hibernate.dialect.HSQLDialect"

    /**
     * Hibernate feature to automatically validate and exports DDL to the schema.
     * By default, creates and drops the schema automatically when a session is starts and ends
     */
    var ddlAuto = "create-drop"

    /**
     * The JDBC driver used to connect to the database.
     */
    @RequiredProperty
    open var driverClass = "org.hsqldb.jdbcDriver"

    /**
     * The database connection URL.
     */
    @RequiredProperty
    open var url = "jdbc:hsqldb:mem:cas-hsql-database"
    
    /**
     * The database user.
     *
     * The database user must have sufficient permissions to be able to handle
     * schema changes and updates, when needed.
     */
    @RequiredProperty
    open var user = "sa"

    /**
     * The database connection password.
     */
    @RequiredProperty
    open var password = StringUtils.EMPTY

    /**
     * Qualifies unqualified table names with the given catalog in generated SQL.
     */
    var defaultCatalog: String? = null

    /**
     * Qualify unqualified table names with the given schema/tablespace in generated SQL.
     */
    var defaultSchema: String? = null

    /**
     * The SQL query to be executed to test the validity of connections.
     */
    var healthQuery = StringUtils.EMPTY

    /**
     * Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
     */
    private var idleTimeout = "PT10M"

    /**
     * Attempts to do a JNDI data source look up for the data source name specified.
     * Will attempt to locate the data source object as is, or will try to return a proxy
     * instance of it, in the event that [.dataSourceProxy] is used.
     */
    var dataSourceName: String? = null

    /**
     * Additional settings provided by Hibernate in form of key-value pairs.
     *
     * @see org.hibernate.cfg.AvailableSettings
     */
    var properties: Map<String, String> = HashMap()

    /**
     * Database connection pooling settings.
     */
    var pool = ConnectionPoolingProperties()

    /**
     * Controls the amount of time that a connection can be out of the pool before a message
     * is logged indicating a possible connection leak.
     */
    var leakThreshold = 3000

    /**
     * A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
     */
    var batchSize = 5

    /**
     * Set the pool initialization failure timeout.
     *
     *  * Any value greater than zero will be treated as a timeout for pool initialization.
     * The calling thread will be blocked from continuing until a successful connection
     * to the database, or until the timeout is reached.  If the timeout is reached, then
     * a `PoolInitializationException` will be thrown.
     *  * A value of zero will *not*  prevent the pool from starting in the
     * case that a connection cannot be obtained. However, upon start the pool will
     * attempt to obtain a connection and validate that the `connectionTestQuery`
     * and `connectionInitSql` are valid.  If those validations fail, an exception
     * will be thrown.  If a connection cannot be obtained, the validation is skipped
     * and the the pool will start and continue to try to obtain connections in the
     * background.  This can mean that callers to `DataSource#getConnection()` may
     * encounter exceptions.
     *  * A value less than zero will *not* bypass any connection attempt and
     * validation during startup, and therefore the pool will start immediately.  The
     * pool will continue to try to obtain connections in the background. This can mean
     * that callers to `DataSource#getConnection()` may encounter exceptions.
     *
     * Note that if this timeout value is greater than or equal to zero (0), and therefore an
     * initial connection validation is performed, this timeout does not override the
     * `connectionTimeout` or `validationTimeout`; they will be honored before this
     * timeout is applied.  The default value is one millisecond.
     */
    var failFastTimeout: Long = 1

    /**
     * This property determines whether data source isolates internal pool queries, such as the connection alive test,
     * in their own transaction.
     *
     *
     * Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction.
     * This property only applies if [.autocommit] is disabled.
     */
    var isIsolateInternalQueries: Boolean = false

    /**
     * The default auto-commit behavior of connections in the pool.
     * Determined whether queries such as update/insert should be immediately executed
     * without waiting for an underlying transaction.
     */
    var isAutocommit: Boolean = false

    /**
     * Indicates whether JNDI data sources retrieved should be proxied
     * or returned back verbatim.
     */
    var isDataSourceProxy: Boolean = false

    fun getIdleTimeout(): Long {
        return Beans.newDuration(idleTimeout).toMillis()
    }

    fun setIdleTimeout(idleTimeout: String) {
        this.idleTimeout = idleTimeout
    }

    companion object {

        private const val serialVersionUID = 761486823496930920L
    }
}

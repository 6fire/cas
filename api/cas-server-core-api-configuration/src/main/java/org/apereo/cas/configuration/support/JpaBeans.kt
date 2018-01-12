package org.apereo.cas.configuration.support

import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.lang3.StringUtils
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.apereo.cas.configuration.model.support.jpa.DatabaseProperties
import org.apereo.cas.configuration.model.support.jpa.JpaConfigDataHolder
import org.hibernate.cfg.Environment
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.util.*
import javax.sql.DataSource

/**
 * This is [JpaBeans].
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class JpaBeans {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(JpaBeans::class.java)

        /**
         * Get new data source, from JNDI lookup or created via direct configuration
         * of Hikari pool.
         *
         *
         * If jpaProperties contains [AbstractJpaProperties.getDataSourceName] a lookup will be
         * attempted. If the DataSource is not found via JNDI then CAS will attempt to
         * configure a Hikari connection pool.
         *
         *
         * Since the datasource beans are [org.springframework.cloud.context.config.annotation.RefreshScope],
         * they will be a proxied by Spring
         * and on some application servers there have been classloading issues. A workaround
         * for this is to use the [AbstractJpaProperties.isDataSourceProxy] setting and then the dataSource will be
         * wrapped in an application level class. If that is an issue, don't do it.
         *
         *
         * If user wants to do lookup as resource, they may include `java:/comp/env`
         * in `dataSourceName` and put resource reference in web.xml
         * otherwise `dataSourceName` is used as JNDI name.
         *
         * @param jpaProperties the jpa properties
         * @return the data source
         */
        fun newDataSource(jpaProperties: AbstractJpaProperties): DataSource {
            val dataSourceName = jpaProperties.dataSourceName
            val proxyDataSource = jpaProperties.isDataSourceProxy

            if (StringUtils.isNotBlank(dataSourceName)) {
                try {
                    val dsLookup = JndiDataSourceLookup()
                    dsLookup.isResourceRef = false
                    val containerDataSource = dsLookup.getDataSource(dataSourceName)
                    return if (!proxyDataSource) {
                        containerDataSource
                    } else DataSourceProxy(containerDataSource)
                } catch (e: DataSourceLookupFailureException) {
                    LOGGER.warn("Lookup of datasource [{}] failed due to {} " + "falling back to configuration via JPA properties.", dataSourceName, e.message)
                }

            }

            try {
                val bean = HikariDataSource()
                if (StringUtils.isNotBlank(jpaProperties.driverClass)) {
                    bean.driverClassName = jpaProperties.driverClass
                }
                bean.jdbcUrl = jpaProperties.url
                bean.username = jpaProperties.user
                bean.password = jpaProperties.password
                bean.loginTimeout = jpaProperties.pool.getMaxWait().toInt()
                bean.maximumPoolSize = jpaProperties.pool.maxSize
                bean.minimumIdle = jpaProperties.pool.minSize
                bean.idleTimeout = jpaProperties.getIdleTimeout()
                bean.leakDetectionThreshold = jpaProperties.leakThreshold.toLong()
                bean.initializationFailTimeout = jpaProperties.failFastTimeout
                bean.isIsolateInternalQueries = jpaProperties.isIsolateInternalQueries
                bean.connectionTestQuery = jpaProperties.healthQuery
                bean.isAllowPoolSuspension = jpaProperties.pool.isSuspension
                bean.isAutoCommit = jpaProperties.isAutocommit
                bean.validationTimeout = jpaProperties.pool.timeoutMillis
                return bean
            } catch (e: Exception) {
                LOGGER.error("Error creating data source: [{}]", e.message)
                throw IllegalArgumentException(e)
            }

        }

        /**
         * New hibernate jpa vendor adapter.
         *
         * @param databaseProperties the database properties
         * @return the hibernate jpa vendor adapter
         */
        fun newHibernateJpaVendorAdapter(databaseProperties: DatabaseProperties): HibernateJpaVendorAdapter {
            val bean = HibernateJpaVendorAdapter()
            bean.setGenerateDdl(databaseProperties.isGenDdl)
            bean.setShowSql(databaseProperties.isShowSql)
            return bean
        }


        /**
         * New entity manager factory bean.
         *
         * @param config        the config
         * @param jpaProperties the jpa properties
         * @return the local container entity manager factory bean
         */
        fun newHibernateEntityManagerFactoryBean(config: JpaConfigDataHolder,
                                                 jpaProperties: AbstractJpaProperties): LocalContainerEntityManagerFactoryBean {
            val bean = LocalContainerEntityManagerFactoryBean()
            bean.jpaVendorAdapter = config.jpaVendorAdapter

            if (StringUtils.isNotBlank(config.persistenceUnitName)) {
                bean.persistenceUnitName = config.persistenceUnitName
            }
            bean.setPackagesToScan(*config.packagesToScan.toTypedArray())

            if (config.dataSource != null) {
                bean.dataSource = config.dataSource
            }

            val properties = Properties()
            properties.put(Environment.DIALECT, jpaProperties.dialect)
            properties.put(Environment.HBM2DDL_AUTO, jpaProperties.ddlAuto)
            properties.put(Environment.STATEMENT_BATCH_SIZE, jpaProperties.batchSize)
            if (StringUtils.isNotBlank(jpaProperties.defaultCatalog)) {
                properties.put(Environment.DEFAULT_CATALOG, jpaProperties.defaultCatalog!!)
            }
            if (StringUtils.isNotBlank(jpaProperties.defaultSchema)) {
                properties.put(Environment.DEFAULT_SCHEMA, jpaProperties.defaultSchema!!)
            }
            properties.put(Environment.ENABLE_LAZY_LOAD_NO_TRANS, java.lang.Boolean.TRUE)
            properties.put(Environment.FORMAT_SQL, java.lang.Boolean.TRUE)
            properties.putAll(jpaProperties.properties)
            bean.setJpaProperties(properties)

            return bean
        }
    }
}

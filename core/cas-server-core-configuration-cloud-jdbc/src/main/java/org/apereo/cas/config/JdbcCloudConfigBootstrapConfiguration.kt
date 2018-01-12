package org.apereo.cas.config

import org.apache.commons.lang3.StringUtils
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.apereo.cas.configuration.support.JpaBeans
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.bootstrap.config.PropertySourceLocator
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.jdbc.core.JdbcTemplate

import javax.sql.DataSource
import java.util.Properties

/**
 * This is [JdbcCloudConfigBootstrapConfiguration].
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("jdbcCloudConfigBootstrapConfiguration")
class JdbcCloudConfigBootstrapConfiguration : PropertySourceLocator {

    override fun locate(environment: Environment): PropertySource<*> {
        val props = Properties()

        try {
            val connection = JdbcCloudConnection(environment)
            val dataSource = JpaBeans.newDataSource(connection)
            val jdbcTemplate = JdbcTemplate(dataSource)
            val rows = jdbcTemplate.queryForList(connection.sql)
            for (row in rows) {
                props.put(row["name"], row["value"])
            }
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }

        return PropertiesPropertySource(javaClass.simpleName, props)
    }

    private class JdbcCloudConnection internal constructor(private val environment: Environment) : AbstractJpaProperties() {
        
        val sql: String
            get() = StringUtils.defaultIfBlank(getSetting(environment, "sql"), SQL)

        override var url: String
            get() = StringUtils.defaultIfBlank(getSetting(environment, "url"), super.url)
            set(value: String) {
                super.url = value
            }

        override var password: String
            get() = StringUtils.defaultIfBlank(getSetting(environment, "password"), super.password)
            set(value: String) {
                super.password = value
            }

        override var user: String
            get() = StringUtils.defaultIfBlank(getSetting(environment, "user"), super.user)
            set(value: String) {
                super.user = value
            }

        override var driverClass: String
            get() = StringUtils.defaultIfBlank(getSetting(environment, "driverClass"), super.driverClass)
            set(value: String) {
                super.driverClass = value
            }

        companion object {
            private val SQL = "SELECT id, name, value FROM CAS_SETTINGS_TABLE"
            private val serialVersionUID = 3141915452108685020L

            private fun getSetting(environment: Environment, key: String): String {
                return environment.getProperty("cas.spring.cloud.jdbc." + key)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JdbcCloudConfigBootstrapConfiguration::class.java)
    }
}

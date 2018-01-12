package org.apereo.cas.configuration.support

import org.apache.commons.lang3.math.NumberUtils
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesProperties
import org.apereo.cas.configuration.model.support.ConnectionPoolingProperties
import org.apereo.services.persondir.IPersonAttributeDao
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao
import org.slf4j.LoggerFactory
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean
import java.time.Duration
import java.util.*
import java.util.stream.Collectors


/**
 * A re-usable collection of utility methods for object instantiations and configurations used cross various
 * `@Bean` creation methods throughout CAS server.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
class Beans {
    companion object {

        /**
         * New thread pool executor factory bean.
         *
         * @param config the config
         * @return the thread pool executor factory bean
         */
        fun newThreadPoolExecutorFactoryBean(config: ConnectionPoolingProperties): ThreadPoolExecutorFactoryBean {
            val bean = newThreadPoolExecutorFactoryBean(config.maxSize.toLong(), config.maxSize.toLong())
            bean.setCorePoolSize(config.minSize)
            return bean
        }

        /**
         * New thread pool executor factory bean.
         *
         * @param keepAlive the keep alive
         * @param maxSize   the max size
         * @return the thread pool executor factory bean
         */
        fun newThreadPoolExecutorFactoryBean(keepAlive: Long,
                                             maxSize: Long): ThreadPoolExecutorFactoryBean {
            val bean = ThreadPoolExecutorFactoryBean()
            bean.setMaxPoolSize(maxSize.toInt())
            bean.setKeepAliveSeconds(keepAlive.toInt())
            return bean
        }

        /**
         * New attribute repository person attribute dao.
         *
         * @param p the properties
         * @return the person attribute dao
         */
        fun newStubAttributeRepository(p: PrincipalAttributesProperties): IPersonAttributeDao {
            try {
                val dao = NamedStubPersonAttributeDao()
                val pdirMap = HashMap<String, List<Any>>()
                p.stub.attributes.forEach { key, value ->
                    val vals = org.springframework.util.StringUtils.commaDelimitedListToStringArray(value)
                    pdirMap.put(key, Arrays.stream(vals).collect(Collectors.toList()))
                }
                dao.backingMap = pdirMap
                return dao
            } catch (e: Exception) {
                throw RuntimeException(e.message, e)
            }

        }


        /**
         * New duration. If the provided length is duration,
         * it will be parsed accordingly, or if it's a numeric value
         * it will be pared as a duration assuming it's provided as seconds.
         *
         * @param length the length in seconds.
         * @return the duration
         */
        fun newDuration(length: String): Duration {
            try {
                return if (NumberUtils.isCreatable(length)) {
                    Duration.ofSeconds(java.lang.Long.parseLong(length))
                } else Duration.parse(length)
            } catch (e: Exception) {
                throw RuntimeException(e.message, e)
            }

        }
    }
}

package org.apereo.cas.configuration.model.support.jpa.ticketregistry

import org.apereo.cas.configuration.model.core.util.EncryptionRandomizedSigningJwtCryptographyProperties
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties
import org.apereo.cas.configuration.support.Beans
import org.apereo.cas.configuration.support.RequiresModule
import org.springframework.boot.context.properties.NestedConfigurationProperty

import javax.persistence.LockModeType

/**
 * Common properties for jpa ticket reg.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-jpa-ticket-registry")
class JpaTicketRegistryProperties : AbstractJpaProperties() {

    /**
     * Ticket locking type. Acceptable values are
     * `READ,WRITE,OPTIMISTIC,OPTIMISTIC_FORCE_INCREMENT,PESSIMISTIC_READ,
     * PESSIMISTIC_WRITE,PESSIMISTIC_FORCE_INCREMENT,NONE`.
     */
    var ticketLockType = LockModeType.NONE

    /**
     * Indicates the lock duration when one is about to be acquired by the cleaner.
     */
    private var jpaLockingTimeout = DEFAULT_LOCK_TIMEOUT

    /**
     * Crypto settings for the registry.
     */
    @NestedConfigurationProperty
    var crypto = EncryptionRandomizedSigningJwtCryptographyProperties()

    init {
        super.url = "jdbc:hsqldb:mem:cas-ticket-registry"
        this.crypto.isEnabled = false
    }

    fun getJpaLockingTimeout(): Long {
        return Beans.newDuration(jpaLockingTimeout).seconds
    }

    fun setJpaLockingTimeout(jpaLockingTimeout: String) {
        this.jpaLockingTimeout = jpaLockingTimeout
    }

    companion object {

        /**
         * Default lock timeout is 1 hour.
         */
        const val DEFAULT_LOCK_TIMEOUT = "PT1H"

        private val serialVersionUID = -8053839523783801072L
    }
}

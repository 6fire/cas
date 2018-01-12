package org.apereo.cas.configuration.support

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * This is [RequiresModule] that is put on top of a CAS properties class
 * to indicate the required/using module that takes advantage of the settings.
 * The module typically needs to be available on the classpath at runtime
 * in order to activate a certain feature in CAS.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class RequiresModule(
        /**
         * Indicate the name of the module required.
         * Module names typically don't carry prefixes such as `cas-server-`.
         * The name indicates only the actual functionality.
         *
         * @return the name
         */
        val name: String,
        /**
         * Indicates the module is automatically included and is present
         * on the classpath. In such cases, the feature at hand may only be tweaked
         * using a toggle in settings.
         *
         * @return the boolean
         */
        val automated: Boolean = false)

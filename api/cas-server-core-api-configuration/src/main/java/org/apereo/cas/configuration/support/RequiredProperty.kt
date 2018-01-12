package org.apereo.cas.configuration.support

import java.lang.annotation.Documented
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * This is [RequiredProperty] that is put on top of a CAS property/field
 * to indicate the presence of the field is required for CAS to function correctly
 * and/or to recognize the existence of an enabled feature, etc.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RequiredProperty(
        /**
         * The message associated with this required property.
         * Might want to explain caveats and falls back to defaults.
         *
         * @return the msg
         */
        val message: String = "The property is required")

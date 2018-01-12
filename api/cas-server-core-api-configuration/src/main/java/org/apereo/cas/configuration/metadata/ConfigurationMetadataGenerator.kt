package org.apereo.cas.configuration.metadata

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Modifier
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.BooleanLiteralExpr
import com.github.javaparser.ast.expr.LiteralStringValueExpr
import com.github.javaparser.ast.type.ClassOrInterfaceType
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.apache.commons.lang3.ClassUtils
import org.apache.commons.lang3.StringUtils
import org.apereo.cas.configuration.model.core.authentication.PasswordPolicyProperties
import org.apereo.cas.configuration.model.core.authentication.PrincipalTransformationProperties
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties
import org.apereo.cas.configuration.model.support.ldap.LdapSearchEntryHandlersProperties
import org.apereo.cas.configuration.support.RequiredProperty
import org.apereo.cas.configuration.support.RequiresModule
import org.apereo.services.persondir.support.QueryType
import org.apereo.services.persondir.util.CaseCanonicalizationMode
import org.jooq.lambda.Unchecked
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeElementsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import org.springframework.boot.bind.RelaxedNames
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty
import org.springframework.boot.configurationmetadata.ValueHint
import org.springframework.core.io.Resource
import org.springframework.util.ReflectionUtils
import java.io.File
import java.io.FileInputStream
import java.io.Serializable
import java.lang.reflect.Field
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * This is [ConfigurationMetadataGenerator].
 * This class is invoked by the build during the finalization of the compile phase.
 * Its job is to scan the generated configuration metadata and produce metadata
 * for settings that the build process is unable to parse. Specifically,
 * this includes fields that are of collection type (indexed) where the inner type is an
 * externalized class.
 *
 *
 * Example:
 * `private List<SomeClassProperties> list = new ArrayList<>()
` *
 * The generator additionally adds hints to the metadata generated to indicate
 * required properties and modules.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
class ConfigurationMetadataGenerator(private val buildDir: String, private val sourcePath: String) {
    private val cachedPropertiesClasses = HashMap<String, Class<*>>()

    /**
     * Execute.
     *
     * @throws Exception the exception
     */
    @Throws(Exception::class)
    fun execute() {
        val jsonFile = File(buildDir, "classes/java/main/META-INF/spring-configuration-metadata.json")
        val mapper = ObjectMapper().findAndRegisterModules()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        val values = object : TypeReference<Map<String, Set<ConfigurationMetadataProperty>>>() {
        }
        val jsonMap = (mapper.readValue(jsonFile, values) as Map<String, Set<*>>).toMutableMap()
        var properties = (jsonMap["properties"] as Set<ConfigurationMetadataProperty>).toMutableSet()
        val groups = (jsonMap["groups"] as Set<ConfigurationMetadataProperty>).toMutableSet()

        val collectedProps = HashSet<ConfigurationMetadataProperty>()
        val collectedGroups = HashSet<ConfigurationMetadataProperty>()

        properties
                .stream()
                .filter { p -> NESTED_TYPE_PATTERN.matcher(p.getType()).matches() }
                .forEach(Unchecked.consumer<ConfigurationMetadataProperty> { p ->
                    val matcher = NESTED_TYPE_PATTERN.matcher(p.type)
                    val indexBrackets = matcher.matches()
                    val typeName = matcher.group(1)
                    val typePath = buildTypeSourcePath(typeName)

                    parseCompilationUnit(collectedProps, collectedGroups, p, typePath, typeName, indexBrackets)

                })

        properties.addAll(collectedProps)
        groups.addAll(collectedGroups)

        val hints = processHints(properties, groups)

        jsonMap.put("properties", properties)
        jsonMap.put("groups", groups)
        jsonMap.put("hints", hints)

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val pp = DefaultPrettyPrinter()
        mapper.writer(pp).writeValue(jsonFile, jsonMap)
    }

    private fun buildTypeSourcePath(type: String): String {
        val newName = type.replace(".", File.separator)
        return "$sourcePath/src/main/java/$newName.java"
    }

    private fun parseCompilationUnit(collectedProps: MutableSet<ConfigurationMetadataProperty>,
                                     collectedGroups: MutableSet<ConfigurationMetadataProperty>,
                                     p: ConfigurationMetadataProperty,
                                     typePath: String,
                                     typeName: String,
                                     indexNameWithBrackets: Boolean) {

        try {
            FileInputStream(typePath).use { `is` ->
                val cu = JavaParser.parse(`is`)
                FieldVisitor(collectedProps, collectedGroups, indexNameWithBrackets, typeName).visit(cu, p)
                if (cu.types.size > 0) {
                    val decl = ClassOrInterfaceDeclaration::class.java.cast(cu.getType(0))
                    for (i in 0 until decl.extendedTypes.size) {
                        val parentType = decl.extendedTypes[i]
                        val parentClazz = locatePropertiesClassForType(parentType)
                        val parentTypePath = buildTypeSourcePath(parentClazz!!.name)

                        parseCompilationUnit(collectedProps, collectedGroups, p,
                                parentTypePath, parentClazz.name, indexNameWithBrackets)
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(e.message, e)
        }

    }

    private inner class FieldVisitor internal constructor(private val properties: MutableSet<ConfigurationMetadataProperty>, private val groups: MutableSet<ConfigurationMetadataProperty>,
                                                          private val indexNameWithBrackets: Boolean, private val parentClass: String) : VoidVisitorAdapter<ConfigurationMetadataProperty>() {

        override fun visit(field: FieldDeclaration, property: ConfigurationMetadataProperty) {
            if (field.variables.isEmpty()) {
                throw IllegalArgumentException("Field $field has no variable definitions")
            }
            val `var` = field.getVariable(0)
            if (field.modifiers.contains(Modifier.STATIC)) {
                LOGGER.debug("Field [{}] is static and will be ignored for metadata generation", `var`.nameAsString)
                return
            }

            if (field.javadoc.isPresent) {
                val prop = createConfigurationProperty(field, property)
                processNestedClassOrInterfaceTypeIfNeeded(field, prop)
            } else {
                LOGGER.error("Field $field has no Javadoc defined")
            }
        }

        private fun createConfigurationProperty(n: FieldDeclaration, arg: ConfigurationMetadataProperty): ConfigurationMetadataProperty {
            val variable = n.variables[0]
            val name = StreamSupport.stream(RelaxedNames.forCamelCase(variable.nameAsString).spliterator(), false)
                    .map({ it.toString() })
                    .findFirst()
                    .orElse(variable.nameAsString)

            val indexedGroup = arg.name + if (indexNameWithBrackets) "[]" else StringUtils.EMPTY
            val indexedName = indexedGroup + "." + name

            val prop = ConfigurationMetadataProperty()
            val description = n.javadoc.get().description.toText()
            prop.description = description
            prop.shortDescription = StringUtils.substringBefore(description, ".")
            prop.name = indexedName
            prop.id = indexedName

            val elementType = n.elementType.asString()
            if (elementType == String::class.java.simpleName
                    || elementType == Int::class.java.simpleName
                    || elementType == Long::class.java.simpleName
                    || elementType == Double::class.java.simpleName
                    || elementType == Float::class.java.simpleName) {
                prop.type = "java.lang." + elementType
            } else {
                prop.type = elementType
            }

            if (variable.initializer.isPresent) {
                val exp = variable.initializer.get()
                if (exp is LiteralStringValueExpr) {
                    prop.defaultValue = exp.value
                } else if (exp is BooleanLiteralExpr) {
                    prop.defaultValue = exp.value
                }
            }
            properties.add(prop)

            val grp = ConfigurationMetadataProperty()
            grp.id = indexedGroup
            grp.name = indexedGroup
            grp.type = this.parentClass
            groups.add(grp)

            return prop
        }

        private fun processNestedClassOrInterfaceTypeIfNeeded(n: FieldDeclaration, prop: ConfigurationMetadataProperty) {
            if (n.elementType is ClassOrInterfaceType) {
                val type = n.elementType as ClassOrInterfaceType
                if (!shouldTypeBeExcluded(type)) {
                    val clz = locatePropertiesClassForType(type)
                    if (clz != null && !clz.isMemberClass) {
                        val typePath = buildTypeSourcePath(clz.name)
                        parseCompilationUnit(properties, groups, prop, typePath, clz.name, false)
                    }
                }
            }
        }

        private fun shouldTypeBeExcluded(type: ClassOrInterfaceType): Boolean {
            return type.nameAsString.matches((String::class.java.simpleName + "|"
                    + Int::class.java.simpleName + "|"
                    + Double::class.java.simpleName + "|"
                    + Long::class.java.simpleName + "|"
                    + Float::class.java.simpleName + "|"
                    + Boolean::class.java.simpleName + "|"
                    + PrincipalTransformationProperties.CaseConversion::class.java.simpleName + "|"
                    + QueryType::class.java.simpleName + "|"
                    + AbstractLdapProperties.LdapType::class.java.simpleName + "|"
                    + CaseCanonicalizationMode::class.java.simpleName + "|"
                    + PasswordPolicyProperties.PasswordPolicyHandlingOptions::class.java.simpleName + "|"
                    + LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes::class.java.simpleName + "|"
                    + Map::class.java.simpleName + "|"
                    + Resource::class.java.simpleName + "|"
                    + List::class.java.simpleName + "|"
                    + Set::class.java.simpleName).toRegex())
        }

    }

    private fun locatePropertiesClassForType(type: ClassOrInterfaceType): Class<*>? {
        if (cachedPropertiesClasses.containsKey(type.nameAsString)) {
            return cachedPropertiesClasses[type.nameAsString]
        }

        val filterInputs = { s: String? -> s != null && s.contains(type.nameAsString) }
        val filterResults = { s: String? -> s != null && s.endsWith(type.nameAsString) }
        val packageName = ConfigurationMetadataGenerator::class.java.`package`.name
        val reflections = Reflections(ConfigurationBuilder()
                .filterInputsBy(filterInputs)
                .setUrls(ClasspathHelper.forPackage(packageName))
                .setScanners(TypeElementsScanner()
                        .includeFields(false)
                        .includeMethods(false)
                        .includeAnnotations(false)
                        .filterResultsBy(filterResults),
                        SubTypesScanner(false)))
        val clz = reflections.getSubTypesOf(Serializable::class.java).stream()
                .filter { c -> c.simpleName.equals(type.nameAsString, ignoreCase = true) }
                .findFirst()
                .orElseThrow { IllegalArgumentException("Cant locate class for " + type.nameAsString) }
        cachedPropertiesClasses.put(type.nameAsString, clz)
        return clz
    }

    private fun processHints(props: Collection<ConfigurationMetadataProperty>,
                             groups: Collection<ConfigurationMetadataProperty>): Set<ConfigurationMetadataHint> {

        val hints = LinkedHashSet<ConfigurationMetadataHint>()

        for (entry in props) {
            try {
                val propName = StringUtils.substringAfterLast(entry.name, ".")
                val groupName = StringUtils.substringBeforeLast(entry.name, ".")
                val grp = groups
                        .stream()
                        .filter { g -> g.name.equals(groupName, ignoreCase = true) }
                        .findFirst()
                        .orElseThrow { IllegalArgumentException("Cant locate group " + groupName) }

                val matcher = PATTERN_GENERICS.matcher(grp.type)
                val className = if (matcher.find()) matcher.group(1) else grp.type
                val clazz = ClassUtils.getClass(className)


                val hint = ConfigurationMetadataHint()
                hint.name = entry.name

                if (clazz.isAnnotationPresent(RequiresModule::class.java)) {
                    val annotation = Arrays
                            .stream(clazz.annotations)
                            .filter { it.annotationClass == RequiresModule::class.java }
                            .findFirst()
                            .map({ RequiresModule::class.java.cast(it) })
                            .get()
                    val valueHint = ValueHint()
                    valueHint.value = Stream.of(RequiresModule::class.java.name, annotation.automated).collect(Collectors.toList())
                    valueHint.description = annotation.name
                    hint.values.add(valueHint)
                }

                val foundRequiredProperty = StreamSupport.stream(RelaxedNames.forCamelCase(propName).spliterator(), false)
                        .map<Field> { n -> ReflectionUtils.findField(clazz, n) }
                        .anyMatch { f -> f != null && f!!.isAnnotationPresent(RequiredProperty::class.java) }

                if (foundRequiredProperty) {
                    val valueHint = ValueHint()
                    valueHint.value = RequiredProperty::class.java.name
                    hint.values.add(valueHint)
                }

                if (!hint.values.isEmpty()) {
                    hints.add(hint)
                }
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
            }

        }
        return hints
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConfigurationMetadataGenerator::class.java)
        private val PATTERN_GENERICS = Pattern.compile(".+\\<(.+)\\>")
        private val NESTED_TYPE_PATTERN = Pattern.compile("java\\.util\\.\\w+<(org\\.apereo\\.cas\\..+)>")

        /**
         * Main.
         *
         * @param args the args
         * @throws Exception the exception
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val buildDir = args[0]
            val projectDir = args[1]
            ConfigurationMetadataGenerator(buildDir, projectDir).execute()
        }
    }
}

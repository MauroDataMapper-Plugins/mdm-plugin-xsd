/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper

import uk.ac.ox.softeng.maurodatamapper.core.facet.Metadata
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.EnumerationType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.LocalSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.NoFixedFacet
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Restriction
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.RestrictionCapable
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_LIST
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_RESTRICTION_BASE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_UNION

/**
 * @since 24/08/2017
 */
class SimpleTypeWrapper extends AnnotatedWrapper<AbstractSimpleType> implements XsdNaming, RestrictionCapable {

    /**
     * The following types are known to perform unnecessary restrictions.
     * They are grouped by the XSD/Model in which they are known to exist.
     * An example is the 'bl' type which extends xs:boolean with a pattern restriction of 'true|false', this is pointless as the boolean type
     * restricts that already.
     */
    public static final Map<String, List<String>> KNOWN_UNNECESSARY_TYPES = [cosd: []]


    SimpleTypeWrapper(XsdSchemaService xsdSchemaService, AbstractSimpleType wrappedElement) {
        super(xsdSchemaService, wrappedElement)
    }

    SimpleTypeWrapper(XsdSchemaService xsdSchemaService, AbstractSimpleType wrappedElement, String name) {
        this(xsdSchemaService, wrappedElement)
        givenName = name
    }

    @Override
    String getName() {
        givenName ?: createSimpleTypeName(wrappedElement.getName())
    }

    @Override
    RestrictionWrapper getRestriction() {
        wrappedElement.getRestriction() ? new RestrictionWrapper(xsdSchemaService, wrappedElement.getRestriction()) : null
    }

    Boolean isUnion() {
        wrappedElement.getUnion()?.getMemberTypes()
    }

    Boolean isList() {
        wrappedElement.getList()?.getItemType()
    }

    Boolean isLocalSimpleType() {
        wrappedElement instanceof LocalSimpleType
    }

    Boolean isKnownUnnecessaryType() {
        name in KNOWN_UNNECESSARY_TYPES.values().flatten()
    }

    Boolean isSameAsDataType(DataType dataType, SchemaWrapper schema) {
        if (isRestricted()) {
            Collection<Metadata> xsdMd = dataType.findMetadataByNamespace(METADATA_NAMESPACE)
            Metadata restrictMd = xsdMd.find {it.key == METADATA_XSD_RESTRICTION_BASE}
            String restrictionName = standardiseTypeName(restriction.extensionName)
            DataType rb = schema.getDataType(restrictionName)

            if (!restrictMd ||
                restrictMd.value != rb.label ||
                xsdMd.size() != (restrictions.size() + 1)
            ) return false

            return restrictions.every {k, v -> xsdMd.find {it.key == k && it.value == v}}
        }

        if (isUnion()) {
            debug('Checking for union similarity: {} >> {}', standardiseTypeName(wrappedElement.name ?: name, false), dataType.label)
            Metadata unionMd = dataType.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_UNION)
            if (!unionMd) return false
            return unionMd.value == wrappedElement.getUnion().getMemberTypes().collect {it.getLocalPart()}.join(' ')
        }

        if (isList()) {
            debug('Checking for list similarity: {} >> {}', standardiseTypeName(wrappedElement.name ?: name, false), dataType.label)
            Metadata listMd = dataType.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_LIST)
            if (!listMd) return false
            return listMd.value == wrappedElement.getList().getItemType().getLocalPart()
        }
        false
    }

    void incrementName() {
        givenName = name.find(/(\w+)(\.(\d+))?/) {
            int i = it[3]?.toInteger() ?: 0
            "${it[1]}.${i + 1}"
        }
    }

    DataType findOrCreateRestrictionBase(User user, DataModel dataModel, SchemaWrapper schema) {
        if (!restriction.hasBase()){
           SimpleTypeWrapper base = new SimpleTypeWrapper(xsdSchemaService, restriction.wrappedElement.simpleType, "baseSimpleType${getName()}")
            if (base) return schema.findOrCreateDataType(base, user, dataModel)
            warn('Unknown extension type [{}]', restriction.extensionFqdn)
            return null
        }
        else if (restriction.extensionNamespace != XS_NAMESPACE) {
            SimpleTypeWrapper base = schema.getSimpleTypeByName(restriction.extensionName)
            if (base) return schema.findOrCreateDataType(base, user, dataModel)
            warn('Unknown extension type [{}]', restriction.extensionFqdn)
            return null
        }
        schema.getDataType(restriction.extensionName)
    }

    DataType createDataType(User user, DataModel dm, SchemaWrapper schema) {

        debug('Creating new DataType')

        if (isRestricted()) {
            DataType restrictionBase
          //  debug('Restriction base type "{}"', restriction.extensionFqdn)
            restrictionBase = findOrCreateRestrictionBase(user, dm, schema)

            List<FacetWrapper> enums = getEnumerations(schema)

            if (enums.isEmpty()) {

                // Primitive type
                trace('Is a restriction primitive type')
                if (hasRestrictions() || isKnownUnnecessaryType()) {
                    PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dm, getName(), extractDescriptionFromAnnotations(), user)
                    addRestrictionsToMetadata(pt, user, restrictionBase)
                    return pt
                }
                warn('No restrictions present removing unnecessary simple type and replacing with "{}"', restrictionBase.label)
                return restrictionBase
            }

            trace('Is a simple type with {} enumerations', enums.size())
            // Enumerated type
            EnumerationType et = xsdSchemaService.createEnumerationTypeForDataModel(dm, getName(), extractDescriptionFromAnnotations(), user)
            addRestrictionsToMetadata(et, user, restrictionBase)

            enums.each {fw ->
                xsdSchemaService.addEnumerationValueToEnumerationType(et, fw.getValue(), fw.extractDescriptionFromAnnotations(), user)
            }
            return et

        }

        if (isUnion()) {
            trace('Is a union primitive type')
            PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dm, getName(), extractDescriptionFromAnnotations(), user)
            String members = wrappedElement.getUnion().getMemberTypes().collect {mem -> mem.getLocalPart()}.join(' ')
            addMetadataToComponent(pt, METADATA_XSD_UNION, members, user)
            return pt
        }

        if (isList()) {
            trace('Is a list primitive type')
            PrimitiveType pt = xsdSchemaService.createPrimitiveTypeForDataModel(dm, getName(), extractDescriptionFromAnnotations(), user)
            addMetadataToComponent(pt, METADATA_XSD_LIST, wrappedElement.getList().getItemType().getLocalPart(), user)
            return pt
        }
        warn('Unknown/Unhandled DataType')
        null
    }

    List<FacetWrapper> getEnumerations(SchemaWrapper schema) {
        List<FacetWrapper> enums = []
        if (getRestriction()) {

            Collection enumerations = getRestriction().findRestrictionsOfKind(RestrictionKind.enumeration).collect {
                new FacetWrapper(xsdSchemaService, it.getValue() as NoFixedFacet)
            }
            enums.addAll(enumerations)

            // Enumeration extension
            if (getRestriction().isExtension()) {
                SimpleTypeWrapper st = schema.getSimpleTypeByName(getRestriction().getExtensionName())
                if (st) enums.addAll(st.getEnumerations(schema))
            }
        }

        enums
    }

    boolean matchesName(String name) {
        name && (name == getName() || name == wrappedElement.getName())
    }


    void setName(String name) {
        givenName = name
        wrappedElement.setName(name)
    }

    QName getType() {
        return getTypeForName(getName())
    }

    private void checkBaseTypeExists(User user, DataModel dataModel, SchemaWrapper schema, QName baseType) {
        debug("Find or create base type '{}'", baseType.getLocalPart())
        SimpleTypeWrapper base = schema.getSimpleTypeByName(getRestriction().getBase().getLocalPart())
        if (base == null) return
        base.createDataType(user, dataModel, schema)
    }

    private void populateFromDataType(DataType dataType, String typeName) {
        setName(typeName)
        debug("Populating from {}", dataType)
        wrappedElement.setAnnotation(createAnnotation(dataType.getDescription()))

        Set<Metadata> xsdMetadata = dataType.findMetadataByNamespace(METADATA_NAMESPACE)

        if (xsdMetadata.isEmpty()) {
            warn("SimpleType cannot be reliably created as no defined XSD data in {}", dataType.getLabel())
            return
        }

        String restrictionType = findMetadata(xsdMetadata, METADATA_XSD_RESTRICTION_BASE)
        if (Strings.isNullOrEmpty(restrictionType)) {
            warn("SimpleType cannot be reliably created as no XSD restriction base defined in {}", dataType.getLabel())
            return
        }

        Restriction restriction = buildRestriction(xsdMetadata, restrictionType, dataType)

        // Remove unnecessary simple types which only restrict a base type but dont add anything
        if (restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().isEmpty()) {
            warn("SimpleType is unnecessary as no additional XSD metadata in {}, therefore removing", dataType.getLabel())
            setName(restrictionType)
            return
        }

        wrappedElement.setRestriction(restriction)
        // TODO List & Union
    }

    private Restriction buildRestriction(Collection<Metadata> xsdMetadata, String restrictionType, DataType dataType) {
        Restriction restriction = new Restriction()
        restriction.setBase(getTypeForName(restrictionType))

        xsdMetadata.findAll {md -> !md.getKey().equalsIgnoreCase(METADATA_XSD_RESTRICTION_BASE)}.each {md ->
            RestrictionKind rk = RestrictionKind.findFromDisplayText(md.getKey())

            if (rk != null) {
                trace("Defining restriction {} : {}", rk, md.getValue())
                Object element = RestrictionWrapper.createRestrictionElement(rk, md.getValue())
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element)
            } else {
                warn("Unknown restriction type {}", md.getKey())
            }
        }

        if (dataType.instanceOf(EnumerationType.class)) {
            trace("DataType is enumerationType with {} values", ((EnumerationType) dataType).getEnumerationValues().size())

            ((EnumerationType) dataType).getEnumerationValues().sort().each {ev ->
                Object element = RestrictionWrapper.createRestrictionElement(RestrictionKind.enumeration, ev.getKey(),
                        createAnnotation(ev.getValue()))
                if (element != null) restriction.getMinExclusivesAndMinInclusivesAndMaxExclusives().add(element)
            }
        }


        return restriction
    }

    static SimpleTypeWrapper createSimpleType(XsdSchemaService xsdSchemaService, DataType dataType, String typeName) {
        SimpleTypeWrapper wrapper = new SimpleTypeWrapper(xsdSchemaService, new SimpleType())

        if (XsdMetadata.PRIMITIVE_XML_TYPES.contains(typeName)) {
            wrapper.wrappedElement.setName(typeName)
            return wrapper
        }

        wrapper.populateFromDataType(dataType, typeName)
        return wrapper
    }


    private String findMetadata(Set<Metadata> metadata, String key) {
        Metadata optional = metadata.find {md -> md.getKey().equalsIgnoreCase(key)}
        return optional?.getValue()
    }
}

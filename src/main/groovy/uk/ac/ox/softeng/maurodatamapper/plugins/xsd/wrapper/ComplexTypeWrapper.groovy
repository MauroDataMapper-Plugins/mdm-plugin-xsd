/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.*
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.RestrictionCapable
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming
import uk.ac.ox.softeng.maurodatamapper.security.User

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName
import java.util.List

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin.*
/**
 * @since 24/08/2017
 */
class ComplexTypeWrapper extends ComplexContentWrapper<AbstractComplexType> implements XsdNaming, RestrictionCapable {

    boolean local

    ComplexTypeWrapper(XsdSchemaService xsdSchemaService, AbstractComplexType wrappedElement) {
        super(xsdSchemaService, wrappedElement)
        local = false
    }

    ComplexTypeWrapper(XsdSchemaService xsdSchemaService, AbstractComplexType wrappedElement, String name) {
        this(xsdSchemaService, wrappedElement)
        givenName = name
        local = true
    }

    @Override
    String getName() {
        if (!givenName) givenName = createComplexTypeName(wrappedElement.getName())
        givenName
    }


    @Override
    RestrictionWrapper getRestriction() {
        if (getComplexContent()?.getRestriction())
            return new RestrictionWrapper(xsdSchemaService, getComplexContent().getRestriction())
        if (getSimpleContent()?.getRestriction())
            return new RestrictionWrapper(xsdSchemaService, getSimpleContent().getRestriction())
        null
    }

    @Override
    ExplicitGroup getAll() {
        wrappedElement.getAll()
    }

    @Override
    ExplicitGroup getChoice() {
        wrappedElement.getChoice()
    }

    @Override
    ExplicitGroup getSequence() {
        wrappedElement.getSequence()
    }

    @Override
    List<Annotated> getAttributesAndAttributeGroups() {
        wrappedElement.getAttributesAndAttributeGroups()
    }

    ComplexContent getComplexContent() {
        wrappedElement.getComplexContent()
    }

    SimpleContent getSimpleContent() {
        wrappedElement.getSimpleContent()
    }

    boolean matchesName(String name) {
        name && (name == getName() || name == wrappedElement.getName())
    }

    ExtensionTypeWrapper getExtension() {
        if (getComplexContent()?.getExtension())
            return new ExtensionTypeWrapper(xsdSchemaService, getComplexContent().getExtension())
        if (getSimpleContent()?.getExtension())
            return new ExtensionTypeWrapper(xsdSchemaService, getSimpleContent().getExtension())
        null
    }

    List<BaseAttributeWrapper> getAttributes() {
        List<BaseAttribute> attributes = new ArrayList<>(getBaseAttributes())
        if (getRestriction()) attributes.addAll(getRestriction().getBaseAttributes())
        if (getExtension()) attributes.addAll(getExtension().getBaseAttributes())
        trace('Found {} attributes to be added as elements', attributes.size())
        attributes.collect {new BaseAttributeWrapper(xsdSchemaService, it)}
    }

    List<ElementWrapper> getAllElements() {
        List<ElementWrapper> subElements = new ArrayList<>(getElements())
        if (getRestriction()) subElements.addAll(getRestriction().getElements())
        if (getExtension()) subElements.addAll(getExtension().getElements())
        subElements
    }

    Boolean isActuallySimpleType() {
        if (getAllElements()) return false
        List<BaseAttributeWrapper> attributeWrappers = getAttributes()
        if (!attributeWrappers || attributeWrappers.size() == 1) return true
        false
    }

    SimpleTypeWrapper convertToSimpleType() {
        if (getAttributes()) {
            BaseAttributeWrapper singleAttribute = getAttributes().first()
            return new SimpleTypeWrapper(xsdSchemaService, singleAttribute.simpleType, createSimpleTypeName(getName(), true))
        }
        null
    }

    Boolean isSameAsDataClass(DataClass dataClass) {

        if (!local && standardiseTypeName(wrappedElement.name, false) == dataClass.label) return true

        Set<String> elements = getAllElements().collect {it.getName()}.toSet() + getAttributes().collect {it.name}
        Set<String> dcElements = dataClass.dataElements.collect {it.label}.toSet()

        if (dcElements == elements) return true
        else if (elements.every {it in dcElements}) {
            String dcBase = null
            String base = null
            if (getRestriction()) {
                dcBase = dataClass.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_RESTRICTION_BASE)?.value
                base = createComplexTypeName(getRestriction().extensionName)
            } else if (getExtension()) {
                dcBase = dataClass.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_EXTENSION_BASE)?.value
                base = createComplexTypeName(getExtension().extensionName)
            }
            return dcBase && base && dcBase == base
        }
        false
    }

    void incrementName() {
        givenName = name.find(/(\w+)(\.(\d+))?/) {
            int i = it[3]?.toInteger() ?: 0
            "${it[1]}.${i + 1}"
        }
    }

    DataClass createDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                              QName type, Integer minOccurs, Integer maxOccurs) {
        long start = System.currentTimeMillis()
        DataClass dataClass = initialiseDataClass(user, parentDataModel, parentDataClass, type, minOccurs, maxOccurs)
        populateDataClass(user, parentDataModel, parentDataClass, schema, dataClass)
        schema.dataStore.dataClassCreations << System.currentTimeMillis() - start
        dataClass
    }

    DataClass initialiseDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, QName type,
                                  Integer minOccurs, Integer maxOccurs) {
        CatalogueItem parent = parentDataClass ?: parentDataModel
        // Only allow min and max occurs when dataclass is top level class
        Integer min = parentDataClass ? null : minOccurs
        Integer max = parentDataClass ? null : maxOccurs
        debug('Creating new DataClass with parent "{}"', parent.getLabel())
        DataClass dataClass
        if(parentDataClass)  {
            dataClass = xsdSchemaService.createDataClass(parentDataClass, getName(), extractDescriptionFromAnnotations(), user, min, max)
        }
        else{
            dataClass = xsdSchemaService.createDataClass(parentDataModel, getName(), extractDescriptionFromAnnotations(), user, min, max)
        }

        if (!dataClass.getDescription() && type) dataClass.setDescription(type.getLocalPart())

        if (type) {
            addMetadataToComponent(dataClass, METADATA_LABEL_PREFIX + 'Complex Type Name', type.getLocalPart(), user)
        }

        if (wrappedElement.isAbstract()) {
            addMetadataToComponent(dataClass, METADATA_LABEL_PREFIX + 'Abstract', 'true', user)
        }
        if (wrappedElement.isMixed()) {
            addMetadataToComponent(dataClass, METADATA_LABEL_PREFIX + 'Mixed', 'true', user)
        }
        dataClass
    }

    DataClass populateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                DataClass dataClass) {
        if (getRestriction()) {
            DataClass baseClass = null
            if (getRestriction().isExtension()) {
                baseClass = findOrCreateBaseTypeDataClass(user, parentDataModel, parentDataClass, schema, getRestriction().getBase())
            }
            addRestrictionsToMetadata(dataClass, user, baseClass)
        }

        debug('Adding sub-elements to DataClass')

        List<ElementWrapper> subElements = getAllElements()
        List<BaseAttributeWrapper> attributes = getAttributes()

        if (subElements.isEmpty() && attributes.isEmpty()) {

            if (getExtension()) {
                ComplexTypeWrapper extensionComplexType = schema.getComplexTypeByName(getExtension().base.localPart)
                warn('complexType has no content, replacing with {}', extensionComplexType.name)
                return schema.findOrCreateDataClass(user, parentDataModel, parentDataClass, extensionComplexType)
            } else {
                warn('complexType has no content, you need to remove it')
            }
        }

        subElements.each {subElement -> subElement.createDataModelElement(user, parentDataModel, dataClass, schema)}
        attributes.each {attribute -> attribute.createDataModelElement(user, parentDataModel, dataClass, schema)}

        if (getExtension()) {
            return extendDataClass(user, parentDataModel, schema, dataClass)
        }

        dataClass
    }

    DataClass findOrCreateBaseTypeDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                            QName baseType) {
        debug('Type has base type {} loading class into model', baseType.getLocalPart())
        ComplexTypeWrapper base = schema.getComplexTypeByName(baseType.getLocalPart())
        schema.findOrCreateDataClass(user, parentDataModel, parentDataClass, base)
    }


    DataClass extendDataClass(User user, DataModel parentDataModel, SchemaWrapper schema, DataClass dataClass) {
        debug('Extends type {} collapsing elements', getExtension().getBase().getLocalPart())

        addMetadataToComponent(dataClass, METADATA_XSD_EXTENSION_BASE,
                               createComplexTypeName(getExtension().getBase().getLocalPart()), user)

        if (getSimpleContent()) {
            debug('Simple content extension, adding "value" element')
            // Extending a datatype, so we add a new element 'value' to hold the content
            DataType dataType = findBaseTypeDataType(schema, getExtension().getBase())
            xsdSchemaService.createDataElementForDataClass(dataClass, 'value', extractDescriptionFromAnnotations(), user, dataType,
                                                           1, 1)

        } else {
            // Standard complex element extending another
            DataClass extension = findOrCreateBaseTypeDataClass(user, parentDataModel, dataClass, schema, getExtension().getBase())

            if (extension == null) {
                warn('Extends type {} but data class has not been found or created', getExtension().getBase().getLocalPart())
                return dataClass
            }

            if (extension.getDataElements()) {
                extension.getDataElements().each {el -> xsdSchemaService.createDuplicateElementForDataClass(user, dataClass, el)}
            }

        }
        dataClass
    }

    DataType findBaseTypeDataType(SchemaWrapper schema, QName baseType) {
        String typeName = createSimpleTypeName(baseType.getLocalPart(),false)
        debug("Type has base type {} loading DataType into model", typeName)

        DataType dataType = schema.getDataType(typeName)
        if (dataType == null) {
            warn("element is a {} typed element but it has not been created", typeName)
        }

        return dataType
    }


    void populateFromDataClass(SchemaWrapper schema, DataClass dataClass) {
        setName(createComplexTypeName(dataClass), true)
        debug("Populating from {}", dataClass)
        wrappedElement.setAnnotation(createAnnotation(dataClass.getDescription()))

        if (dataClass.getDataElements() != null) {
            Set<DataElement> childDataElements = xsdSchemaService.getDataElements(dataClass)
            Map<String, List<DataElement>> grouped = childDataElements
                    .sort {it.getLabel()}
                    .groupBy {dataElement ->
                        Metadata md = dataElement.findMetadataByNamespaceAndKey(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_CHOICE)
                        if (md != null) return md.getValue()
                        md = dataElement.findMetadataByNamespaceAndKey(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_ALL)
                        if (md != null) return "all"
                        return "sequence"
                    }

            if (grouped.isEmpty()) return

            if (grouped.size() == 1) {
                String type = (String) grouped.keySet().toArray()[0]
                ExplicitGroup children = buildChildGroup(schema, type, grouped.get(type))
                switch (type) {
                    case "all":
                        wrappedElement.setAll((All) children)
                        break
                    case "sequence":
                        wrappedElement.setSequence(children)
                        break
                    default:
                        wrappedElement.setChoice(children)
                }
            } else {
                ExplicitGroup parent = new ExplicitGroup()
                grouped.each {type, elements ->
                    ExplicitGroup children = buildChildGroup(schema, type, elements)
                    String elType = type.contains("choice") ? "choice" : type
                    JAXBElement<ExplicitGroup> jaxb = new JAXBElement<>(new QName(XS_NAMESPACE, elType), ExplicitGroup.class, children)
                    parent.getElementsAndGroupsAndAlls().add(jaxb)


                }
                wrappedElement.setSequence(parent)
            }
        }
    }


   static ComplexTypeWrapper createComplexType(SchemaWrapper schema, DataClass dataClass) {
        ComplexTypeWrapper wrapper = new ComplexTypeWrapper(schema.xsdSchemaService, new ComplexType())
        wrapper.populateFromDataClass(schema, dataClass)
        return wrapper
    }

   static private ExplicitGroup buildChildGroup(SchemaWrapper schema, String type, List<DataElement> elements) {
       ExplicitGroup children
       if (type.equalsIgnoreCase('all')) children = new All()
       else children = new ExplicitGroup()
       elements.each {de -> children.getElementsAndGroupsAndAlls().add(ElementWrapper.createElement(schema, de).wrappedElement)}
       children
   }



}

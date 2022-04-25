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
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.ReferenceType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Appinfo
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Element
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.LocalElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleExtensionType
import uk.ac.ox.softeng.maurodatamapper.security.User

import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_NAMESPACE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_ALL
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_CHOICE
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_DEFAULT
import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_FIXED

/**
 * @since 24/08/2017
 */
class ElementWrapper extends ElementBasedWrapper<AbstractElement> {

    private boolean allElement
    private boolean choiceElement
    private String choiceGroup
    private Integer maxOccurs
    private Integer minOccurs

    ElementWrapper(XsdSchemaService xsdSchemaService, AbstractElement element) {
        super(xsdSchemaService, element)
    }

    //    private ElementWrapper(XsdSchemaService xsdSchemaService, AbstractElement wrappedElement, String name) {
    //        super(xsdSchemaService, wrappedElement, name)
    //    }

    @Override
    String getName() {
        givenName ?: wrappedElement.getName()
    }

    @Override
    SimpleExtensionType getComplexSimpleContentExtension() {
        wrappedElement.getComplexType() && wrappedElement.getComplexType().getSimpleContent() ?
        wrappedElement.getComplexType().getSimpleContent().getExtension() : null
    }

    @Override
    AbstractComplexType getComplexType() {
        wrappedElement.getComplexType()
    }

    @Override
    Integer getMaxOccurs() {
        if (maxOccurs == null) setMaxOccurs(wrappedElement.getMaxOccurs())
        maxOccurs
    }

    @Override
    Integer getMinOccurs() {
        if (minOccurs == null) setMinOccurs(wrappedElement.getMinOccurs())
        minOccurs
    }

    @Override
    RestrictionWrapper getRestriction() {
        getComplexType()?.getComplexContent()?.getRestriction() ?
        new RestrictionWrapper(xsdSchemaService, getComplexType().getComplexContent().getRestriction()) : null
    }

    @Override
    QName getRef() {
        wrappedElement.getRef()
    }

    @Override
    AbstractSimpleType getSimpleType() {
        wrappedElement.getSimpleType()
    }

    @Override
    QName getType() {
        wrappedElement.getType()
    }

    @Override
    DataElement createDataModelElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        DataElement element = super.createDataModelElement(user, parentDataModel, parentDataClass, schema)

        if (element == null) return null

        trace('Adding extra attribute values as metadata')

        if (wrappedElement.getDefault()) {
            addMetadataToComponent(element, METADATA_XSD_DEFAULT, wrappedElement.getDefault(), user)
        }
        if (wrappedElement.getFixed()) {
            addMetadataToComponent(element, METADATA_XSD_FIXED, wrappedElement.getFixed(), user)
        }
        if (choiceElement) {
            addMetadataToComponent(element, METADATA_XSD_CHOICE, choiceGroup, user)
        } else if (allElement) {
            addMetadataToComponent(element, METADATA_XSD_ALL, 'true', user)
        }
        element
    }

    void setMinOccurs(BigInteger minOccurs) {
        this.@minOccurs = minOccurs.intValueExact()
    }

    void setMaxOccurs(String max) {
        if (max == 'unbounded') this.@maxOccurs = -1
        else {
            try {
                this.@maxOccurs = max.toInteger()
            } catch (NumberFormatException ignored) {
                this.@maxOccurs = null
            }
        }
    }

    void setAllElement() {
        allElement = true
    }

    void setChoiceGroup(String group) {
        choiceElement = true
        choiceGroup = group
    }

    void populateFromDataClass(SchemaWrapper schema, DataClass dataClass) {
        wrappedElement.setName(createValidXsdName(dataClass.getLabel()))
        debug("Populate element from {}", dataClass)
        wrappedElement.setAnnotation(createAnnotation(getAnnotation(), dataClass.getDescription()))
        ComplexTypeWrapper complexTypeWrapper = schema.findOrCreateComplexType(dataClass)
        wrappedElement.setType(new QName(complexTypeWrapper.getName()))
    }

    void populateFromDataElement(SchemaWrapper schema, DataElement dataElement) {
        wrappedElement.setName(createValidXsdName(dataElement.getLabel()))
        debug("Populating from {}", dataElement)
        wrappedElement.setAnnotation(createAnnotation(getAnnotation(), dataElement.getDescription()))
        wrappedElement.setMinOccurs(BigInteger.valueOf(dataElement.getMinMultiplicity()))
        wrappedElement.setMaxOccurs(dataElement.getMaxMultiplicity() == -1 ? "unbounded" : dataElement.getMaxMultiplicity().toString())

        DataType dataType = dataElement.getDataType()
        if (dataType.instanceOf(ReferenceType.class)) {
            trace("Is a complexType element")
            ComplexTypeWrapper complexTypeWrapper = schema.findOrCreateComplexType(((ReferenceType) dataType).getReferenceClass())
            wrappedElement.setType(new QName(complexTypeWrapper.getName()))
        } else {
            trace("Is a simpleType element")
            SimpleTypeWrapper simpleTypeWrapper = schema.findOrCreateSimpleType(dataElement.getDataType())
            debug("Setting {} type to {}", dataElement.getDataType(), simpleTypeWrapper.getType())
            wrappedElement.setType(simpleTypeWrapper.getType())
            List<Appinfo> appinfoList = simpleTypeWrapper.getAllAppInfo()
            if (appinfoList) {
                Annotation annotation = getAnnotation()
                annotation.appinfosAndDocumentations.addAll(appinfoList)
                wrappedElement.setAnnotation(annotation)
            }
        }

        Metadata defaultValue = dataElement.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_DEFAULT)
        if (defaultValue != null) {
            trace("Adding default value to element")
            wrappedElement.setDefault(defaultValue.getValue())
        }
        Metadata fixedValue = dataElement.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_FIXED)
        if (fixedValue != null) {
            trace("Adding fixed value to element")
            wrappedElement.setDefault(fixedValue.getValue())
        }
    }

    static ElementWrapper createElement(SchemaWrapper schema, DataElement dataElement) {

        ElementWrapper wrapper = new ElementWrapper(schema.xsdSchemaService, new LocalElement())
        wrapper.populateFromDataElement(schema, dataElement)
        wrapper
    }

    static ElementWrapper createElement(SchemaWrapper schema, DataClass dataClass) {

        ElementWrapper wrapper = new ElementWrapper(schema.xsdSchemaService, new Element())
        wrapper.populateFromDataClass(schema, dataClass)
        wrapper
    }
}

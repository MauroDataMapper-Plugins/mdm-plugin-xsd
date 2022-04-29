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

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleExtensionType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.RestrictionCapable
import uk.ac.ox.softeng.maurodatamapper.security.User

import javax.xml.namespace.QName

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata.METADATA_XSD_ATTRIBUTE_NAME

/**
 * @since 24/08/2017
 */
abstract class ElementBasedWrapper<K extends Annotated> extends AnnotatedWrapper<K> implements RestrictionCapable {

    Map<String, String> additionalMetadata = [:]

    ElementBasedWrapper(XsdSchemaService xsdSchemaService, K element) {
        super(xsdSchemaService, element)
    }

    //    ElementBasedWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
    //        super(xsdSchemaService, wrappedElement, name)
    //    }

    abstract SimpleExtensionType getComplexSimpleContentExtension();

    abstract AbstractComplexType getComplexType();

    abstract Integer getMaxOccurs();

    abstract Integer getMinOccurs();

    abstract QName getRef();

    abstract AbstractSimpleType getSimpleType();

    abstract QName getType();

    boolean isReferenceElement() {
        getName() == null && getRef()
    }

    boolean isLocalComplexType() {
        getComplexType()
    }

    boolean isLocalSimpleType() {
        getSimpleType()
    }

    List<Annotated> getAttributesAndAttributeGroups() {
        getRestriction()?.getAttributesAndAttributeGroups() ?: Collections.emptyList() as List<Annotated>
    }

    BaseAttribute getBaseTypeAttribute() {
        for (Annotated annotated : getAttributesAndAttributeGroups()) {
            if (annotated instanceof BaseAttribute && ((BaseAttribute) annotated).getSimpleType()) {
                return (BaseAttribute) annotated
            }
        }
        null
    }

    DataElement createDataModelElement(User user, DataModel parentDataModel, SchemaWrapper schema) {
        createDataModelElement(user, parentDataModel, null, schema)
    }

    DataElement createDataModelElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        long start = System.currentTimeMillis()
        debug('Creating DataModel Element')
        DataElement dataElement
        if (isReferenceElement()) {
            debug('Creating element "{}" from reference', getRef().getLocalPart())
            ElementBasedWrapper refElement = schema.getElementByName(getRef().getLocalPart())
            if(refElement){
            dataElement = refElement.createDataModelElement(user, parentDataModel, parentDataClass, schema)}
            else{
                warn("Unable to create Data Model Element as Ref Element doesn't exist")
            }
        } else {
            ComplexTypeWrapper complexTypeWrapper = getComplexTypeWrapper(schema)

            if (complexTypeWrapper) {
                debug('Has a complex type {}', complexTypeWrapper.name)

                if (complexTypeWrapper.isActuallySimpleType()) {
                    debug('Complex type {} is actually a simple type masquerading as a complex type', complexTypeWrapper.name)
                    SimpleTypeWrapper simpleTypeWrapper = complexTypeWrapper.convertToSimpleType()
                    if (!complexTypeWrapper.attributes.isEmpty()) additionalMetadata[(METADATA_XSD_ATTRIBUTE_NAME)] = complexTypeWrapper.attributes.first().name
                    dataElement = createDataTypeElement(user, parentDataModel, parentDataClass, schema, simpleTypeWrapper)
                } else {
                    dataElement = createDataClassElement(user, parentDataModel, parentDataClass, schema, complexTypeWrapper)
                }
            } else dataElement = createDataTypeElement(user, parentDataModel, parentDataClass, schema, getSimpleTypeWrapper(schema))
        }
        schema.dataStore.dataElementCreations << System.currentTimeMillis() - start
        dataElement
    }

    DataElement createDataTypeElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                      SimpleTypeWrapper wrapper) {
        debug('Creating a DataType/simpleType element')

        DataType dataType = findOrCreateDataType(user, parentDataModel, schema, wrapper)
        if (dataType) {
            DataElement dataElement = xsdSchemaService.createDataElementForDataClass(parentDataClass, getName(),
                                                                                     extractDescriptionFromAnnotations(), user, dataType,
                                                                                     getMinOccurs(), getMaxOccurs())
            additionalMetadata.each {k, v ->
                addMetadataToComponent(dataElement, k, v, user)
            }
            return dataElement
        }
        warn('No DataElement has been created as no DataType found')
        null
    }

    DataElement createDataClassElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                       ComplexTypeWrapper complexType) {
        debug('Creating a DataClass/complexType element')

        DataClass dataClass = findOrCreateDataClass(user, parentDataModel, parentDataClass, schema, complexType)

        // Will only happen if dataclass is already under construction
        if (!dataClass) {
            warn('Tried to create DataClass {} but got null back as its already under construction', getType().getLocalPart())
            return null
        }

        if (!parentDataClass) {
            // Top level dataclass/element, so should use the name of element rather than the type.
            dataClass.setMaxMultiplicity(getMaxOccurs())
            dataClass.setMinMultiplicity(getMinOccurs())
            dataClass.setLabel(getName())
            return null
        }

        // Use referencetypes to link as data elements
        DataType dataType = findOrCreateDataType(user, parentDataModel, schema, dataClass, null)
        xsdSchemaService.createDataElementForDataClass(parentDataClass, getName(), extractDescriptionFromAnnotations(), user, dataType,
                                                       getMinOccurs(), getMaxOccurs())
    }

    DataClass findOrCreateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                    ComplexTypeWrapper complexType) {

        if (isLocalComplexType()) {
            // Local complex type so no need to store into schema memory
            trace('Is a local complexType, creating DataClass using element name')
            complexType.setName(createComplexTypeName(getName()))
            return complexType.createDataClass(user, parentDataModel, parentDataClass, schema, getType(), getMinOccurs(), getMaxOccurs())
        }
        // Find from schema memory or create and save
        schema.findOrCreateDataClass(user, parentDataModel, parentDataClass, complexType)
    }

    DataType findOrCreateDataType(User user, DataModel dataModel, SchemaWrapper schema, SimpleTypeWrapper wrapper) {
        findOrCreateDataType(user, dataModel, schema, null, wrapper)
    }

    DataType findOrCreateDataType(User user, DataModel dataModel, SchemaWrapper schema, DataClass referencedDataClass,
                                  SimpleTypeWrapper simpleTypeWrapper) {
        DataType dataType = null

        // Is a simple type element
        if (simpleTypeWrapper) {
            trace('Is a simpleType element')
            if (simpleTypeWrapper.getName()) {
                dataType = schema.findOrCreateDataType(simpleTypeWrapper, user, dataModel)
            }
            if (!dataType) warn('Is a simpleType element but it has no wrapper name')
            return dataType
        }

        // Creating or finding a reference datatype
        if (referencedDataClass) {
            trace('Is a referenceType element')
            dataType = schema.findOrCreateReferenceDataType(user, dataModel, referencedDataClass, extractDescriptionFromAnnotations())
            if (dataType == null) {
                warn('Is a referenceType element but it has not been created')
            }
            return dataType
        }

        // Type defined but not previously provided by schema, otherwise it would have been found as simpleTypeWrapper
        // This will enter for all elements which base primitive types
        if (getType()) {
            trace('Is of type {}', getType())
            String typeName = createSimpleTypeName(getType().getLocalPart())
            if (getType().getLocalPart()) {
                dataType = schema.getDataType(typeName)
            } else {
                warn('Has a null type local part')
            }
            if (dataType == null) {
                warn('Is a "{}" typed element but it has not been created', typeName)
            }
            return dataType
        }

        // Complex element with simple content
        SimpleExtensionType extensionType = getComplexSimpleContentExtension()
        if (extensionType) {
            warn('Is a complexType with simple content')

            String typeName = extensionType.getBase().getLocalPart()

            if (typeName) {
                dataType = schema.getDataType(typeName)
            } else {
                warn('Has a null type local part')
            }
            if (dataType == null) {
                warn('Is a {} complex simple element but it has not been created', typeName)
            }
            return dataType
        }
        error('Cannot be identified as any type')
        null
    }

    ComplexTypeWrapper getComplexTypeWrapper(SchemaWrapper schema) {
        // Local complex type
        if (isLocalComplexType()) {
            return new ComplexTypeWrapper(xsdSchemaService, getComplexType(), createComplexTypeName(getName()))
        }
        // Defined complex type
        if (getType()) {
            ComplexTypeWrapper wrapper = schema.getComplexTypeByName(getType().getLocalPart())
            return wrapper ?: schema.getComplexTypeByName(createComplexTypeName(getType().getLocalPart()))
        }
        null
    }

    SimpleTypeWrapper getSimpleTypeWrapper(SchemaWrapper schema) {
        if (isLocalSimpleType()) {
            trace('Is a local simpleType')
            return new SimpleTypeWrapper(xsdSchemaService, getSimpleType(), createSimpleTypeName(getName(), true))
        }

        if (getType()) {
            trace('Has a type {}', getType())
            SimpleTypeWrapper wrapper = schema.getSimpleTypeByName(getType().getLocalPart())
            return wrapper ?: schema.getSimpleTypeByName(createSimpleTypeName(getType().getLocalPart()))
        }

        BaseAttribute baseAttribute = getBaseTypeAttribute()
        if (baseAttribute) {
            debug('Has a base attribute {}', baseAttribute.getName())
            if (getBaseTypeAttribute().simpleType)
                return new SimpleTypeWrapper(xsdSchemaService, baseAttribute.simpleType, createSimpleTypeName(getName(), true))
        }

        null
    }

}

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

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.LocalSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleExtensionType
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.namespace.QName

/**
 * @since 24/08/2017
 */
public abstract class ElementBasedWrapper<K extends Annotated> extends AnnotatedWrapper<K> {

    ElementBasedWrapper(XsdSchemaService xsdSchemaService, K element) {
        super(xsdSchemaService, element);
    }

    ElementBasedWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
        super(xsdSchemaService, wrappedElement, name);
    }

    public DataElement createDataTypeElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        debug("Is a DataType/simpleType element");

        DataType dataType = findOrCreateDataType(user, parentDataModel, schema);
        if (dataType != null) {
            return xsdSchemaService.createDataElementForDataClass(parentDataClass, getName(), extractDescriptionFromAnnotations(), user, dataType,
                                                                  getMinOccurs(), getMaxOccurs());
        }
        warn("No DataElement has been created as no DataType found");
        return null;
    }

    public abstract SimpleExtensionType getComplexSimpleContentExtension();

    public abstract AbstractComplexType getComplexType();

    public abstract Integer getMaxOccurs();

    public abstract Integer getMinOccurs();

    public abstract QName getRef();

    public abstract AbstractSimpleType getSimpleType();

    public abstract QName getType();

    public abstract boolean isLocalComplexType();

    public abstract boolean isLocalSimpleType();

    DataElement createDataModelElement(User user, DataModel parentDataModel, SchemaWrapper schema) {
        return createDataModelElement(user, parentDataModel, null, schema);
    }

    DataElement createDataModelElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {

        if (isReferenceElement()) {
            debug("Creating element '{}' from reference", getRef().getLocalPart());
            ElementBasedWrapper refElement = schema.getElementByName(getRef().getLocalPart());
            return refElement.createDataModelElement(user, parentDataModel, parentDataClass, schema);
        }

        ComplexTypeWrapper complexType = getComplexTypeWrapper(schema);

        // Complex type element
        return complexType != null ?
               createDataClassElement(user, parentDataModel, parentDataClass, schema, complexType) :
               createDataTypeElement(user, parentDataModel, parentDataClass, schema);
    }

    private DataElement createDataClassElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                               ComplexTypeWrapper complexType) {
        debug("Is a DataClass/complexType element");

        DataClass dataClass = findOrCreateDataClass(user, parentDataModel, parentDataClass, schema, complexType);

        // Will only happen if dataclass is already under construction
        if (dataClass == null) {
            warn("Tried to create DataClass {} but got null back as its already under construction",
                 getType().getLocalPart());
            return null;
        }

        if (parentDataClass == null || schema.isCreateLinksRatherThanReferences()) {
            // Top level dataclass/element, so should use the name of element rather than the type.
            dataClass.setMaxMultiplicity(getMaxOccurs());
            dataClass.setMinMultiplicity(getMinOccurs());
            dataClass.setLabel(getName());
            return null;
        }

        // Use referencetypes to link as data elements
        DataType dataType = findOrCreateDataType(user, parentDataModel, schema, dataClass);
        return xsdSchemaService.createDataElementForDataClass(parentDataClass, getName(), extractDescriptionFromAnnotations(), user, dataType,
                                                              getMinOccurs(), getMaxOccurs());
    }

    private DataClass findOrCreateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                            ComplexTypeWrapper complexType) {

        if (isLocalComplexType()) {
            // Local complex type so no need to store into schema memory
            trace("Is a local complexType, creating DataClass using element name");
            complexType.setName(getName());
            return complexType.createDataClass(user, parentDataModel, parentDataClass, schema, getType(), getMinOccurs(), getMaxOccurs());
        }
        if (schema.isCreateLinksRatherThanReferences()) {
            complexType.setName(getName());
            return schema.createAndStoreDataClass(user, parentDataModel, parentDataClass, complexType, getType(),
                                                  getMinOccurs(), getMaxOccurs());
        }
        // Find from schema memory or create and save
        return schema.findOrCreateDataClass(user, parentDataModel, parentDataClass, complexType);

    }

    private DataType findOrCreateDataType(User user, DataModel dataModel, SchemaWrapper schema) {
        return findOrCreateDataType(user, dataModel, schema, null);
    }

    private DataType findOrCreateDataType(User user, DataModel dataModel, SchemaWrapper schema, DataClass referencedDataClass) {
        SimpleTypeWrapper simpleTypeWrapper = getSimpleTypeWrapper(schema);
        DataType dataType = null;

        // Is a simple type element
        if (simpleTypeWrapper != null) {
            trace("Is a simpleType element");
            if (!Strings.isNullOrEmpty(simpleTypeWrapper.getName())) {
                dataType = schema.computeIfDataTypeAbsent(
                    simpleTypeWrapper.getName(), {key -> simpleTypeWrapper.createDataType(user, dataModel, schema)});
            }
            if (dataType == null) {
                warn("Is a simpleType element but it has no wrapper name");
            }
            return dataType;
        }

        // Creating or finding a reference datatype
        if (referencedDataClass != null) {
            trace("Is a referenceType element");
            dataType = schema.computeIfDataTypeAbsent(
                SimpleTypeWrapper.createSimpleTypeName(referencedDataClass.getLabel()),
                {key ->
                    xsdSchemaService.createReferenceTypeForDataModel(dataModel, key, extractDescriptionFromAnnotations(), user,
                                                                     referencedDataClass)
                });
            if (dataType == null) {
                warn("Is a referenceType element but it has not been created");
            }
            return dataType;
        }

        // Type defined but not previously provided by schema, otherwise it would have been found as simpleTypeWrapper
        // This will enter for all elements which base primitive types
        if (getType() != null) {
            trace("Is of type {}", getType());
            String typeName = SimpleTypeWrapper.createSimpleTypeName(getType().getLocalPart());
            if (getType().getLocalPart() != null) {
                dataType = schema.getDataType(typeName);
            } else {
                warn("Has a null type local part");
            }
            if (dataType == null) {
                warn("Is a '{}' typed element but it has not been created", typeName);
            }
            return dataType;
        }

        // Complex element with simple content
        SimpleExtensionType extensionType = getComplexSimpleContentExtension();
        if (extensionType != null) {
            warn("Is a complexType with simple content");

            String typeName = extensionType.getBase().getLocalPart();

            if (typeName != null) {
                dataType = schema.getDataType(typeName);
            } else {
                warn("Has a null type local part");
            }
            if (dataType == null) {
                warn("Is a {} complex simple element but it has not been created", typeName);
            }
            return dataType;
        } else {
            error("Cannot be identified as any type");
        }
        return null;
    }

    private List<Annotated> getAttributesAndAttributeGroups() {
        return getRestriction() != null && getRestriction().getAttributesAndAttributeGroups() != null ?
               getRestriction().getAttributesAndAttributeGroups() :
               Collections.emptyList();
    }

    private AbstractSimpleType getBaseTypeAttribute() {
        for (Annotated annotated : getAttributesAndAttributeGroups()) {
            if (annotated instanceof BaseAttribute && ((BaseAttribute) annotated).getSimpleType() != null) {
                return ((BaseAttribute) annotated).getSimpleType();
            }
        }
        return null;
    }

    private ComplexTypeWrapper getComplexTypeWrapper(SchemaWrapper schema) {
        // Local complex type
        if (isLocalComplexType()) {
            return new ComplexTypeWrapper(xsdSchemaService, getComplexType());
        }
        // Defined complex type
        if (getType() != null) {
            ComplexTypeWrapper wrapper = schema.getComplexTypeByName(getType().getLocalPart());
            return wrapper != null ? wrapper : schema.getComplexTypeByName(
                ComplexTypeWrapper.createComplexTypeName(getType().getLocalPart()));
        }
        return null;
    }

    private SimpleTypeWrapper getSimpleTypeWrapper(SchemaWrapper schema) {
        if (isLocalSimpleType()) {
            trace("Is a local simpleType");
            return new SimpleTypeWrapper(xsdSchemaService, getSimpleType(), getName());
        }

        if (getBaseTypeAttribute() != null) {
            debug("Has a base attribute {}", getBaseTypeAttribute().getName());
            if (getBaseTypeAttribute() instanceof LocalSimpleType) return new SimpleTypeWrapper(xsdSchemaService, getBaseTypeAttribute(), getName());
            return new SimpleTypeWrapper(xsdSchemaService, getBaseTypeAttribute());
        }

        if (getType() != null) {
            trace("Has a type {}", getType());
            SimpleTypeWrapper wrapper = schema.getSimpleTypeByName(getType().getLocalPart());
            return wrapper != null ? wrapper : schema.getSimpleTypeByName(
                SimpleTypeWrapper.createSimpleTypeName(getType().getLocalPart()));
        }

        return null;
    }

    private boolean isReferenceElement() {
        return getName() == null && getRef() != null;
    }
}

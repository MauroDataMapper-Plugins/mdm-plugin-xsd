/**
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.All
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ComplexContent
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExplicitGroup
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleContent
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.bind.JAXBElement
import javax.xml.namespace.QName

/**
 * @since 24/08/2017
 */
public class ComplexTypeWrapper extends ComplexContentWrapper<AbstractComplexType> {

    ComplexTypeWrapper(XsdSchemaService xsdSchemaService, AbstractComplexType wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    public ExplicitGroup getAll() {
        return wrappedElement.getAll();
    }

    @Override
    public List<Annotated> getAttributesAndAttributeGroups() {
        return wrappedElement.getAttributesAndAttributeGroups();
    }

    public ExplicitGroup getChoice() {
        return wrappedElement.getChoice();
    }

    public ExplicitGroup getSequence() {
        return wrappedElement.getSequence();
    }

    public ComplexContent getComplexContent() {
        return wrappedElement.getComplexContent();
    }

    public ExtensionTypeWrapper getExtension() {
        return getComplexContent() != null && getComplexContent().getExtension() != null ?
               new ExtensionTypeWrapper(xsdSchemaService, getComplexContent().getExtension()) :
               getSimpleContent() != null && getSimpleContent().getExtension() != null ?
               new ExtensionTypeWrapper(xsdSchemaService, getSimpleContent().getExtension()) : null;
    }

    @Override
    public String getName() {
        return Strings.isNullOrEmpty(givenName) ? createComplexTypeName(wrappedElement.getName()) : givenName;
    }

    public void setName(String name) {
        givenName = name;
        wrappedElement.setName(name);
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return getComplexContent() != null && getComplexContent().getRestriction() != null ?
               new RestrictionWrapper(xsdSchemaService, getComplexContent().getRestriction()) :
               getSimpleContent() != null && getSimpleContent().getRestriction() != null ?
               new RestrictionWrapper(xsdSchemaService, getSimpleContent().getRestriction()) : null;

    }

    DataClass createDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                              QName type, Integer minOccurs, Integer maxOccurs) {

        DataClass dataClass = initialiseDataClass(user, parentDataModel, parentDataClass, type, minOccurs, maxOccurs);
        return populateDataClass(user, parentDataModel, parentDataClass, schema, dataClass);
    }

    DataClass initialiseDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, QName type,
                                  Integer minOccurs, Integer maxOccurs) {
        CatalogueItem parent = parentDataClass == null ? parentDataModel : parentDataClass;
        // Only allow min and max occurs when dataclass is top level class
        Integer min = parentDataClass == null ? minOccurs : null;
        Integer max = parentDataClass == null ? maxOccurs : null;
        debug("Creating new DataClass with parent '{}'", parent.getLabel());
        DataClass dataClass = xsdSchemaService.createDataClass(parent, getName(), extractDescriptionFromAnnotations(), user, min, max);

        if (Strings.isNullOrEmpty(dataClass.getDescription()) && type != null) {
            dataClass.setDescription(type.getLocalPart());
        }

        if (type != null) {
            addMetadataToComponent(dataClass, XsdPlugin.METADATA_LABEL_PREFIX + "Complex Type Name", type.getLocalPart(), user);
        }

        if (wrappedElement.isAbstract()) {
            addMetadataToComponent(dataClass, XsdPlugin.METADATA_LABEL_PREFIX + "Abstract", "true", user);
        }
        if (wrappedElement.isMixed()) {
            addMetadataToComponent(dataClass, XsdPlugin.METADATA_LABEL_PREFIX + "Mixed", "true", user);
        }
        return dataClass;
    }

    boolean matchesName(String name) {
        return !Strings.isNullOrEmpty(name) && (name.equals(getName()) || name.equals(wrappedElement.getName()));
    }

    DataClass populateDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema, DataClass dataClass) {
        if (getRestriction() != null) {
            addRestrictionsToMetadata(dataClass, user, getRestriction());
            if (getRestriction().getBase() != null) {
                findOrCreateBaseTypeDataClass(user, parentDataModel, parentDataClass, schema, getRestriction().getBase());
            }
        }

        debug("Adding sub-elements to DataClass");

        List<ElementWrapper> subElements = getSubElements();
        List<BaseAttributeWrapper> attributes = getAttributes();

        if (subElements.isEmpty() && attributes.isEmpty()) {
            warn("complexType has no content");
        }

        subElements.each {subElement -> subElement.createDataModelElement(user, parentDataModel, dataClass, schema)}
        attributes.each {attribute -> attribute.createDataModelElement(user, parentDataModel, dataClass, schema)}

        if (getExtension() != null) {
            return extendDataClass(user, parentDataModel, schema, dataClass);
        }

        return dataClass;
    }

    private ExplicitGroup buildChildGroup(SchemaWrapper schema, String type, List<DataElement> elements) {
        ExplicitGroup children;
        if (type.equalsIgnoreCase("all")) children = new All();
        else children = new ExplicitGroup();
        elements.each {de -> children.getElementsAndGroupsAndAlls().add(ElementWrapper.createElement(schema, de).wrappedElement)}
        return children;
    }

    private DataClass extendDataClass(User user, DataModel parentDataModel, SchemaWrapper schema, DataClass dataClass) {
        debug("Extends type {} collapsing elements", getExtension().getBase().getLocalPart());

        addMetadataToComponent(dataClass, XsdPlugin.METADATA_XSD_EXTENSION_BASE,
                               ComplexTypeWrapper.createComplexTypeName(getExtension().getBase().getLocalPart()), user);

        if (getSimpleContent() != null) {
            debug("Simple content extension, adding 'value' element");
            // Extending a datatype, so we add a new element "value" to hold the content
            DataType dataType = findBaseTypeDataType(schema, getExtension().getBase());
            xsdSchemaService.createDataElementForDataClass(dataClass, "value", extractDescriptionFromAnnotations(), user, dataType,
                                                           1, 1);

        } else {
            // Standard complex element extending another
            DataClass extension = findOrCreateBaseTypeDataClass(user, parentDataModel, dataClass, schema, getExtension().getBase());

            if (extension == null) {
                warn("Extends type {} but data class has not been found or created", getExtension().getBase().getLocalPart());
                return dataClass;
            }

            if (extension.getChildDataElements() != null) {
                extension.getChildDataElements().each {el -> xsdSchemaService.createDuplicateElementForDataClass(user, dataClass, el)}
            }

        }
        return dataClass;
    }

    private DataType findBaseTypeDataType(SchemaWrapper schema, QName baseType) {
        String typeName = SimpleTypeWrapper.createSimpleTypeName(baseType.getLocalPart());
        debug("Type has base type {} loading DataType into model", typeName);

        DataType dataType = schema.getDataType(typeName);
        if (dataType == null) {
            warn("element is a {} typed element but it has not been created", typeName);
        }

        return dataType;
    }

    private DataClass findOrCreateBaseTypeDataClass(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema,
                                                    QName baseType) {
        debug("Type has base type {} loading class into model", baseType.getLocalPart());
        ComplexTypeWrapper base = schema.getComplexTypeByName(baseType.getLocalPart());
        return schema.findOrCreateDataClass(user, parentDataModel, parentDataClass, base);
    }

    private List<BaseAttributeWrapper> getAttributes() {
        List<BaseAttribute> attributes = new ArrayList<>();
        attributes.addAll(getBaseAttributes());

        if (getRestriction() != null) {
            attributes.addAll(getRestriction().getBaseAttributes());
        }
        if (getExtension() != null) {
            attributes.addAll(getExtension().getBaseAttributes());
        }
        debug("Found {} attributes to be added as elements", attributes.size());
        return attributes.collect {baseAttribute -> new BaseAttributeWrapper(xsdSchemaService, baseAttribute)}
    }

    private SimpleContent getSimpleContent() {
        return getWrappedElement().getSimpleContent();
    }

    private List<ElementWrapper> getSubElements() {

        List<ElementWrapper> subElements = new ArrayList<>(getElements());

        if (getRestriction() != null) {
            subElements.addAll(getRestriction().getElements());
        }

        if (getExtension() != null) {
            subElements.addAll(getExtension().getElements());
        }

        return subElements;
    }

    private void populateFromDataClass(SchemaWrapper schema, DataClass dataClass) {
        setName(createComplexTypeName(dataClass));
        debug("Populating from {}", dataClass);
        wrappedElement.setAnnotation(createAnnotation(dataClass.getDescription()));

        if (dataClass.getChildDataElements() != null) {
            Set<DataElement> childDataElements = xsdSchemaService.getChildDataElements(dataClass);
            Map<String, List<DataElement>> grouped = childDataElements
                .sort {it.getLabel()}
                .groupBy {dataElement ->
                    Metadata md = dataElement.findMetadataByNamespaceAndKey(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_CHOICE);
                    if (md != null) return md.getValue();
                    md = dataElement.findMetadataByNamespaceAndKey(XsdPlugin.METADATA_NAMESPACE, XsdPlugin.METADATA_XSD_ALL);
                    if (md != null) return "all";
                    return "sequence";
                }

            if (grouped.isEmpty()) return;

            if (grouped.size() == 1) {
                String type = (String) grouped.keySet().toArray()[0];
                ExplicitGroup children = buildChildGroup(schema, type, grouped.get(type));
                switch (type) {
                    case "all":
                        wrappedElement.setAll((All) children);
                        break;
                    case "sequence":
                        wrappedElement.setSequence(children);
                        break;
                    default:
                        wrappedElement.setChoice(children);
                }
            } else {
                ExplicitGroup parent = new ExplicitGroup();
                grouped.each {type, elements ->
                    ExplicitGroup children = buildChildGroup(schema, type, elements);
                    String elType = type.contains("choice") ? "choice" : type;
                    JAXBElement<ExplicitGroup> jaxb = new JAXBElement<>(new QName(XS_NAMESPACE, elType), ExplicitGroup.class, children);
                    parent.getElementsAndGroupsAndAlls().add(jaxb);


                }
                wrappedElement.setSequence(parent);
            }
        }
    }

    static String createComplexTypeName(String name) {
        return standardiseTypeName(name);
    }

    static ComplexTypeWrapper createComplexType(SchemaWrapper schema, DataClass dataClass) {
        ComplexTypeWrapper wrapper = new ComplexTypeWrapper(schema.xsdSchemaService, new ComplexType());
        wrapper.populateFromDataClass(schema, dataClass);
        return wrapper;
    }

}

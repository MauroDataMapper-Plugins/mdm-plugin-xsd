package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClass;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataElement;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.DataType;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.datatype.ReferenceType;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel;
import ox.softeng.metadatacatalogue.core.facet.Metadata;
import ox.softeng.metadatacatalogue.core.user.CatalogueUser;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractComplexType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractElement;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractSimpleType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Element;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.LocalElement;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.SimpleExtensionType;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.Strings;

import java.math.BigInteger;
import javax.xml.namespace.QName;

import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_NAMESPACE;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_ALL;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_CHOICE;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_DEFAULT;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_FIXED;

/**
 * @since 24/08/2017
 */
public class ElementWrapper extends ElementBasedWrapper<AbstractElement> {

    private boolean allElement;
    private boolean choiceElement;
    private String choiceGroup;
    private Integer maxOccurs;
    private Integer minOccurs;

    ElementWrapper(XsdSchemaService xsdSchemaService, AbstractElement element) {
        this(xsdSchemaService, element, null);
    }

    private ElementWrapper(XsdSchemaService xsdSchemaService, AbstractElement wrappedElement, String name) {
        super(xsdSchemaService, wrappedElement, name);
    }

    @Override
    public SimpleExtensionType getComplexSimpleContentExtension() {
        return wrappedElement.getComplexType() != null && wrappedElement.getComplexType().getSimpleContent() != null ?
               wrappedElement.getComplexType().getSimpleContent().getExtension() : null;
    }

    @Override
    public AbstractComplexType getComplexType() {
        return wrappedElement.getComplexType();
    }

    @Override
    public Integer getMaxOccurs() {
        if (maxOccurs == null) setMaxOccurs(wrappedElement.getMaxOccurs());
        return maxOccurs;
    }

    @Override
    public Integer getMinOccurs() {
        if (minOccurs == null) setMinOccurs(wrappedElement.getMinOccurs());
        return minOccurs;
    }

    public void setMinOccurs(BigInteger minOccurs) {
        this.minOccurs = minOccurs.intValueExact();
    }

    @Override
    public QName getRef() {
        return wrappedElement.getRef();
    }

    @Override
    public AbstractSimpleType getSimpleType() {
        return wrappedElement.getSimpleType();
    }

    @Override
    public QName getType() {
        return wrappedElement.getType();
    }

    @Override
    public boolean isLocalComplexType() {
        return wrappedElement.getComplexType() != null;
    }

    @Override
    public boolean isLocalSimpleType() {
        return wrappedElement.getSimpleType() != null;
    }

    @Override
    DataElement createDataModelElement(CatalogueUser user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        DataElement element = super.createDataModelElement(user, parentDataModel, parentDataClass, schema);

        if (element == null) return null;

        trace("Adding extra attribute values as metadata");

        if (!Strings.isNullOrEmpty(wrappedElement.getDefault())) {
            addMetadataToComponent(element, METADATA_XSD_DEFAULT, wrappedElement.getDefault(), user);
        }
        if (!Strings.isNullOrEmpty(wrappedElement.getFixed())) {
            addMetadataToComponent(element, METADATA_XSD_FIXED, wrappedElement.getFixed(), user);
        }
        if (choiceElement) {
            addMetadataToComponent(element, METADATA_XSD_CHOICE, choiceGroup, user);
        } else if (allElement) {
            addMetadataToComponent(element, METADATA_XSD_ALL, "true", user);
        }
        return element;
    }

    public void setMaxOccurs(String max) {
        if (max.equals("unbounded")) this.maxOccurs = -1;
        else {
            try {
                this.maxOccurs = Integer.parseInt(max);
            } catch (NumberFormatException ignored) {
                this.maxOccurs = null;
            }
        }
    }

    @Override
    public String getName() {
        return wrappedElement.getName();
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return isLocalComplexType()
               && getComplexType().getComplexContent() != null
               && getComplexType().getComplexContent().getRestriction() != null ?
               new RestrictionWrapper(xsdSchemaService, getComplexType().getComplexContent().getRestriction()) : null;
    }

    void setAllElement() {
        allElement = true;
    }

    void setChoiceGroup(String group) {
        choiceElement = true;
        choiceGroup = group;
    }

    private void populateFromDataClass(SchemaWrapper schema, DataClass dataClass) {
        wrappedElement.setName(createValidXsdName(dataClass.getLabel()));
        debug("Populate element from {}", dataClass);
        wrappedElement.setAnnotation(createAnnotation(dataClass.getDescription()));
        ComplexTypeWrapper complexTypeWrapper = schema.findOrCreateComplexType(dataClass);
        wrappedElement.setType(new QName(complexTypeWrapper.getName()));
    }

    private void populateFromDataElement(SchemaWrapper schema, DataElement dataElement) {
        wrappedElement.setName(createValidXsdName(dataElement.getLabel()));
        debug("Populating from {}", dataElement);
        wrappedElement.setAnnotation(createAnnotation(dataElement.getDescription()));
        wrappedElement.setMinOccurs(BigInteger.valueOf(dataElement.getMinMultiplicity()));
        wrappedElement.setMaxOccurs(dataElement.getMaxMultiplicity() == -1 ? "unbounded" : dataElement.getMaxMultiplicity().toString());

        DataType dataType = dataElement.getDataType();
        if (dataType.instanceOf(ReferenceType.class)) {
            trace("Is a complexType element");
            ComplexTypeWrapper complexTypeWrapper = schema.findOrCreateComplexType(((ReferenceType) dataType).getReferenceClass());
            wrappedElement.setType(new QName(complexTypeWrapper.getName()));
        } else {
            trace("Is a simpleType element");
            SimpleTypeWrapper simpleTypeWrapper = schema.findOrCreateSimpleType(dataElement.getDataType());
            debug("Setting {} type to {}", dataElement.getDataType(), simpleTypeWrapper.getType());
            wrappedElement.setType(simpleTypeWrapper.getType());
        }

        Metadata defaultValue = dataElement.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_DEFAULT);
        if (defaultValue != null) {
            trace("Adding default value to element");
            wrappedElement.setDefault(defaultValue.getValue());
        }
        Metadata fixedValue = dataElement.findMetadataByNamespaceAndKey(METADATA_NAMESPACE, METADATA_XSD_FIXED);
        if (fixedValue != null) {
            trace("Adding fixed value to element");
            wrappedElement.setDefault(fixedValue.getValue());
        }
    }

    static ElementWrapper createElement(SchemaWrapper schema, DataElement dataElement) {

        ElementWrapper wrapper = new ElementWrapper(schema.xsdSchemaService, new LocalElement());
        wrapper.populateFromDataElement(schema, dataElement);
        return wrapper;
    }

    static ElementWrapper createElement(SchemaWrapper schema, DataClass dataClass) {

        ElementWrapper wrapper = new ElementWrapper(schema.xsdSchemaService, new Element());
        wrapper.populateFromDataClass(schema, dataClass);
        return wrapper;
    }
}

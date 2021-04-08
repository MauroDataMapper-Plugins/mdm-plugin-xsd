package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataClass;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.component.DataElement;
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel;
import ox.softeng.metadatacatalogue.core.user.CatalogueUser;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractComplexType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractSimpleType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.BaseAttribute;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.SimpleExtensionType;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.Strings;

import javax.xml.namespace.QName;

import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_LABEL_PREFIX;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_DEFAULT;
import static ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin.METADATA_XSD_FIXED;

/**
 * @since 05/09/2017
 */
public class BaseAttributeWrapper extends ElementBasedWrapper<BaseAttribute> {

    BaseAttributeWrapper(XsdSchemaService xsdSchemaService, BaseAttribute wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    @Override
    public DataElement createDataTypeElement(CatalogueUser user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        if ("prohibited".equals(wrappedElement.getUse())) {
            debug("Is prohibited therefore not creating");
            return null;
        }

        DataElement element = super.createDataTypeElement(user, parentDataModel, parentDataClass, schema);
        if (element == null) return null;

        debug("Adding extra base attribute values as metadata");

        if (!Strings.isNullOrEmpty(wrappedElement.getDefault())) {
            addMetadataToComponent(element, METADATA_XSD_DEFAULT, wrappedElement.getDefault(), user);
        }
        if (wrappedElement.getForm() != null) {
            addMetadataToComponent(element, METADATA_LABEL_PREFIX + "Form", wrappedElement.getForm().value(), user);
        }
        if (!Strings.isNullOrEmpty(wrappedElement.getFixed())) {
            addMetadataToComponent(element, METADATA_XSD_FIXED, wrappedElement.getFixed(), user);
        }
        return element;

    }

    @Override
    public SimpleExtensionType getComplexSimpleContentExtension() {
        return null;
    }

    @Override
    public AbstractComplexType getComplexType() {
        return null;
    }

    @Override
    public Integer getMaxOccurs() {
        switch (wrappedElement.getUse()) {
            case "prohibited":
                return 0;
            default:
                return 1;
        }
    }

    @Override
    public Integer getMinOccurs() {
        switch (wrappedElement.getUse()) {
            case "required":
                return 1;
            default:
                return 0;
        }
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
        return false;
    }

    @Override
    public boolean isLocalSimpleType() {
        return wrappedElement.getSimpleType() != null;
    }

    @Override
    public String getName() {
        return Strings.isNullOrEmpty(givenName) ? wrappedElement.getName() : givenName;
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return null;
    }

}

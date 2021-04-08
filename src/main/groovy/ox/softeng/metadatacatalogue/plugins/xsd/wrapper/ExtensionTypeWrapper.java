package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Annotated;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.ExplicitGroup;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.ExtensionType;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.Strings;

import java.util.List;
import javax.xml.namespace.QName;

/**
 * @since 05/09/2017
 */
public class ExtensionTypeWrapper extends ComplexContentWrapper<ExtensionType> {
    ExtensionTypeWrapper(XsdSchemaService xsdSchemaService, ExtensionType wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    @Override
    public ExplicitGroup getAll() {
        return wrappedElement.getAll();
    }

    @Override
    public List<Annotated> getAttributesAndAttributeGroups() {
        return wrappedElement.getAttributesAndAttributeGroups();
    }

    @Override
    public ExplicitGroup getChoice() {
        return wrappedElement.getChoice();
    }

    @Override
    public ExplicitGroup getSequence() {
        return wrappedElement.getSequence();
    }

    public QName getBase() {
        return wrappedElement.getBase();
    }

    @Override
    public String getName() {
        return Strings.isNullOrEmpty(givenName) ? "Extension[" + getBase().getLocalPart() + "]" : givenName;
    }

    @Override
    public RestrictionWrapper getRestriction() {
        return null;
    }
}

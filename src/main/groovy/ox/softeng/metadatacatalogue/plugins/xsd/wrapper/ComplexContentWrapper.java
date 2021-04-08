package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractElement;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Annotated;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Any;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.BaseAttribute;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.ExplicitGroup;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;

/**
 * @since 05/09/2017
 */
public abstract class ComplexContentWrapper<K extends Annotated> extends AnnotatedWrapper<K> {

    ComplexContentWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    ComplexContentWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
        super(xsdSchemaService, wrappedElement, name);
    }

    public abstract ExplicitGroup getAll();

    public abstract List<Annotated> getAttributesAndAttributeGroups();

    public abstract ExplicitGroup getChoice();

    public List<ElementWrapper> getElements() {
        if (getSequence() != null) {
            return getSubElementsOfGroup(getSequence());
        }
        if (getChoice() != null) {
            List<ElementWrapper> elements = getSubElementsOfGroup(getChoice());
            elements.forEach(e -> e.setChoiceGroup("choice"));
            return elements;
        }
        if (getAll() != null) {
            List<ElementWrapper> elements = getSubElementsOfGroup(getAll());
            elements.forEach(ElementWrapper::setAllElement);
            return elements;
        }
        if (hasBaseAttributes()) trace("complex content has attribute based content");
        else trace("complex content has no elements");
        return Collections.emptyList();
    }

    public abstract ExplicitGroup getSequence();

    List<BaseAttribute> getBaseAttributes() {
        return getAttributesAndAttributeGroups().stream()
            .filter(attr -> attr instanceof BaseAttribute)
            .map(attr -> (BaseAttribute) attr)
            .collect(Collectors.toList());
    }

    private List<ElementWrapper> getSubElementsOfGroup(ExplicitGroup group) {
        ArrayList<ElementWrapper> elements = new ArrayList<>();

        int groupMarker = 0;
        for (Object o : group.getElementsAndGroupsAndAlls()) {

            if (o instanceof JAXBElement) {
                JAXBElement el = (JAXBElement) o;
                if (el.getValue() instanceof AbstractElement) {
                    elements.add(new ElementWrapper(xsdSchemaService, (AbstractElement) el.getValue()));
                } else if (el.getValue() instanceof ExplicitGroup) {
                    List<ElementWrapper> subElements = getSubElementsOfGroup((ExplicitGroup) el.getValue());
                    int finalGroupMarker = groupMarker;
                    subElements.forEach(e -> {
                        switch (el.getName().getLocalPart()) {
                            case "choice":
                                e.setChoiceGroup("choice-" + finalGroupMarker);
                                e.setMaxOccurs(((ExplicitGroup) el.getValue()).getMaxOccurs());
                                e.setMinOccurs(((ExplicitGroup) el.getValue()).getMinOccurs());
                                break;
                            case "all":
                                e.setAllElement();
                                e.setMaxOccurs(((ExplicitGroup) el.getValue()).getMaxOccurs());
                                e.setMinOccurs(((ExplicitGroup) el.getValue()).getMinOccurs());
                                break;
                            case "sequence":
                                e.setMaxOccurs(((ExplicitGroup) el.getValue()).getMaxOccurs());
                                e.setMinOccurs(((ExplicitGroup) el.getValue()).getMinOccurs());
                                break;
                            default:
                                warn("Handling sub element group with unknown type {}", el.getName().getLocalPart());
                        }
                    });
                    groupMarker++;
                    elements.addAll(subElements);
                } else {
                    warn("complex content jaxb element with value {}", el.getValue().getClass().getSimpleName());
                }
            } else if (!(o instanceof Any)) {
                warn("complex content encountered sub element of type {}", o.getClass().getSimpleName());
            }
        }
        return elements;
    }

    private Boolean hasBaseAttributes() {
        return !getBaseAttributes().isEmpty();
    }
}

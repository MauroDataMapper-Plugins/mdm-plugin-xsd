package ox.softeng.metadatacatalogue.plugins.xsd.wrapper;

import ox.softeng.metadatacatalogue.core.catalogue.CatalogueItem;
import ox.softeng.metadatacatalogue.core.user.CatalogueUser;
import ox.softeng.metadatacatalogue.plugins.xsd.XsdPlugin;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractElement;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.AbstractSimpleType;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Annotated;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Annotation;
import ox.softeng.metadatacatalogue.plugins.xsd.org.w3.xmlschema.Facet;
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService;

import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;

import static ox.softeng.metadatacatalogue.plugins.xsd.wrapper.AnnotationContentWrapper.DOCUMENTATION_CONTENT;

/**
 * @since 25/08/2017
 */
public abstract class AnnotatedWrapper<K extends Annotated> extends OpenAttrsWrapper<K> {

    AnnotatedWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    AnnotatedWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
        super(xsdSchemaService, wrappedElement, name);
    }

    public abstract RestrictionWrapper getRestriction();

    void addRestrictionsToMetadata(CatalogueItem component, CatalogueUser user, RestrictionWrapper restriction) {
        if (restriction.getBase() != null) {
            addMetadataToComponent(component, XsdPlugin.METADATA_XSD_RESTRICTION_BASE, restriction.getBase().getLocalPart(), user);
        }

        for (Object e : restriction.findAllRestrictionsWithoutKind(RestrictionKind.enumeration)) {
            Facet f;
            RestrictionKind rk;
            // See Restriction class, they are either jaxb elements with facet values or facets.
            if (e instanceof JAXBElement) {
                JAXBElement je = (JAXBElement) e;
                f = (Facet) je.getValue();
                rk = RestrictionKind.findFromElement(je);
            } else {
                f = (Facet) e;
                rk = RestrictionKind.findFromFacet(f);
            }
            addMetadataToComponent(component, rk.displayText, f.getValue(), user);
        }
    }

    String extractDescriptionFromAnnotations() {
        return extractDescriptionFromAnnotations(DOCUMENTATION_CONTENT);
    }

    String extractDescriptionFromAnnotations(Annotated element, final String contentPreference) {
        Annotation annotation = element.getAnnotation();
        trace("Getting description from annotation for {}", getElementName(element));
        if (annotation == null) {
            return "";
        }

        List<AnnotationContentWrapper> annotations = getAnnotationContent(element);

        if (annotations.isEmpty()) {
            return "";
        }

        AnnotationContentWrapper annotationContent = annotations.get(0);

        if (annotations.size() > 1) {
            warn("XSD component {} has multiple appinfo and documentation elements, choosing first element and will prefer '{}' content",
                 getElementName(element), contentPreference);

            if (!contentPreference.equals(annotationContent.getContentType())) {
                Optional<AnnotationContentWrapper> optional = annotations.stream()
                    .filter(a -> contentPreference.equals(a.getContentType())).findFirst();
                if (optional.isPresent()) {
                    annotationContent = optional.get();
                }
            }
        }


        List<Object> contents = annotationContent.getContents().stream().filter(c -> {
            if (c instanceof String) return !Strings.isNullOrEmpty(((String) c).trim());
            return c != null;
        }).collect(Collectors.toList());

        if (contents.isEmpty()) {
            warn("XSD component {} has annotation but no content", getElementName(element));
            return "";
        }

        Object content = contents.get(0);

        if (contents.size() > 1) {
            warn("XSD component {} has multiple content types, using first content of type {}",
                 getElementName(element), content.getClass().getCanonicalName());
        }

        // return content instanceof Element ? ((Element) content).getTextContent() : content.toString();
        return getStringContent(content);
    }

    private String extractDescriptionFromAnnotations(final String contentPreference) {
        return extractDescriptionFromAnnotations(wrappedElement, contentPreference);
    }

    private List<AnnotationContentWrapper> getAnnotationContent(Annotated element) {
        return element.getAnnotation().getAppinfosAndDocumentations().stream()
            .map(AnnotationContentWrapper::new)
            .filter(c -> !c.getContents().isEmpty()) // Filter out all empty content
            .collect(Collectors.toList());
    }

    private String getElementName(Annotated element) {
        if (element instanceof Facet) return ((Facet) element).getValue();
        if (element instanceof AbstractSimpleType) return ((AbstractSimpleType) element).getName();
        if (element instanceof AbstractElement) return ((AbstractElement) element).getName();
        return element.toString();
    }

    private String getStringContent(Object content) {

        trace("Getting string content for {}", content.getClass().getCanonicalName());

        if (Node.class.isAssignableFrom(content.getClass())) {
            Node nod = (Node) content;
            if (nod.getTextContent() != null) {
                return normaliseSpace(nod.getTextContent()).trim();
            }
        }

        if (content instanceof String) {
            return normaliseSpace((String) content).trim();
        }

        return normaliseSpace(content.toString());
    }

    static void addMetadataToComponent(CatalogueItem component, String key, String value, CatalogueUser user) {
        component.addToMetadata(XsdPlugin.METADATA_NAMESPACE, key, value, user);
    }

    static String standardiseTypeName(String name) {
        String[] split = name.replaceAll(" ", "_").replaceAll("-", "_").split("_");
        List<String> standardised = new ArrayList<>();
        for (String s : split) {
            standardised.addAll(Arrays.asList(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, s).split("_")));
        }
        StringBuilder sb = new StringBuilder(standardised.get(0));
        for (int i = 1; i < standardised.size(); i++) {
            String current = standardised.get(i);
            String last = standardised.get(i - 1);
            if (!Strings.isNullOrEmpty(current)) {
                sb.append("_").append(current);
            }
        }

        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, sb.toString());
    }

    private static String normaliseSpace(String str) {
        return str.replaceAll("\\s{2,}", " ").trim();
    }
}

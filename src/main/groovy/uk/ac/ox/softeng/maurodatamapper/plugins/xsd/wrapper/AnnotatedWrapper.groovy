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

import uk.ac.ox.softeng.maurodatamapper.core.model.CatalogueItem
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.CaseFormat
import com.google.common.base.Strings
import org.w3c.dom.Node

import javax.xml.bind.JAXBElement

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

    void addRestrictionsToMetadata(CatalogueItem component, User user, RestrictionWrapper restriction) {
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
        return extractDescriptionFromAnnotations(AnnotationContentWrapper.DOCUMENTATION_CONTENT);
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
                AnnotationContentWrapper optional = annotations.find {a -> contentPreference.equals(a.getContentType())}
                if (optional) {
                    annotationContent = optional;
                }
            }
        }


        List<Object> contents = annotationContent.contents.findAll {c ->
            if (c instanceof String) return !Strings.isNullOrEmpty(((String) c).trim());
            return c != null;
        }

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

    String extractDescriptionFromAnnotations(final String contentPreference) {
        return extractDescriptionFromAnnotations(wrappedElement, contentPreference);
    }

    private List<AnnotationContentWrapper> getAnnotationContent(Annotated element) {
        return element.getAnnotation().getAppinfosAndDocumentations()
            .collect {new AnnotationContentWrapper(it)}
            .findAll {c -> !c.getContents().isEmpty()}

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

    static void addMetadataToComponent(CatalogueItem component, String key, String value, User user) {
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

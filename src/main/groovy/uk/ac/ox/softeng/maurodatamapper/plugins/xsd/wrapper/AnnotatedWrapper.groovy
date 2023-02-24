/*
 * Copyright 2020-2023 University of Oxford and NHS England
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
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Appinfo
import uk.ac.ox.softeng.maurodatamapper.security.User

import org.slf4j.helpers.MessageFormatter
import org.w3c.dom.Node

/**
 * @since 25/08/2017
 */
abstract class AnnotatedWrapper<K extends Annotated> extends OpenAttrsWrapper<K> {

    AnnotatedWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        super(xsdSchemaService, wrappedElement)
    }

    Annotation getAnnotation() {
        wrappedElement.annotation ?: new Annotation()
    }

    List<Appinfo> getAllAppInfo() {
        if (!wrappedElement.annotation) return []
        wrappedElement.annotation.appinfosAndDocumentations.findAll {it instanceof Appinfo} as List<Appinfo>
    }

    void error(String msg, Object... args) {
        super.error(msg, args)
        String fullMsg = MessageFormatter.arrayFormat(msg, buildLoggingArgs(args)).getMessage()
        wrappedElement.setAnnotation(createAppInfoAnnotation(annotation, "ERROR: ${fullMsg}".toString()))
    }

    void warn(String msg, Object... args) {
        super.warn(msg, args)
        String fullMsg = MessageFormatter.arrayFormat(msg, buildLoggingArgs(args)).getMessage()
        wrappedElement.setAnnotation(createAppInfoAnnotation(annotation, "WARNING: ${fullMsg}".toString()))
    }

    String extractDescriptionFromAnnotations() {
        extractDescriptionFromAnnotations(AnnotationContentWrapper.DOCUMENTATION_CONTENT)
    }

    String extractDescriptionFromAnnotations(String contentPreference) {
        extractDescriptionFromAnnotations(wrappedElement, contentPreference, getName())
    }

    String extractDescriptionFromAnnotations(Annotated element, final String contentPreference, String elementName) {
        Annotation annotation = element.getAnnotation()
        if (!annotation) return ''

        trace('Getting description from annotation for {}', elementName)

        List<AnnotationContentWrapper> annotations = getAnnotationContent(annotation)

        if (annotations.isEmpty()) return ''

        AnnotationContentWrapper annotationContent = annotations[0]

        if (annotations.size() > 1) {
            debug('XSD component {} has multiple appinfo and documentation elements, choosing first element and will prefer "{}" content',
                  getElementName(element), contentPreference)

            if (contentPreference != annotationContent.getContentType()) {
                AnnotationContentWrapper optional = annotations.find {a -> (contentPreference == a.getContentType())}
                if (optional) annotationContent = optional
            }
        }

        List<Object> contents = annotationContent.getContents().findAll {c -> c instanceof String ? c?.trim() : c}

        if (contents.isEmpty()) {
            warn('XSD component {} has annotation but no content', getElementName(element))
            return ''
        }

        Object content = contents[0]

        if (contents.size() > 1) {
            warn('XSD component {} has multiple content types, using first content of type {}',
                 getElementName(element), content.getClass().getCanonicalName())
        }

        getStringContent(content)
    }

    List<AnnotationContentWrapper> getAnnotationContent(Annotation annotation) {
        annotation.getAppinfosAndDocumentations()
            .collect {new AnnotationContentWrapper(it)}
            .findAll {c -> !c.getContents().isEmpty()} // Filter out all empty content
    }

    String getStringContent(Object content) {

        trace('Getting string content for {}', content.getClass().getCanonicalName())

        if (Node.isAssignableFrom(content.getClass())) {
            Node nod = (Node) content
            if (nod.getTextContent()) {
                return normaliseSpace(nod.getTextContent()).trim()
            }
        }

        if (content instanceof String) {
            return normaliseSpace(content).trim()
        }

        normaliseSpace(content.toString())
    }

    void addMetadataToComponent(CatalogueItem component, String key, String value, User user) {
        component.addToMetadata(XsdMetadata.METADATA_NAMESPACE, key, value, user.emailAddress)
    }

}

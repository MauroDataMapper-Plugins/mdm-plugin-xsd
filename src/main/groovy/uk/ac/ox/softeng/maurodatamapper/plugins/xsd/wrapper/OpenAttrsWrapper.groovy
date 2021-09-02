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
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Documentation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.OpenAttrs
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @since 24/08/2017
 */
abstract class OpenAttrsWrapper<K extends OpenAttrs> implements Comparable<OpenAttrsWrapper>, XsdNaming {

    public static final String XS_NAMESPACE = 'http://www.w3.org/2001/XMLSchema'
    public static final String XS_PREFIX = 'xs'

    K wrappedElement
    String givenName
    XsdSchemaService xsdSchemaService

    OpenAttrsWrapper(XsdSchemaService xsdSchemaService, K wrappedElement) {
        this.xsdSchemaService = xsdSchemaService
        this.wrappedElement = wrappedElement
    }

    //    OpenAttrsWrapper(XsdSchemaService xsdSchemaService, K wrappedElement, String name) {
    //        this(xsdSchemaService, wrappedElement)
    //        this.givenName = name
    //    }

    abstract String getName()

    @Override
    int compareTo(OpenAttrsWrapper that) {
        this.getName() <=> that.getName()
    }

    @Override
    String toString() {
        getName()
    }

    String getLoggingName() {
        getName()
    }

    void setName(String name) {
        givenName = name
    }

    void debug(String msg, Object... args) {
        if (getLogger().isDebugEnabled()) getLogger().debug('{} - ' + msg, buildLoggingArgs(args))
    }

    void error(String msg, Object... args) {
        getLogger().error('{} - ' + msg, buildLoggingArgs(args))
    }

    void info(String msg, Object... args) {
        getLogger().info('{} - ' + msg, buildLoggingArgs(args))
    }

    void trace(String msg, Object... args) {
        if (getLogger().isTraceEnabled()) getLogger().trace('{} - ' + msg, buildLoggingArgs(args))
    }

    void warn(String msg, Object... args) {
        getLogger().warn('{} - ' + msg, buildLoggingArgs(args))
    }

    Object[] buildLoggingArgs(Object... args) {
        Object[] arguments = new Object[args.length + 1]
        arguments[0] = getLoggingName()
        System.arraycopy(args, 0, arguments, 1, args.length)
        arguments
    }

    Logger getLogger() {
        LoggerFactory.getLogger(getClass())
    }

    //
    //    static Annotation createAnnotation(Object element) {
    //        if (!element) return null
    //        Annotation annotation = new Annotation()
    //        Documentation documentation = new Documentation()
    //        documentation.getContent().add(element)
    //        annotation.getAppinfosAndDocumentations().add(documentation)
    //
    //        annotation
    //    }
    //
    //    @SafeVarargs
    //    static Annotation createAnnotationDocumentation(Pair<String, String>... dataPairs) {
    //        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance()
    //        DocumentBuilder docBuilder = null
    //        try {
    //            docBuilder = docFactory.newDocumentBuilder()
    //        } catch (ParserConfigurationException ignored) {
    //        }
    //
    //        if (!docBuilder) return null
    //
    //        Document doc = docBuilder.newDocument()
    //
    //        Element element = doc.createElement('p')
    //        for (Pair<String, String> pair : dataPairs) {
    //            if (!Strings.isNullOrEmpty(pair.getValue())) {
    //                Text text = doc.createTextNode(pair.getKey() + ': ' + pair.getValue())
    //                element.appendChild(text)
    //                element.appendChild(doc.createElement('br'))
    //            }
    //        }
    //
    //        createAnnotation(element)
    //    }
    //
    //    static String createComplexTypeName(CatalogueItem catalogueItem) {
    //        SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(catalogueItem.getLabel()) ? catalogueItem.getLabel() :
    //        createValidTypeName(catalogueItem.getLabel() + ' Type', catalogueItem.getLastUpdated())
    //    }
    //
    //    static String createSimpleTypeName(CatalogueItem catalogueItem) {
    //        SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(catalogueItem.getLabel()) ? catalogueItem.getLabel() :
    //        createValidTypeName(catalogueItem.getLabel(), catalogueItem.getLastUpdated())
    //    }
    //
    //    static QName getTypeForName(String name) {
    //        SimpleTypeWrapper.PRIMITIVE_XML_TYPES.contains(name) ? new QName(XS_NAMESPACE, name) : new QName(name)
    //    }
    //
    //    static String createValidXsdName(String name) {
    //        String underscoreName = name.replaceAll('[ -]', '_')
    //        String camelName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, underscoreName)
    //        camelName.matches('^\\d.*') ? '_' + camelName : camelName
    //    }
    //
    //    private static String createValidTypeName(String name, OffsetDateTime lastUpdated) {
    //        String validXsdName = createValidXsdName(name)
    //        "${validXsdName}-${Math.abs(lastUpdated.hashCode())}"
    //    }
}

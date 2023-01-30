/*
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Appinfo
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Documentation
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.OpenAttrs
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.xml.namespace.QName

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

    void setName(String name, Boolean setWrappedElementName = false) {
        givenName = name
        if(setWrappedElementName) wrappedElement.setName(name)
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

    static Annotation createAnnotation(Annotation annotation, Object element) {
        if (!element) {
            if(annotation.appinfosAndDocumentations) return annotation
            return null
        }
        Documentation documentation = new Documentation()
        documentation.getContent().add(element)
        annotation.getAppinfosAndDocumentations().add(documentation)
        annotation
    }

    static Annotation createAppInfoAnnotation(Annotation annotation, Object element) {
        if (!element) {
            if(annotation.appinfosAndDocumentations) return annotation
            return null
        }
        Appinfo appinfo = new Appinfo()
        appinfo.getContent().add(element)
        annotation.getAppinfosAndDocumentations().add(appinfo)
        annotation
    }

    static QName getTypeForName(String name) {
        XsdMetadata.PRIMITIVE_XML_TYPES.contains(name) ? new QName(XS_NAMESPACE, name) : new QName(name)
    }
}

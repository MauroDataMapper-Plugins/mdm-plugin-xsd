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

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Appinfo
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Documentation

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @since 25/08/2017
 */
class AnnotationContentWrapper {

    static final String DOCUMENTATION_CONTENT = 'documentation'
    static final String APPINFO_CONTENT = 'appinfo'

    private static final Logger logger = LoggerFactory.getLogger(AnnotationContentWrapper)

    private final Object annotationContent

    AnnotationContentWrapper(Object annotationContent) {
        this.annotationContent = annotationContent
    }

    String getContentType() {
        if (annotationContent instanceof Appinfo) {
            return APPINFO_CONTENT
        }
        if (annotationContent instanceof Documentation) {
            return DOCUMENTATION_CONTENT
        }
        logger.warn('Unknown annotation content type inside wrapper {}', annotationContent.getClass().getCanonicalName())
        null
    }

    List<Object> getContents() {
        if (annotationContent instanceof Appinfo) {
            return ((Appinfo) annotationContent).getContent()
        }
        if (annotationContent instanceof Documentation) {
            return ((Documentation) annotationContent).getContent()
        }
        logger.warn('Unknown annotation content type inside wrapper {}', annotationContent.getClass().getCanonicalName())
        Collections.emptyList()
    }
}

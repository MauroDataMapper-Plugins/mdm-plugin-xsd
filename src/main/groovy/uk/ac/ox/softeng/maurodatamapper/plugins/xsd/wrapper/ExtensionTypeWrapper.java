/**
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper;

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Annotated;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExplicitGroup;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.ExtensionType;

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

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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataElement
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractComplexType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.AbstractSimpleType
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.BaseAttribute
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.SimpleExtensionType
import uk.ac.ox.softeng.maurodatamapper.security.User

import com.google.common.base.Strings

import javax.xml.namespace.QName

/**
 * @since 05/09/2017
 */
public class BaseAttributeWrapper extends ElementBasedWrapper<BaseAttribute> {

    BaseAttributeWrapper(XsdSchemaService xsdSchemaService, BaseAttribute wrappedElement) {
        super(xsdSchemaService, wrappedElement);
    }

    @Override
    public DataElement createDataTypeElement(User user, DataModel parentDataModel, DataClass parentDataClass, SchemaWrapper schema) {
        if ("prohibited".equals(wrappedElement.getUse())) {
            debug("Is prohibited therefore not creating");
            return null;
        }

        DataElement element = super.createDataTypeElement(user, parentDataModel, parentDataClass, schema);
        if (element == null) return null;

        debug("Adding extra base attribute values as metadata");

        if (!Strings.isNullOrEmpty(wrappedElement.getDefault())) {
            addMetadataToComponent(element, XsdPlugin.METADATA_XSD_DEFAULT, wrappedElement.getDefault(), user);
        }
        if (wrappedElement.getForm() != null) {
            addMetadataToComponent(element, XsdPlugin.METADATA_LABEL_PREFIX + "Form", wrappedElement.getForm().value(), user);
        }
        if (!Strings.isNullOrEmpty(wrappedElement.getFixed())) {
            addMetadataToComponent(element, XsdPlugin.METADATA_XSD_FIXED, wrappedElement.getFixed(), user);
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

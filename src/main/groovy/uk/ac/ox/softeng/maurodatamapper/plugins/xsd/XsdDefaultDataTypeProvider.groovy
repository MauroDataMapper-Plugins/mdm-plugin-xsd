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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.SimpleTypeWrapper.PRIMITIVE_XML_TYPES

import static XsdPlugin.METADATA_NAMESPACE
import static XsdPlugin.METADATA_XSD_TARGET_NAMESPACE
import static XsdPlugin.METADATA_XSD_TARGET_NAMESPACE_PREFIX

/**
 * @since 19/04/2018
 */
class XsdDefaultDataTypeProvider implements DefaultDataTypeProvider {

    @Override
    List<DataType> getDefaultListOfDataTypes() {
        PRIMITIVE_XML_TYPES.collect {t ->
            new PrimitiveType(label: t, description: "XML primitive type: xs:$t")
                .addToMetadata(namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE, value: uk.ac.ox.softeng.maurodatamapper.plugins.xsd.
                    wrapper.OpenAttrsWrapper.XS_NAMESPACE)
                .addToMetadata(namespace: METADATA_NAMESPACE, key: METADATA_XSD_TARGET_NAMESPACE_PREFIX, value: uk.ac.ox.softeng.maurodatamapper.
                    plugins.xsd.wrapper.OpenAttrsWrapper.XS_PREFIX)
        }
    }

    @Override
    String getDisplayName() {
        'XSD DataTypes'
    }

    @Override
    String getVersion() {
        return null
    }

    @Override
    int getOrder() {
        -1
    }
}

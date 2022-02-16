/*
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider

import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.PrimitiveType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.DefaultDataTypeProvider
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.OpenAttrsWrapper

/**
 * @since 19/04/2018
 */
class XsdDefaultDataTypeProvider implements DefaultDataTypeProvider {

    @Override
    List<DataType> getDefaultListOfDataTypes() {
        XsdMetadata.PRIMITIVE_XML_TYPES.collect {t ->
            new PrimitiveType(label: t, description: "XML primitive type: xs:$t")
                .addToMetadata(namespace: XsdMetadata.METADATA_NAMESPACE, key: XsdMetadata.METADATA_XSD_TARGET_NAMESPACE, value: OpenAttrsWrapper.XS_NAMESPACE)
                .addToMetadata(namespace: XsdMetadata.METADATA_NAMESPACE, key: XsdMetadata.METADATA_XSD_TARGET_NAMESPACE_PREFIX, value: OpenAttrsWrapper.XS_PREFIX)
        }
    }

    @Override
    String getDisplayName() {
        'XSD DataTypes'
    }

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    int getOrder() {
        -1
    }
}

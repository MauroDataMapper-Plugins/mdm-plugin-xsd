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


import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLinkService
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.security.User

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.BiFunction

/**
 * @since 04/09/2017
 */
class DataStore {

    private static final Logger logger = LoggerFactory.getLogger(DataStore)

    private final User user
    private final Map<String, DataClass> dataClasses
    private final Map<String, DataType> dataTypes
    private final List<String> underConstruction = []
    private final SemanticLinkService semanticLinkService
    List<Long> dataElementCreations = []
    List<Long> dataClassCreations = []
    List<Long> dataTypeCreations = []

    DataStore(User user, SemanticLinkService semanticLinkService, int dataTypeSize, int dataClassSize) {
        this.user = user
        this.semanticLinkService = semanticLinkService
        dataClasses = new HashMap<>(dataTypeSize)
        dataTypes = new HashMap<>(dataClassSize)
    }

    boolean addToConstruction(String s) {
        !underConstruction.contains(s) && underConstruction.add(s)
    }

    @Deprecated
    DataType addDataType(String key, BiFunction<String, DataType, DataType> mappingFunction) {
        dataTypes.compute(key, mappingFunction)
    }

    DataClass getDataClass(String key) {
        dataClasses[key]
    }

    DataType getDataType(String key) {
        dataTypes[key]
    }

    void putDataClass(String key, DataClass value) {
        dataClasses[key] = value
    }

    DataType putDataType(String key, DataType value) {
        dataTypes[key] = value
    }

    void removeFromConstruction(String s) {
        String last = underConstruction.last()
        if (s != last) logger.warn('Trying to remove {} from under construction but it isnt the most recent item, that is {}', s, last)
        underConstruction.remove(s)
    }
}

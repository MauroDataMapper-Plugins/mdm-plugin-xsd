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

import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLink
import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLinkService
import uk.ac.ox.softeng.maurodatamapper.core.facet.SemanticLinkType
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.DataClass
import uk.ac.ox.softeng.maurodatamapper.datamodel.item.datatype.DataType
import uk.ac.ox.softeng.maurodatamapper.security.User

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.function.Function

/**
 * @since 04/09/2017
 */
class DataStore {

    private static final Logger logger = LoggerFactory.getLogger(DataStore.class);
    private final boolean createLinksRatherThanReferences;
    private final User user;
    private Map<String, Set<DataClass>> allDataClasses;
    private Map<String, DataClass> dataClasses;
    private Map<String, DataType> dataTypes;
    private SemanticLinkService semanticLinkService;
    private List<String> underConstruction;

    DataStore(User user, SemanticLinkService semanticLinkService, boolean createLinksRatherThanReferences) {
        dataClasses = new HashMap<>();
        allDataClasses = new HashMap<>();
        dataTypes = new HashMap<>();
        underConstruction = new ArrayList<>();
        this.user = user;
        this.createLinksRatherThanReferences = createLinksRatherThanReferences;
        this.semanticLinkService = semanticLinkService;
    }

    boolean addToConstruction(String s) {
        return !underConstruction.contains(s) && underConstruction.add(s);
    }

    DataType computeIfDataTypeAbsent(String key,
                                     Function<? super String, ? extends DataType> mappingFunction) {
        return dataTypes.computeIfAbsent(key, mappingFunction);
    }

    DataClass getDataClass(String key) {
        return dataClasses.get(key);
    }

    DataType getDataType(String key) {
        return dataTypes.get(key);
    }

    boolean isCreateLinksRatherThanReferences() {
        return createLinksRatherThanReferences;
    }

    void putAllDataTypes(Map<? extends String, ? extends DataType> m) {
        dataTypes.putAll(m);
    }

    void putDataClass(String key, DataClass value) {
        Set<DataClass> existing = allDataClasses[key] ?: [] as Set


        if (createLinksRatherThanReferences && !existing.isEmpty()) {
            logger.debug("Adding links for {} existing data classes with type {}", existing.size(), key);
            existing.each {dc ->

                SemanticLink linkTarget = semanticLinkService.createSemanticLink(user, value, dc, SemanticLinkType.REFINES);
                SemanticLink linkSource = semanticLinkService.createSemanticLink(user, dc, value, SemanticLinkType.REFINES);

                dc.addTo("sourceForLinks", linkSource);
                value.addTo("targetForLinks", linkSource);

                dc.addTo("targetForLinks", linkTarget);
                value.addTo("sourceForLinks", linkTarget);
            }
        }

        existing.add(value);
        allDataClasses[key] = existing
        dataClasses[key] = value;
    }

    DataType putDataType(String key, DataType value) {
        return dataTypes.put(key, value);
    }

    void removeFromConstruction(String s) {
        String last = underConstruction.get(underConstruction.size() - 1);
        if (!s.equals(last)) logger.warn("Trying to remove {} from under construction but it isnt the most recent item, that is {}", s, last);
        underConstruction.remove(s);
    }
}

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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.profile

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.profile.provider.JsonProfileProviderService

/**
 * @since 15/02/2022
 */
class XsdDataModelProfileProviderService extends JsonProfileProviderService{

    @Override
    String getJsonResourceFile() {
        'dataModelProfile.json'
    }

    @Override
    String getMetadataNamespace() {
        XsdMetadata.METADATA_NAMESPACE
    }

    @Override
    String getDisplayName() {
        'XSD DataModel Profile'
    }

    @Override
    List<String> profileApplicableForDomains() {
        return ['DataModel']
    }
}

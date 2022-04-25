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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter

import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.FileParameter
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportGroupConfig
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.config.ImportParameterConfig
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelFileImporterProviderServiceParameters

/**
 * Created by james on 31/05/2017.
 */
class XsdImporterProviderServiceParameters extends DataModelFileImporterProviderServiceParameters {

    //TODO scrap the public private, what are these roots
    @ImportParameterConfig(
        displayName = "Root Element to Import",
        description = "If XSD defines multiple elements, and you only want to import one",
        optional = true,
        order = 1,
        group = @ImportGroupConfig(
            name = "Source",
            order = 1
        )
    )
     String rootElement

    @ImportParameterConfig(
        displayName = 'File',
        description = 'The file containing the data to be imported. Please be aware archived imports may take a long period of time',
        order = -1,
        group = @ImportGroupConfig(
            name = 'Source',
            order = -1
        )
    )
    FileParameter importFile


    @ImportParameterConfig(
        displayName = "Primary File in Zip Folder",
        description = "Select primary file within archive",
        optional = true,
        order = 2,
        group = @ImportGroupConfig(
            name = "Source",
            order = 1
        )
    )
     String zipFolderLocation
}

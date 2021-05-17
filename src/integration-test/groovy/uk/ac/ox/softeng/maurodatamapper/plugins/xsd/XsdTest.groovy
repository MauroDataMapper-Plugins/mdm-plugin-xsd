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

import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
//import uk.ac.ox.softeng.maurodatamapper.core.spi.importer.parameter.FileParameter
//import uk.ac.ox.softeng.maurodatamapper.plugins.test.BaseImportPluginTest
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.XsdImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.fail

/**
 * @since 11/09/2017
 */
abstract class XsdTest {

}

/*extends BaseImportPluginTest<DataModel, XsdImporterProviderServiceParameters, XsdImporterProviderService> {

    @Override
    DataModel saveDomain(DataModel domain) {
        getBean(DataModelService).saveWithBatching(domain)
    }

    XsdImporterProviderServiceParameters createImportParameters(String filename, String modelName) throws IOException {
        XsdImporterProviderServiceParameters params = new XsdImporterProviderServiceParameters()
        params.setAuthor("Test Author")
        params.setOrganisation("Test Org")
        params.setDescription("Test description")
        params.setFinalised(true)
        params.setDataModelName(modelName)
        params.setFolderId(testFolder.id)

        Path p = Paths.get("src/integration-test/resources/" + filename)
        if (!Files.exists(p)) {
            fail("File " + filename + " cannot be found")
        }

        FileParameter file = new FileParameter(p.toString(), "application/xml", Files.readAllBytes(p));
        params.setImportFile(file)
        return params
    }

    DataModel importDataModelAndRetrieveFromDatabase(XsdImporterProviderServiceParameters params) {
        DataModel importedModel = importDomain(params)

        getLogger().debug("Getting datamodel {} from database to verify", importedModel.getId())
        // Rather than use the one returned from the import, we want to check whats actually been saved into the DB
        DataModel dataModel = DataModel.get(importedModel.getId())
        assertNotNull("DataModel should exist in Database", dataModel)
        return dataModel
    }
}
 */
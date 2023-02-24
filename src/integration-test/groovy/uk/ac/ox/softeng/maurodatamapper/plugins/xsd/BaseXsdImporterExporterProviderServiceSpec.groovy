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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiException
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.provider.importer.parameter.FileParameter
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelService
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.DataModelJsonImporterService
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.parameter.DataModelFileImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.exporter.XsdExporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.XsdImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator
import uk.ac.ox.softeng.maurodatamapper.test.integration.BaseIntegrationSpec
import uk.ac.ox.softeng.maurodatamapper.test.xml.XmlComparer
import uk.ac.ox.softeng.maurodatamapper.test.xml.evalutator.IgnoreOrderDifferenceEvaluator

import grails.gorm.transactions.Transactional
import grails.testing.spock.RunOnce
import grails.util.BuildSettings
import groovy.util.logging.Slf4j
import org.xmlunit.diff.DifferenceEvaluator
import org.xmlunit.diff.DifferenceEvaluators
import spock.lang.Shared

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.fail

@Slf4j
@Transactional
abstract class BaseXsdImporterExporterProviderServiceSpec extends BaseIntegrationSpec implements XmlComparer{


    DataModel dataModel
    Folder folder
    DataModelJsonImporterService dataModelJsonImporterService
    DataModelService dataModelService


    abstract XsdExporterProviderService getXsdExporterProviderService()
    abstract XsdImporterProviderService getXsdImporterProviderService()


    @Shared
    Path resourcesPath

    @Shared
    DataModelFileImporterProviderServiceParameters basicParameters


    @RunOnce
    def setup() {
        resourcesPath = Paths.get(BuildSettings.BASE_DIR.absolutePath, 'src', 'integration-test', 'resources')
    }


    @Override
    void setupDomainData() {
        folder = new Folder(label: 'xsdTestFolder', createdBy: reader1.emailAddress)
        checkAndSave(folder)
   }

    Tuple2<String,String> testExport(UUID dataModelId, String expectedFilename, String outFileName) throws IOException, ApiException {
        log.info('------------ Exporting -------------')
        ByteArrayOutputStream byteArrayOutputStream = xsdExporterProviderService.exportDomain(reader1, dataModelId)

        String exportedXsd = byteArrayOutputStream.toString(Charset.defaultCharset())

        Path p = Paths.get('build/tmp/', outFileName)
        Files.write(p, exportedXsd.getBytes(Charset.defaultCharset()))

        Path expPath = Paths.get('src/integration-test/resources/expected/' + expectedFilename)
        String expected = new String(Files.readAllBytes(expPath), Charset.defaultCharset())
        Tuple.tuple(fudgeDates(expected), fudgeDates(exportedXsd))
    }

    byte[] loadTestFile(String filename, String filetype) {
        Path testFilePath = resourcesPath.resolve("${filename}.${filetype}").toAbsolutePath()
        assert Files.exists(testFilePath)
        Files.readAllBytes(testFilePath)
    }

    DataModel importModel(byte[] bytes) {
        basicParameters = new XsdImporterProviderServiceParameters().tap {
            importAsNewBranchModelVersion = false
            importAsNewDocumentationVersion = false
            finalised = false
        }

        log.trace('Importing:\n {}', new String(bytes))
        basicParameters.importFile = new FileParameter(fileContents: bytes)
        importModel(basicParameters)
    }

    DataModel importModel(XsdImporterProviderServiceParameters params){
        DataModel imported = getXsdImporterProviderService().importDomain(admin, params) as DataModel
        imported.folder = folder
        imported
    }

    DataModel importJsonModel(byte[] bytes) {
        basicParameters = new DataModelFileImporterProviderServiceParameters().tap {
            importAsNewBranchModelVersion = false
            importAsNewDocumentationVersion = false
            finalised = false
        }

        log.trace('Importing:\n {}', new String(bytes))
        basicParameters.importFile = new FileParameter(fileContents: bytes)
        importJsonModel(basicParameters)
    }

    DataModel importJsonModel(DataModelFileImporterProviderServiceParameters params){
        DataModel imported = dataModelJsonImporterService.importDomain(admin, params) as DataModel
        imported.folder = folder
        imported
    }

    String failureReason

    DifferenceEvaluator getDifferenceEvaluator() {
        return DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            new IgnoreOrderDifferenceEvaluator(),
            new IgnoreNameAttributeDifferenceEvaluator()
        )
    }

    String fudgeDates(String s) {
        return s
            .replaceAll('Last Updated:.+?<br\\s*/>', 'Last Updated:<br/>')
            .replaceAll(/-\d+,/, '-xxxxxxxx,')
    }

    XsdImporterProviderServiceParameters createImportParameters(String filename, String modelName) throws IOException {
        XsdImporterProviderServiceParameters params = new XsdImporterProviderServiceParameters()
        params.setModelName(modelName)
        params.setFolderId(folder.id)
        params.importAsNewBranchModelVersion = false
        params.importAsNewDocumentationVersion = false
        params.finalised = false

        Path p = Paths.get('src/integration-test/resources/' + filename)
        if (!Files.exists(p)) {
            fail('File ' + filename + ' cannot be found')
        }

        FileParameter file = new FileParameter(p.toString(), 'application/xml', Files.readAllBytes(p))
        params.setImportFile(file)
        params
    }

    XsdImporterProviderServiceParameters createImportParametersForZip(String filename, String zipLocation, String modelName) throws IOException {
        XsdImporterProviderServiceParameters params = new XsdImporterProviderServiceParameters()
        params.setModelName(modelName)
        params.setFolderId(folder.id)
        params.importAsNewBranchModelVersion = false
        params.importAsNewDocumentationVersion = false
        params.finalised = false
        params.zipFolderLocation = filename

        Path p = Paths.get(zipLocation)
        if (!Files.exists(p)) {
            fail('File ' + zipLocation + ' cannot be found')
        }

        FileParameter file = new FileParameter(p.toString(), 'application/zip', Files.readAllBytes(p))
        params.setImportFile(file)
        params
    }


    DataModel importDataModelAndRetrieveFromDatabase(XsdImporterProviderServiceParameters params) {
        DataModel importedModel = importModel(params)
        importedModel.setCreatedBy(admin.emailAddress)
        dataModelService.validate(importedModel)
        dataModelService.saveModelWithContent(importedModel)

        log.debug('Getting datamodel {} from database to verify', importedModel.getId())
        // Rather than use the one returned from the import, we want to check whats actually been saved into the DB
        DataModel dataModel = DataModel.get(importedModel.getId())

        assertNotNull('DataModel should exist in Database', dataModel)
        dataModel
    }
}

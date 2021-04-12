/*
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer


import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.xsd.XsdPlugin

/**
 * Created by james on 31/05/2017.
 */
class XsdImporterProviderService extends XsdPlugin {
}

/* DataModelImporterProviderService<DataModelImporterProviderServiceParameters>{
// implements DataModelImporterPlugin<XsdImportParameters> {
    //TODO unsure of this implementation

    //TODO move into grails-app/services folder

    @Autowired
    XsdSchemaService xsdSchemaService

    @Autowired
    DataModelService dataModelService

    @Autowired
    FolderService folderService

    @Override
    String getVersion() {
        getClass().getPackage().getSpecificationVersion() ?: 'SNAPSHOT'
    }

    @Override
    String getDisplayName() {
        'XML Schema (XSD) Importer'
    }

    @Override
    Boolean canImportMultipleDomains() {
        false
    }

    @Override
    DataModel importDataModel(User currentUser, XsdDataModelImporterProviderServiceParameters params) {
        logger.info('Loading XSD model from {}', params.getImportFile().getFileName())
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(params.getImportFile().getFileContents())

        Path path = Paths.get(params.getImportFile().getFileName())
        String parent = path.getParent() == null ? '' : path.getParent().toString()
        String dataModelLabel = params.getDataModelName() ?: path.getFileName().toString()

        importDataModel(currentUser, byteArrayInputStream, path.getFileName().toString(), parent, params.getRootElement(),
                        dataModelLabel, params.description, params.author, params.organisation, folderService.get(params.folderId),
                        params.finalised)
    }

    @Override
    List<DataModel> importDataModels(User currentUser, XsdDataModelImporterProviderServiceParameters params) {
        throw new ApiBadRequestException('XIS02', "${getName()} cannot import multiple DataModels")
    }

    DataModel importDataModel(User currentUser, InputStream byteArrayInputStream, String filename, String directory,
                              String rootElement, String label, String description, String author, String organisation, Folder folder,
                              boolean finalised, boolean createLinksRatherThanReferences = false) {
        try {
            DataModel dataModel = dataModelService.createDataModel(currentUser, label, description, author, organisation, folder)
            dataModel.finalised = finalised
            SchemaWrapper schema = SchemaWrapper.loadSchemaFromInputStream(xsdSchemaService, byteArrayInputStream, filename, directory)
            log.debug('Creating model')
            dataModel = schema.loadIntoDataModel(dataModel, currentUser, rootElement, createLinksRatherThanReferences)
            log.info('Model loaded from XSD')
            dataModel

        } catch (JAXBException | FileNotFoundException e) {
            log.error('Could not load XSD model from ' + filename, e)
            throw new ApiBadRequestException('XIS01', 'Cannot read schema', e)
        }
    }
}
*/
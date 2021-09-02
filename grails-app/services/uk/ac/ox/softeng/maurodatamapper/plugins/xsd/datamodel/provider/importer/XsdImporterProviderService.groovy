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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer

import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiBadRequestException
import uk.ac.ox.softeng.maurodatamapper.core.authority.AuthorityService
import uk.ac.ox.softeng.maurodatamapper.core.container.Folder
import uk.ac.ox.softeng.maurodatamapper.core.container.FolderService
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModel
import uk.ac.ox.softeng.maurodatamapper.datamodel.DataModelType
import uk.ac.ox.softeng.maurodatamapper.datamodel.provider.importer.DataModelImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdSchemaService
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.parameter.XsdImporterProviderServiceParameters
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.SchemaWrapper
import uk.ac.ox.softeng.maurodatamapper.security.User

import groovy.util.logging.Slf4j

import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.bind.JAXBException

/**
 * Created by james on 31/05/2017.
 */
@Slf4j
class XsdImporterProviderService extends DataModelImporterProviderService<XsdImporterProviderServiceParameters> {

    XsdSchemaService xsdSchemaService

    FolderService folderService

    AuthorityService authorityService

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
    List<DataModel> importModels(User currentUser, XsdImporterProviderServiceParameters params) {
        throw new ApiBadRequestException('XIS02', "${getName()} cannot import multiple DataModels")
    }

    @Override
    DataModel importModel(User currentUser, XsdImporterProviderServiceParameters params) {
        log.info('Loading XSD model from {}', params.getImportFile().getFileName())
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(params.getImportFile().getFileContents())

                Path path = Paths.get(params.getImportFile().getFileName())
                String parent = path.getParent() == null ? '' : path.getParent().toString()
                String dataModelLabel = params.getModelName() ?: path.getFileName().toString()

                importDataModel(currentUser, byteArrayInputStream, path.getFileName().toString(), parent, params.getRootElement(),
                                dataModelLabel, params.description, params.author, params.organisation, folderService.get(params.folderId))
    }

    DataModel importDataModel(User currentUser, InputStream byteArrayInputStream, String filename, String directory,
                              String rootElement, String label, String description, String author, String organisation, Folder folder) {
        try {
            DataModel dataModel =  new DataModel(author: author, organisation: organisation, type: DataModelType.DATA_STANDARD, folder: folder,
                                                 authority: authorityService.defaultAuthority, label: label, description: description)
            SchemaWrapper schema = SchemaWrapper.createSchemaWrapperFromInputStream(xsdSchemaService, byteArrayInputStream, filename, directory)
            log.debug('Creating model')
            dataModel = schema.loadIntoDataModel(dataModel, currentUser, rootElement)
            log.info('Model loaded from XSD')
            dataModel

        } catch (JAXBException | FileNotFoundException e) {
            log.error('Could not load XSD model from ' + filename, e)
            throw new ApiBadRequestException('XIS01', 'Cannot read schema', e)
        }
    }
}
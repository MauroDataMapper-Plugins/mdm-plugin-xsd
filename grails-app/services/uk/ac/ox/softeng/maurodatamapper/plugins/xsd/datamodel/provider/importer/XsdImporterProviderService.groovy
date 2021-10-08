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
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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


        String fileType = params.getImportFile().getFileType()

        if(fileType == "application/rar" || fileType == "application/zip"){
            log.info('Loading Zip File')
            File tempDir = Files.createTempDirectory("temp").toFile()

            log.info('Temp Folder Location {}', tempDir.getAbsolutePath())

            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(params.getImportFile().getFileContents()))
            ZipEntry zipEntry = zis.getNextEntry()
            while(zipEntry != null)
            {
                File newFile = newFile(tempDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            Path primaryFile = Paths.get(tempDir.toString(), params.zipFolderLocation)
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Files.readAllBytes(primaryFile))

            String dataModelLabel = params.getModelName() ?: primaryFile.getFileName().toString()
           DataModel dm =  importDataModel(currentUser, byteArrayInputStream, primaryFile.getFileName().toString(), tempDir.toString(), params.getRootElement(),
                            dataModelLabel, params.description, params.author, params.organisation, folderService.get(params.folderId))


            //Deleting contents of folder after import
            tempDir.deleteDir()

            dm

        }
        else{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(params.getImportFile().getFileContents())
            Path path = Paths.get(params.getImportFile().getFileName())
            String parent = path.getParent() == null ? '' : path.getParent().toString()
            String dataModelLabel = params.getModelName() ?: path.getFileName().toString()

            importDataModel(currentUser, byteArrayInputStream, path.getFileName().toString(), parent, params.getRootElement(),
                            dataModelLabel, params.description, params.author, params.organisation, folderService.get(params.folderId))
        }


    }

    DataModel importDataModel(User currentUser, InputStream byteArrayInputStream, String filename, String directory,
                              String rootElement, String label, String description, String author, String organisation, Folder folder) {
        try {
            DataModel dataModel =  new DataModel(author: author, organisation: organisation, type: DataModelType.DATA_STANDARD, folder: folder,
                                                 authority: authorityService.defaultAuthority, label: label, description: description, createdBy: currentUser.emailAddress)
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

    File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


}
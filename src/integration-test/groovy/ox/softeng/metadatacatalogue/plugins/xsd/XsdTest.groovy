package ox.softeng.metadatacatalogue.plugins.xsd

import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModelService
import ox.softeng.metadatacatalogue.core.spi.importer.parameter.FileParameter
import ox.softeng.metadatacatalogue.plugins.test.BaseImportPluginTest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.fail

/**
 * @since 11/09/2017
 */
abstract class XsdTest extends BaseImportPluginTest<DataModel, XsdImportParameters, XsdImporterService> {

    @Override
    DataModel saveDomain(DataModel domain) {
        getBean(DataModelService).saveWithBatching(domain)
    }

    XsdImportParameters createImportParameters(String filename, String modelName) throws IOException {
        XsdImportParameters params = new XsdImportParameters();
        params.setAuthor("Test Author");
        params.setOrganisation("Test Org");
        params.setDescription("Test description");
        params.setFinalised(true);
        params.setDataModelName(modelName);
        params.setFolderId(testFolder.id)

        Path p = Paths.get("src/integration-test/resources/" + filename);
        if (!Files.exists(p)) {
            fail("File " + filename + " cannot be found");
        }

        FileParameter file = new FileParameter(p.toString(), "application/xml", Files.readAllBytes(p));
        params.setImportFile(file);
        return params;
    }

    DataModel importDataModelAndRetrieveFromDatabase(XsdImportParameters params) {
        DataModel importedModel = importDomain(params);

        getLogger().debug("Getting datamodel {} from database to verify", importedModel.getId());
        // Rather than use the one returned from the import, we want to check whats actually been saved into the DB
        DataModel dataModel = DataModel.get(importedModel.getId());
        assertNotNull("DataModel should exist in Database", dataModel);
        return dataModel;
    }
}

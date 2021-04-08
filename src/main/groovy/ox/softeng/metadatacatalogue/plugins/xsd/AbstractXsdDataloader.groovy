package ox.softeng.metadatacatalogue.plugins.xsd

import ox.softeng.metadatacatalogue.core.api.exception.ApiException
import ox.softeng.metadatacatalogue.core.api.exception.ApiInternalException
import ox.softeng.metadatacatalogue.core.catalogue.linkable.datamodel.DataModel
import ox.softeng.metadatacatalogue.core.spi.dataloader.DataModelDataLoaderPlugin
import ox.softeng.metadatacatalogue.core.user.CatalogueUser
import ox.softeng.metadatacatalogue.core.util.Utils

import org.springframework.beans.factory.annotation.Autowired

import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.bind.JAXBException

/**
 * @since 04/09/2017
 */
abstract class AbstractXsdDataloader implements DataModelDataLoaderPlugin {

    @Autowired
    XsdImporterService xsdImporterService

    abstract boolean createLinksOverReferences()

    abstract String[] getFilenames()

    // Override this to import only 1 element from the XSDs
    String getRootElement() {
        null
    }

    String getDataModelName(String filename) {
        filename.replaceFirst('\\.xsd', '')
    }

    @Override
    List<DataModel> importData(CatalogueUser catalogueUser) throws ApiException {
        long start = System.currentTimeMillis()
        List<DataModel> loadedModels = []
        for (String filename : getFilenames()) {

            Path path = Paths.get(filename)

            getLogger().info('Loading {} with directory {}', path.getFileName(), path.getParent())
            InputStream is = getClass().getClassLoader().getResourceAsStream(filename)

            if (is == null) {
                getLogger().warn('Could not load {} as it cannot be found', filename)
                return Collections.emptyList()
            }

            try {
                String parent = path.getParent() != null ? path.getParent().toString() : ''

                DataModel dataModel = xsdImporterService.importDataModel(catalogueUser, is, path.getFileName().toString(), parent,
                                                                         getRootElement(), getDataModelName(path.getFileName().toString()),
                                                                         getDescription(), getAuthor(), getOrganisation(), getFolder(),
                                                                         true, createLinksOverReferences())

                loadedModels.add(dataModel)
                getLogger().info('{} loaded', filename)
            } catch (JAXBException | FileNotFoundException e) {
                throw new ApiInternalException('AXD01', 'Cannot read schema', e)
            }
        }

        getLogger().info('{} XSD models imported. Took {}', loadedModels.size(), Utils.getTimeString(System.currentTimeMillis() - start))
        loadedModels
    }
}
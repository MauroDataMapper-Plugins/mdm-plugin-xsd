package ox.softeng.metadatacatalogue.plugins.xsd;

import ox.softeng.metadatacatalogue.core.spi.importer.parameter.DataModelAdditionalImporterPluginParameters;
import ox.softeng.metadatacatalogue.core.spi.importer.parameter.config.ImportGroupConfig;
import ox.softeng.metadatacatalogue.core.spi.importer.parameter.config.ImportParameterConfig;

/**
 * Created by james on 31/05/2017.
 */
public class XsdImportParameters extends DataModelAdditionalImporterPluginParameters {

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
    private String rootElement;

    public String getRootElement() {
        return rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }
}

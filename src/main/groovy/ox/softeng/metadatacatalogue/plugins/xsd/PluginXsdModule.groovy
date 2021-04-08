package ox.softeng.metadatacatalogue.plugins.xsd

import ox.softeng.metadatacatalogue.core.spi.module.AbstractModule
import ox.softeng.metadatacatalogue.plugins.xsd.spi.XsdSchemaService

/**
 * @since 17/08/2017
 */
class PluginXsdModule extends AbstractModule {
    @Override
    String getName() {
        return "Plugin - XSD"
    }

    //    @Override
    Closure doWithSpring() {
        {->
            xsdSchemaService(XsdSchemaService)
            xsdExporterService(XsdExporterService)
            xsdImporterService(XsdImporterService)
            xsdDefaultDataTypeProvider(XsdDefaultDataTypeProvider)

        }
    }

}

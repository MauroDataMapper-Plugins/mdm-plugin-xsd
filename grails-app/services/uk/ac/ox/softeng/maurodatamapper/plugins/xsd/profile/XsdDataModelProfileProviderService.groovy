package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.profile

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.profile.provider.JsonProfileProviderService

/**
 * @since 15/02/2022
 */
class XsdDataModelProfileProviderService extends JsonProfileProviderService{

    @Override
    String getJsonResourceFile() {
        'dataModelProfile.json'
    }

    @Override
    String getMetadataNamespace() {
        XsdMetadata.METADATA_NAMESPACE
    }

    @Override
    String getDisplayName() {
        'XSD DataModel Profile'
    }

    @Override
    List<String> profileApplicableForDomains() {
        return ['DataModel']
    }
}

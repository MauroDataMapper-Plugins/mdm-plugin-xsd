package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.profile

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdMetadata
import uk.ac.ox.softeng.maurodatamapper.profile.provider.JsonProfileProviderService

/**
 * @since 15/02/2022
 */
class XsdDataClassProfileProviderService extends JsonProfileProviderService{

    @Override
    String getJsonResourceFile() {
        'dataClassProfile.json'
    }

    @Override
    String getMetadataNamespace() {
        XsdMetadata.METADATA_NAMESPACE
    }

    @Override
    String getDisplayName() {
        'XSD DataClass Profile'
    }

    @Override
    List<String> profileApplicableForDomains() {
        return ['DataClass']
    }
}

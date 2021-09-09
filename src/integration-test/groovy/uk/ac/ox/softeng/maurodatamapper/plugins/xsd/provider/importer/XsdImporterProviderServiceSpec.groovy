package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.provider.importer

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.util.logging.Slf4j
import uk.ac.ox.softeng.maurodatamapper.api.exception.ApiBadRequestException
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.datamodel.provider.importer.XsdImporterProviderService
import uk.ac.ox.softeng.maurodatamapper.test.integration.BaseIntegrationSpec

@Integration
@Rollback
@Slf4j
class XsdImporterServiceSpec extends  BaseIntegrationSpec {

    XsdImporterProviderService xsdImporterProviderService

    @Override
    void setupDomainData() {

    }


}
package ox.softeng.metadatacatalogue.plugins.xsd.utils


import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming

/**
 * @since 11/10/2018
 */
class XsdNamingTest implements XsdNaming {

    Logger logger = LoggerFactory.getLogger(XsdNamingTest)

    final Map<String, String> testNames = [
        CamelCase                        : 'camelCase',
        CamelCASE                        : 'camelCase',
        camel_case                       : 'camelCase',
        CAMEL_CASE                       : 'camelCase',
        CAMEL_case                       : 'camelCase',
        camel_CASE                       : 'camelCase',
        element                          : 'element',
        elementA                         : 'elementA',
        'element-A'                      : 'elementA',
        element_A                        : 'elementA',
        'element-a'                      : 'elementA',
        element_a                        : 'elementA',
        'element A'                      : 'elementA',
        'element a'                      : 'elementA',
        anotherElement                   : 'anotherElement',
        'another Element'                : 'anotherElement',
        'another element'                : 'anotherElement',
        'another_Element'                : 'anotherElement',
        'another_element'                : 'anotherElement',
        'another-Element'                : 'anotherElement',
        'another-element'                : 'anotherElement',
        andAnotherElement                : 'andAnotherElement',
        'and-AnotherElement'             : 'andAnotherElement',
        'and_AnotherElement'             : 'andAnotherElement',
        'and-anotherElement'             : 'andAnotherElement',
        'and_anotherElement'             : 'andAnotherElement',
        'related-cancer-diagnoses-40377' : 'relatedCancerDiagnoses40377',
        'positivenegativeunknown-3041'   : 'positivenegativeunknown3041',
        'name-and-version-of-assent-form': 'nameAndVersionOfAssentForm',
        'aa.nhs.thing'                   : 'aaNhsThing',
        ELEMENT                          : 'element',
        'TS.GB-en-NHS.Date'              : 'tsGbEnNhsDate',
        'PN.NHS.Internal'                : 'pnNhsInternal',
        'ST.GB-en-NHS.StringType1'       : 'stGbEnNhsStringType1',
        'set_cs_EntityNameUse'           : 'setCsEntityNameUse',
        anotherAnotherElement            : 'anotherElement',
        GMPSpecified                     : 'gmpSpecified',
        'COSDCOSDRecordType'             : 'cosdRecordType',
        'II.NPfIT.root.uuid'             : 'iiNPfItRootUuid',
    ]

    @Test
    void namingStandardisation() {
        testNames.each {name, expected ->
            logger.info('---- Testing {} ----', name)
            assert standardiseTypeName(name) == expected
        }
    }
}

/*
 * Copyright 2020-2023 University of Oxford and NHS England
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
package ox.softeng.metadatacatalogue.plugins.xsd.utils

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.utils.XsdNaming

import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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

/**
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.selector;

import com.google.common.base.Strings;
import org.w3c.dom.Element;
import org.xmlunit.diff.ElementSelector;

import static uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator.IgnoreNameAttributeDifferenceEvaluator.stripVersion;

import static org.xmlunit.diff.ElementSelectors.byName;

/**
 * @since 24/02/2017
 */
public class CustomElementSelector implements ElementSelector {

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        if (!byName.canBeCompared(controlElement, testElement)) {
            return false;
        }

        String controlName = controlElement.getAttribute("name");
        String testName = testElement.getAttribute("name");
        Boolean result = !Strings.isNullOrEmpty(controlName) && !Strings.isNullOrEmpty(testName) ||
                         stripVersion(controlName).equals(stripVersion(testName));

        if (result && controlElement.getLocalName().equals("enumeration")) {
            String controlValue = controlElement.getAttribute("value");
            String testValue = testElement.getAttribute("value");
            result = !Strings.isNullOrEmpty(controlValue) && controlValue.equals(testValue);
        }

        return result;
    }


}


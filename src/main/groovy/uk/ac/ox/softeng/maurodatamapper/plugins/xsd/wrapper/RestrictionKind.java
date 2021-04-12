/**
 * Copyright 2020 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper;

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.XsdPlugin;
import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema.Facet;

import com.google.common.base.CaseFormat;

import java.util.Arrays;
import javax.xml.bind.JAXBElement;

/**
 * @since 12/09/2017
 */
public enum RestrictionKind {

    maxExclusive("Max Exclusive"),
    minInclusive("Min Exclusive"),
    minLength("Min Length"),
    enumeration("Enumeration"),
    length("Length"),
    pattern("Pattern/Regex"),
    fractionDigits("Number of Fraction Digits"),
    whiteSpace("Whitespace Handling"),
    totalDigits("Total Digits"),
    maxLength("Max Length"),
    minExclusive("Min Exclusive"),
    maxInclusive("Max Exclusive");

    public final String displayText;

    RestrictionKind(String displayText) {
        this.displayText = XsdPlugin.METADATA_LABEL_RESTRICTION_PREFIX + displayText;
    }

    public static RestrictionKind findFromDisplayText(String displayText) {
        return Arrays.stream(values()).filter(v -> v.displayText.equalsIgnoreCase(displayText)).findFirst().orElse(null);
    }

    public static RestrictionKind findFromElement(JAXBElement element) {
        return RestrictionKind.valueOf(element.getName().getLocalPart());
    }

    public static RestrictionKind findFromFacet(Facet facet) {
        return RestrictionKind.valueOf(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, facet.getClass().getSimpleName()));
    }
}

/**
 * Copyright 2020-2022 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.diff.evaluator;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * @since 13/09/2017
 */
public class IgnoreNameAttributeDifferenceEvaluator implements DifferenceEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(IgnoreNameAttributeDifferenceEvaluator.class);

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == ComparisonResult.EQUAL) return outcome; // only evaluate differences.
        if (comparison.getType() == ComparisonType.ATTR_VALUE) {
            if (stripVersion((String) comparison.getTestDetails().getValue()).equals(stripVersion((String) comparison.getTestDetails().getValue()))) {
                return ComparisonResult.EQUAL;
            }
        }
        return outcome;
    }

    public static String stripVersion(String name) {
        return Strings.isNullOrEmpty(name) ? "" : name.replaceFirst("-\\d+$", "");
    }
}

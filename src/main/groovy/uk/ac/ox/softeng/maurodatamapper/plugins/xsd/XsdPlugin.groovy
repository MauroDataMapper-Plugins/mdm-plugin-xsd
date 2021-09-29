/*
 * Copyright 2020-2021 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
package uk.ac.ox.softeng.maurodatamapper.plugins.xsd

import uk.ac.ox.softeng.maurodatamapper.plugins.xsd.wrapper.RestrictionKind
import uk.ac.ox.softeng.maurodatamapper.provider.plugin.MauroDataMapperPlugin

/**
 * @since 14/09/2017
 */
abstract class XsdPlugin implements MauroDataMapperPlugin
{
    //TODO This will have to be implemented as a trait to be shared between the importer and exporter

    public static final String METADATA_LABEL_PREFIX = 'XSD '
    public static final String METADATA_LABEL_RESTRICTION_PREFIX = METADATA_LABEL_PREFIX + 'Restriction '

    public static final String METADATA_NAMESPACE = XsdPlugin.getPackage().getName()

    public static final String METADATA_XSD_TARGET_NAMESPACE = METADATA_LABEL_PREFIX + 'XML Target Namespace'
    public static final String METADATA_XSD_TARGET_NAMESPACE_PREFIX = METADATA_LABEL_PREFIX + 'XML Target Namespace Prefix'
    public static final String METADATA_XSD_RESTRICTION_BASE = METADATA_LABEL_RESTRICTION_PREFIX + "Restriction Base"
    public static final String METADATA_XSD_UNION = METADATA_LABEL_PREFIX + 'Union'
    public static final String METADATA_XSD_LIST = METADATA_LABEL_PREFIX + 'List'
    public static final String METADATA_XSD_EXTENSION_BASE = METADATA_LABEL_PREFIX + 'Extension Base'
    public static final String METADATA_XSD_DEFAULT = METADATA_LABEL_PREFIX + 'Default'
    public static final String METADATA_XSD_FIXED = METADATA_LABEL_PREFIX + 'Fixed'
    public static final String METADATA_XSD_CHOICE = METADATA_LABEL_PREFIX + 'Choice Group'
    public static final String METADATA_XSD_ALL = METADATA_LABEL_PREFIX + 'All Group'
    public static final String METADATA_XSD_ATTRIBUTE = METADATA_LABEL_PREFIX + 'Attribute'
    public static final String METADATA_XSD_ATTRIBUTE_NAME = METADATA_LABEL_PREFIX + 'Attribute Name'
    public static final String METADATA_XSD_MIN_LENGTH =METADATA_LABEL_PREFIX + 'Min Length'

    public static final List<String> PRIMITIVE_XML_TYPES = Arrays.asList(
        // Primitive Types
        'string',
        'boolean',
        'decimal',
        'float',
        'double',
        'duration',
        'dateTime',
        'time',
        'date',
        'gYearMonth',
        'gYear',
        'gMonthDay',
        'gDay',
        'gMonth',
        'hexBinary',
        'base64Binary',
        'anyURI',
        'QName',
        'NOTATION',
        // Derived Types
        'normalizedString',
        'token',
        'language',
        'IDREFS',
        'ENTITIES',
        'NMTOKEN',
        'NMTOKENS',
        'Name',
        'NCName',
        'ID',
        'IDREF',
        'ENTITY',
        'integer',
        'nonPositiveInteger',
        'negativeInteger',
        'long',
        'int',
        'short',
        'byte',
        'nonNegativeInteger',
        'unsignedLong',
        'unsignedInt',
        'unsignedShort',
        'unsignedByte',
        'positiveInteger')

    Boolean allowsExtraMetadataKeys() {
        false
    }

    Set<String> getKnownMetadataKeys() {
        ([METADATA_XSD_TARGET_NAMESPACE,
          METADATA_XSD_TARGET_NAMESPACE_PREFIX,
          METADATA_XSD_RESTRICTION_BASE,
          METADATA_XSD_EXTENSION_BASE,
          METADATA_XSD_DEFAULT,
          METADATA_XSD_FIXED,
          METADATA_XSD_CHOICE,
          METADATA_XSD_MIN_LENGTH,
          METADATA_XSD_ALL] +
         RestrictionKind.values().collect {it.displayText}) as HashSet
    }
}

/**
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.19 at 04:44:22 PM GMT 
//


package uk.ac.ox.softeng.maurodatamapper.plugins.xsd.org.w3.xmlschema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Only elements allowed inside
 *
 * <p>Java class for all complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="all"$gt;
 *   &lt;complexContent$gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}explicitGroup"$gt;
 *       &lt;group ref="{http://www.w3.org/2001/XMLSchema}allModel"/$gt;
 *       &lt;attribute name="minOccurs" default="1"$gt;
 *         &lt;simpleType$gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"$gt;
 *             &lt;enumeration value="0"/$gt;
 *             &lt;enumeration value="1"/$gt;
 *           &lt;/restriction$gt;
 *         &lt;/simpleType$gt;
 *       &lt;/attribute$gt;
 *       &lt;attribute name="maxOccurs" default="1"$gt;
 *         &lt;simpleType$gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}allNNI"$gt;
 *             &lt;enumeration value="1"/$gt;
 *           &lt;/restriction$gt;
 *         &lt;/simpleType$gt;
 *       &lt;/attribute$gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/$gt;
 *     &lt;/restriction$gt;
 *   &lt;/complexContent$gt;
 * &lt;/complexType$gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "all")
public class All
    extends ExplicitGroup {


}

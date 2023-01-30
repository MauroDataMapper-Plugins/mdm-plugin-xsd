/**
 * Copyright 2020-2023 University of Oxford and Health and Social Care Information Centre, also known as NHS Digital
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
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for realGroup complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="realGroup"$gt;
 *   &lt;complexContent$gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}group"$gt;
 *       &lt;sequence$gt;
 *         &lt;element ref="{http://www.w3.org/2001/XMLSchema}annotation" minOccurs="0"/$gt;
 *         &lt;choice minOccurs="0"$gt;
 *           &lt;element ref="{http://www.w3.org/2001/XMLSchema}all"/$gt;
 *           &lt;element ref="{http://www.w3.org/2001/XMLSchema}choice"/$gt;
 *           &lt;element ref="{http://www.w3.org/2001/XMLSchema}sequence"/$gt;
 *         &lt;/choice$gt;
 *       &lt;/sequence$gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/$gt;
 *     &lt;/restriction$gt;
 *   &lt;/complexContent$gt;
 * &lt;/complexType$gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "realGroup")
@XmlSeeAlso({
                GroupRef.class,
                Group.class
            })
public class RealGroup
    extends AbstractGroup {


}

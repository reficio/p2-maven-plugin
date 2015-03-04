/**
 * Copyright (c) 2012 Reficio (TM) - Reestablish your software! All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.reficio.p2.utils;

import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtils {
    public static Document parseXml(InputStream input) {
    	try {
	    	DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    	fac.setValidating(false);
	    	
	    	DocumentBuilder docBuilder = fac.newDocumentBuilder();
	    	Document doc = docBuilder.parse(input);
	    	
	    	return doc;
    	}catch (Exception e) {
    		 throw new RuntimeException(e.getMessage(), e);
    	}
    }
    
    public static void writeXml(Document doc, File outputFile) {
    	try {
	    	Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    	Result output = new StreamResult(outputFile);
	    	Source input = new DOMSource(doc);
	    	transformer.transform(input, output);
    	}catch (Exception e) {
    		 throw new RuntimeException(e.getMessage(), e);
    	}
    }
    
    public static Element fetchOrCreateElement(Document doc, Node parent, String tagName) {
		NodeList nl = parent.getChildNodes();
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); ++i) {
				Node n = nl.item(i);
				if (n instanceof Element) {
					Element e = (Element) n;
					if (((Element) n).getTagName().equals(tagName)) {
						return e;
					}
				}
			}
		}
		// if get to here, not found element with this tagName, so create it
		return createElement(doc, parent, tagName);
	}

	public static Element createElement(Document doc, Node parent, String tagName) {
		Element e = doc.createElement(tagName);
		parent.appendChild(e);
		return e;
	}
}

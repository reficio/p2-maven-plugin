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
package org.reficio.p2;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.reficio.p2.bundler.ArtifactBundlerInstructions;
import org.reficio.p2.utils.Utils;
import org.reficio.p2.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Multimap;

import aQute.lib.osgi.Jar;

public class FeatureBuilder {

	public FeatureBuilder(P2FeatureDefinition p2FeatureDefintion, Map<P2Artifact, ArtifactBundlerInstructions>  bundlerInstructions, String timestamp) {
		this.p2FeatureDefintion = p2FeatureDefintion;
		this.bundlerInstructions = bundlerInstructions;
		this.featureTimeStamp = timestamp;
	}

	Map<P2Artifact, ArtifactBundlerInstructions>  bundlerInstructions;
	P2FeatureDefinition p2FeatureDefintion;

	public void generate(File destinationFolder) {
		try {
			File featureContent = new File(destinationFolder, this.getFeatureFullName());
			featureContent.mkdir();
			this.buildXml();
			XmlUtils.writeXml(this.xmlDoc, new File(featureContent, "feature.xml"));
			
			//we must be generating the feature file from the pom
			FileOutputStream fos = new FileOutputStream(new File(destinationFolder, this.getFeatureFullName()+".jar"));
			Manifest mf = new Manifest();
			JarOutputStream jar = new JarOutputStream(fos, mf);
			addToJar(jar, featureContent);
			
		} catch (Exception e) {
			throw new RuntimeException("Cannot generate feature", e);
		}
	}
	
	private void addToJar(JarOutputStream jar, File content) throws IOException
	{
		for (File f : FileUtils.listFiles(content, null, true) ) {
			String fname = f.getPath().replace("\\", "/");
			if (f.isDirectory()) {
				if (!fname.endsWith("/")) {
					fname = fname + "/";
				}
				JarEntry entry = new JarEntry(fname);
				entry.setTime(f.lastModified());
				jar.putNextEntry(entry);
				jar.closeEntry();
			} else {
				//must be a file
				JarEntry entry = new JarEntry(fname);
				entry.setTime(f.lastModified());
				jar.putNextEntry(entry);
				jar.write( IOUtils.toByteArray(new FileInputStream(f)) );
				jar.closeEntry();
			}
			

		}
	}
	
	//cache this so that the same timestamp is used
	String featureTimeStamp;
	
	
	String getQualifiedFeatureVersion() {
		String v = this.p2FeatureDefintion.getVersion();
		return v.replace("qualifier",featureTimeStamp);
	}
	
	String getFeatureFullName() {
		String fn = this.p2FeatureDefintion.getId()+"_"+this.getQualifiedFeatureVersion();
		return fn;
	}
	
	Document xmlDoc;
	void buildXml() throws ParserConfigurationException, FileNotFoundException {
		xmlDoc = this.fetchOrCreateXml();
		Element featureElement = XmlUtils.fetchOrCreateElement(xmlDoc, xmlDoc, "feature");
		if (null != this.p2FeatureDefintion.getId()) {
			featureElement.setAttribute("id", this.p2FeatureDefintion.getId());
		} else if (featureElement.hasAttribute("id")) {
			this.p2FeatureDefintion.setId(featureElement.getAttribute("id"));
		} else {
			throw new RuntimeException("No id defined for feature in pom or featureFile");
		}
		if (null != this.p2FeatureDefintion.getVersion()) {
			featureElement.setAttribute("version", this.p2FeatureDefintion.getVersion());
		} else if (featureElement.hasAttribute("version")) {
			this.p2FeatureDefintion.setVersion(featureElement.getAttribute("version"));
		} else {
			throw new RuntimeException("No version defined for feature in pom or featureFile");
		}
		if (null != this.p2FeatureDefintion.getLabel()) {
			featureElement.setAttribute("label", this.p2FeatureDefintion.getLabel());
		}
		if (null != this.p2FeatureDefintion.getProviderName()) {
			featureElement.setAttribute("provider-name", this.p2FeatureDefintion.getProviderName());
		}
		if (null != this.p2FeatureDefintion.getDescription()) {
			Element descriptionElement = XmlUtils.fetchOrCreateElement(xmlDoc, featureElement, "description");
			descriptionElement.setTextContent(this.p2FeatureDefintion.getDescription());
		}
		if (null != this.p2FeatureDefintion.getCopyright()) {
			Element copyrightElement = XmlUtils.fetchOrCreateElement(xmlDoc, featureElement, "copyright");
			copyrightElement.setTextContent(this.p2FeatureDefintion.getCopyright());
		}
		if (null != this.p2FeatureDefintion.getLicense()) {
			Element licenceElement = XmlUtils.fetchOrCreateElement(xmlDoc, featureElement, "license");
			licenceElement.setTextContent(this.p2FeatureDefintion.getLicense());
		}

		for(P2Artifact artifact: this.p2FeatureDefintion.artifacts) {
			Element pluginElement = XmlUtils.createElement(xmlDoc,featureElement,"plugin");
			String id = this.bundlerInstructions.get(artifact).getSymbolicName();
			String version = this.bundlerInstructions.get(artifact).getProposedVersion();
			pluginElement.setAttribute("id", id);
			pluginElement.setAttribute("download-size", "0"); //TODO
			pluginElement.setAttribute("install-size", "0");  //TODO
			pluginElement.setAttribute("version", version);
			pluginElement.setAttribute("unpack", "false");
			
		}
		
		//update qualified version if need be
		String xmlVersion = featureElement.getAttribute("version");
		if (xmlVersion.contains("qualifier")) {
			featureElement.setAttribute("version", xmlVersion.replace("qualifier", featureTimeStamp));
		}
	}

	Document fetchOrCreateXml() throws ParserConfigurationException, FileNotFoundException {
		if (null == this.p2FeatureDefintion.featureFile) {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		} else {
			return XmlUtils.parseXml(new FileInputStream(this.p2FeatureDefintion.featureFile));
		}
	}

	public void generateSourceFeature(File destinationFolder) {
		Element featureElement = XmlUtils.fetchOrCreateElement(xmlDoc, xmlDoc, "feature");
		featureElement.setAttribute("id", featureElement.getAttribute("id")+".source");
		featureElement.setAttribute("label", featureElement.getAttribute("label")+" Developer Resources");
		NodeList nl = featureElement.getElementsByTagName("plugin");
		List<Element> elements = new ArrayList<Element>();
		//can't remove as we iterate over nl, because its size changes when we remove
		for(int n = 0; n < nl.getLength(); ++n) {
			elements.add((Element)nl.item(n));
		}
		for(Element e: elements) {
			featureElement.removeChild(e);
		}
		
		for(P2Artifact artifact: this.p2FeatureDefintion.artifacts) {
			Element pluginElement = XmlUtils.createElement(xmlDoc,featureElement,"plugin");
			String id = this.bundlerInstructions.get(artifact).getSourceSymbolicName();
			String version = this.bundlerInstructions.get(artifact).getProposedVersion();
			pluginElement.setAttribute("id", id);
			pluginElement.setAttribute("download-size", "0"); //TODO
			pluginElement.setAttribute("install-size", "0");  //TODO
			pluginElement.setAttribute("version", version);
			pluginElement.setAttribute("unpack", "false");
			
		}
		
		try {
			File sourceFeatureContent = new File(destinationFolder, this.getFeatureFullName()+".source");
			sourceFeatureContent.mkdir();
			XmlUtils.writeXml(this.xmlDoc, new File(sourceFeatureContent, "feature.xml"));
			
			//TODO: add other files that are required by the feature
			
			FileOutputStream fos = new FileOutputStream(new File(destinationFolder, this.getFeatureFullName()+".jar"));
			Manifest mf = new Manifest();
			JarOutputStream jar = new JarOutputStream(fos, mf);
			addToJar(jar, sourceFeatureContent);
		} catch (Exception e) {
			throw new RuntimeException("Cannot generate feature", e);
		}
	}


	void createFeatureWithTycho() {
	}
}

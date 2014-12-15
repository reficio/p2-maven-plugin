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

import java.io.File;
import java.util.List;

/**
 * Represents one &lt;feature&gt; section in the plugin configuration.
 * This class is mutable only because the values are set by Maven using the setters.
 *
 * @author Dr. David H. Akehurst<br>
 *         itemis<br>
 *         http://www.itemis.de
 * @since 1.1.2
 */
public class P2FeatureDefinition extends P2Feature {
	
	public P2FeatureDefinition() {
	}
	
	File featureFile;
	public File getFeatureFile() {
		return featureFile;
	}
	public void setFeatureFile(File featureFile) {
		this.featureFile = featureFile;
	}
	
	String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	String version;
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	String label;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	String providerName;
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	String description;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	String copyright;
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	String license;
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	
	boolean generateSourceFeature;
	public boolean getGenerateSourceFeature() {
		return generateSourceFeature;
	}
	public void setGenerateSourceFeature(boolean generateSourceFeature) {
		this.generateSourceFeature = generateSourceFeature;
	}
	
	List<P2Artifact> artifacts;
	public List<P2Artifact> getArtifacts() {
		return artifacts;
	}
	public void setArtifacts(List<P2Artifact> artifacts) {
		this.artifacts = artifacts;
	}

//	List<> includes;
//	List<> requires;
	
}

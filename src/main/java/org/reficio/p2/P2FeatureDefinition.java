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
import java.util.ArrayList;
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
public class P2FeatureDefinition  {
	
	public P2FeatureDefinition() {
	}
	
	private File featureFile;
	private String id;
	private String version;
	private String label;
	private String providerName;
	private String description;
	private String copyright;
	private String license;
	private boolean generateSourceFeature;
	private List<P2Artifact> artifacts;
	private boolean unpack;
	
	
	public File getFeatureFile() {
		return featureFile;
	}
	public void setFeatureFile(File featureFile) {
		this.featureFile = featureFile;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}
	
	public boolean getGenerateSourceFeature() {
		return generateSourceFeature;
	}
	public void setGenerateSourceFeature(boolean generateSourceFeature) {
		this.generateSourceFeature = generateSourceFeature;
	}
	
	public List<P2Artifact> getArtifacts() {
		if (null==artifacts) {
			this.artifacts = new ArrayList<P2Artifact>();
		}
		return artifacts;
	}
	public void setArtifacts(List<P2Artifact> artifacts) {
		this.artifacts = artifacts;
	}
	
	public boolean getUnpack() {
		return unpack;
	}
	public void setUnpack(boolean unpack) {
		this.unpack = unpack;
	}
	
}

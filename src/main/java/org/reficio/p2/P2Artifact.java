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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents one &lt;artifact&gt; section in the plugin configuration.
 * This class is mutable only because the values are set by Maven using the setters.
 *
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public class P2Artifact {

    /**
     * Artifact id in the following format "groupId:artifactId:version"
     */
    private String id;

    /**
     * Indicator to include transitive dependencies
     */
    private boolean transitive = true;

    /**
     * Indicator to override default manifest
     */
    private boolean override = false;

    /**
     * Indicator to include source dependencies
     */
    private boolean source = false;

    /**
     * Indicator to generate a singleton bundle
     */
    private boolean singleton = false;

    /**
     * Specifies transitive dependencies that should be excluded
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * The BND instructions for the bundle.
     */
    private Map<String, Object> instructions = new LinkedHashMap<String, Object>();

    public P2Artifact() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getInstructions() {
        return instructions;
    }

    public void setInstructions(Map<String, Object> instructions) {
        this.instructions = instructions;
    }

    public boolean shouldIncludeTransitive() {
        return transitive;
    }

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    public boolean shouldOverrideManifest() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public boolean shouldIncludeSources() {
        return source;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public String getVersion() {
    	String[] parts = this.getId().split(":");
    	if (parts.length < 3) {
    		throw new RuntimeException("Artifact Id must be of the form <groupId>:<artifactId>:<version>: "+this.getId());
    	}
    	return parts[2];
    }
    
    public String getArtifactId() {
    	String[] parts = this.getId().split(":");
    	if (parts.length < 3) {
    		throw new RuntimeException("Artifact Id must be of the form <groupId>:<artifactId>:<version>: "+this.getId());
    	}
    	return parts[1];
    }
    
    public String getGroupId() {
    	String[] parts = this.getId().split(":");
    	if (parts.length < 3) {
    		throw new RuntimeException("Artifact Id must be of the form <groupId>:<artifactId>:<version>: "+this.getId());
    	}
    	return parts[1];
    }
}

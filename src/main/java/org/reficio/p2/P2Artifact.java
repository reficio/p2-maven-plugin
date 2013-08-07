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

import org.reficio.p2.utils.ResolvedArtifact;
import org.sonatype.aether.artifact.Artifact;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
public class P2Artifact {

    /**
     * Artifact id the following format "groupId:artifactId:version"
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
     * Specifies transitive dependencies that should be excluded
     */
    private List<String> excludes = new ArrayList<String>();

    /**
     * The BND instructions for the bundle.
     */
    private Map instructions = new LinkedHashMap();

    private List<ResolvedArtifact> resolvedArtifacts = new ArrayList<ResolvedArtifact>();

    public P2Artifact() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map getInstructions() {
        return instructions;
    }

    public void setInstructions(Map instructions) {
        this.instructions = instructions;
    }

    public void addResolvedArtifact(Artifact resolved, Artifact resolvedSource) {
		// <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>
		// <groupId>:<artifactId>:<version>
		String resolvedId = String.format("%s:%s:%s", resolved.getGroupId(), resolved.getArtifactId(),
				resolved.getBaseVersion());
		// <groupId>:<artifactId>:<extension>:<version>
		String resolved2Id = String.format("%s:%s:%s:%s", resolved.getGroupId(), resolved.getArtifactId(),
				resolved.getExtension(), resolved.getBaseVersion());
		// <groupId>:<artifactId>:<extension>:<classifier>:<version>
		String resolved3Id = String.format("%s:%s:%s:%s:%s", resolved.getGroupId(), resolved.getArtifactId(),
				resolved.getExtension(), resolved.getClassifier(), resolved.getBaseVersion());
		boolean rootArtifact = id.equals(resolvedId) || id.equals(resolved2Id) || id.equals(resolved3Id);
        ResolvedArtifact resolvedArtifact = new ResolvedArtifact(resolved, resolvedSource, rootArtifact);
        this.resolvedArtifacts.add(resolvedArtifact);
    }

    public List<ResolvedArtifact> getResolvedArtifacts() {
        return resolvedArtifacts;
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

    public boolean shouldIncludeSources() {
        return source;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

}

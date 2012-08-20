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

import org.sonatype.aether.artifact.Artifact;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.0
 * <p/>
 * Reficio (TM) - Reestablish your software!</br>
 * http://www.reficio.org
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
     * Indicator to include transitive dependencies
     */
    private boolean override = false;

    /**
     * The BND instructions for the bundle.
     */
    private Map instructions = new LinkedHashMap();

    private List<Artifact> artifacts;

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

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
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

}

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
package org.reficio.p2.bundler;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Tom Bujok (tom.bujok@gmail.com) <br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public final class ArtifactBundlerInstructions {

    private final String name;
    private final String symbolicName;
    private final String symbolicNameWithOptions;
    private final String version;
    private final String sourceName;
    private final String sourceSymbolicName;
    private final String proposedVersion;

    private final Map<String, String> instructions;
    private final Boolean snapshot;

    private ArtifactBundlerInstructions(String name, String symbolicName, String symbolicNameWithOptions, String version, String sourceName,
                                        String sourceSymbolicName, String proposedVersion, Map<String, String> instructions,
                                        Boolean snapshot) {
        this.name = name;
        this.symbolicName = symbolicName;
        this.symbolicNameWithOptions = symbolicNameWithOptions;
        this.version = version;
        this.sourceName = sourceName;
        this.sourceSymbolicName = sourceSymbolicName;
        this.proposedVersion = proposedVersion;
        this.instructions = instructions;
        this.snapshot = snapshot;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private String symbolicName;
        private String symbolicNameWithOptions;
        private String version;
        private String sourceName;
        private String sourceSymbolicName;
        private String proposedVersion;
        private Map<String, String> instructions = new HashMap<String, String>();
        private Boolean snapshot;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder symbolicName(String symbolicName) {
            this.symbolicName = symbolicName;
            return this;
        }

        public Builder symbolicNameWithOptions(String symbolicNameWithOptions) {
            this.symbolicNameWithOptions = symbolicNameWithOptions;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public Builder sourceSymbolicName(String sourceSymbolicName) {
            this.sourceSymbolicName = sourceSymbolicName;
            return this;
        }

        public Builder proposedVersion(String proposedVersion) {
            this.proposedVersion = proposedVersion;
            return this;
        }

        public  Builder instructions(Map<String, String> instructions) {
            this.instructions = new HashMap<String, String>(instructions);
            return this;
        }

        public Builder snapshot(Boolean snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public ArtifactBundlerInstructions build() {
            return new ArtifactBundlerInstructions(name, symbolicName, symbolicNameWithOptions,
                    version, sourceName, sourceSymbolicName, proposedVersion, instructions, snapshot);
        }
    }

    public String getName() {
        return name;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getSymbolicNameWithOptions() {
        return symbolicNameWithOptions;
    }

    public String getVersion() {
        return version;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceSymbolicName() {
        return sourceSymbolicName;
    }

    public String getProposedVersion() {
        return proposedVersion;
    }

    public Map<String, String> getInstructions() {
        return instructions;
    }

    public Boolean isSnapshot() {
        return snapshot;
    }
}

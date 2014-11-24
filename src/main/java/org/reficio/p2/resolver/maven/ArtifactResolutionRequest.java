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
package org.reficio.p2.resolver.maven;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public final class ArtifactResolutionRequest {

    private final String rootArtifactId;
    private final List<String> excludes;
    private final boolean resolveTransitive;
    private final boolean resolveSource;

    private ArtifactResolutionRequest(String rootArtifactId, List<String> excludes,
                                      boolean resolveTransitive, boolean resolveSource) {
        this.rootArtifactId = rootArtifactId;
        this.excludes = excludes;
        this.resolveTransitive = resolveTransitive;
        this.resolveSource = resolveSource;
    }

    public String getRootArtifactId() {
        return rootArtifactId;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public boolean isResolveTransitive() {
        return resolveTransitive;
    }

    public boolean isResolveSource() {
        return resolveSource;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String rootArtifactId;
        private List<String> excludes;
        private boolean resolveTransitive;
        private boolean resolveSource;

        public Builder rootArtifactId(String rootArtifactId) {
            this.rootArtifactId = rootArtifactId;
            return this;
        }

        public Builder excludes(List<String> excludes) {
            this.excludes = new ArrayList<String>(excludes);
            return this;
        }

        public Builder resolveTransitive(boolean resolveTransitive) {
            this.resolveTransitive = resolveTransitive;
            return this;
        }

        public Builder resolveSource(boolean resolveSource) {
            this.resolveSource = resolveSource;
            return this;
        }

        public ArtifactResolutionRequest build() {
            return new ArtifactResolutionRequest(rootArtifactId, excludes, resolveTransitive, resolveSource);
        }
    }
}

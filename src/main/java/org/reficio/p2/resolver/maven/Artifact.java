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

import java.io.File;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import static java.util.Arrays.asList;

/**
 * Represents one artifact (normally a jar file).
 * Decouples the plugin's model and the Aether model.
 *
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public class Artifact {

    private final String groupId;
    private final String artifactId;
    private final String baseVersion;
    private final String extension;
    private final String classifier;
    private final boolean snapshot;
    private final String version;
    private final File file;

    public Artifact(String groupId, String artifactId, String baseVersion, String extension, String classifier,
                    boolean snapshot, String version, File file) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseVersion = baseVersion;
        this.extension = extension;
        this.classifier = classifier;
        this.snapshot = snapshot;
        this.version = version;
        this.file = file;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public String getExtension() {
        return extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public String getVersion() {
        return version;
    }

    public File getFile() {
        return file;
    }

    public String getShortId() {
        // <groupId>:<artifactId>:<version>
        return String.format("%s:%s:%s", getGroupId(), getArtifactId(),
                getBaseVersion());
    }

    public String getExtendedId() {
        // <groupId>:<artifactId>:<extension>:<version>
        return String.format("%s:%s:%s:%s", getGroupId(), getArtifactId(),
                getExtension(), getBaseVersion());
    }

    public String getLongId() {
        // <groupId>:<artifactId>:<extension>:<classifier>:<version>
        return String.format("%s:%s:%s:%s:%s", getGroupId(), getArtifactId(),
                getExtension(), getClassifier(), getBaseVersion());
    }

    public String toString() {
        if (file != null) {
            return file.getName();
        } else {
            return groupId + "." + artifactId + "-" + version + "." + extension;
        }
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other, asList("file"));
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, asList("file"));
    }
}

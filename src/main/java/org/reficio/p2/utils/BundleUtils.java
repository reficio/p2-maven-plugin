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

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;
import org.apache.felix.bundleplugin.BundlePlugin;
import org.apache.maven.artifact.DefaultArtifact;
import org.sonatype.aether.artifact.Artifact;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
public class BundleUtils extends BundlePlugin {

    private static final String BUNDLE_SYMBOLIC_NAME_ATTR_NAME = "Bundle-SymbolicName";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String BUNDLE_NAME = "Bundle-Name";

    public boolean reportErrors(Analyzer analyzer) {
        return super.reportErrors("", analyzer);
    }

    public static org.apache.maven.artifact.Artifact aetherToMavenArtifactBasic(Artifact artifact) {
        DefaultArtifact mavenArtifact = new DefaultArtifact(
                artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(),
                "compile", artifact.getExtension(), artifact.getClassifier(), null);
        return mavenArtifact;
    }

    public String calculateBundleSymbolicName(Artifact artifact) {
        return super.getMaven2OsgiConverter().getBundleSymbolicName(aetherToMavenArtifactBasic(artifact));
    }

    public String calculateBundleVersion(Artifact artifact) {
        return super.getMaven2OsgiConverter().getVersion(aetherToMavenArtifactBasic(artifact));
    }

    public boolean isBundle(Jar jar) {
        return getBundleSymbolicName(jar) != null;
    }

    public String getBundleSymbolicName(Jar jar) {
        return getManifestValue(jar, BUNDLE_SYMBOLIC_NAME_ATTR_NAME);
    }

    public String getBundleVersion(Jar jar) {
        return getManifestValue(jar, BUNDLE_VERSION);
    }

    public String getBundleName(Jar jar) {
        return getManifestValue(jar, BUNDLE_NAME);
    }

    private String getManifestValue(Jar jar, String attributeName) {
        try {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return null;
            }
            Attributes.Name symbolicName = new Attributes.Name(attributeName);
            Attributes attributes = manifest.getMainAttributes();
            if (attributes == null) {
                return null;
            }
            return attributes.getValue(symbolicName);
        } catch (IOException e) {
            return null;
        }
    }

    public static Properties transformDirectives(Map instructions) {
        Properties properties = new Properties();
        properties.putAll(BundlePlugin.transformDirectives(instructions));
        return properties;
    }

}

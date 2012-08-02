/**
 * Copyright (c) 2012 centeractive ag. All Rights Reserved.
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
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.sonatype.aether.artifact.Artifact;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2012-02-09
 * Time: 9:46 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class BundleUtils extends BundlePlugin {

    private static final String BUNDLE_SYMBOLIC_NAME_ATTR_NAME = "Bundle-SymbolicName";

    public boolean reportErrors(Analyzer analyzer) {
        return super.reportErrors("", analyzer);
    }

    public static org.apache.maven.artifact.Artifact aetherToMavenArtifactBasic(Artifact artifact) {
    	String type = artifact.getProperty("type", "");
    	String scope = org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
    	String classifier = null;
    	ArtifactHandler artifactHandler = new DefaultArtifactHandler(type);
    	
		DefaultArtifact mavenArtifact = new DefaultArtifact(
				artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getVersion(), 
                scope, type, classifier, artifactHandler);
        
		return mavenArtifact;
    }

    public String getBundleSymbolicName(Artifact artifact) {
        return super.getMaven2OsgiConverter().getBundleSymbolicName(aetherToMavenArtifactBasic(artifact));
    }

    public String getBundleVersion(Artifact artifact) {
        return super.getMaven2OsgiConverter().getVersion(aetherToMavenArtifactBasic(artifact));
    }

    public boolean isBundle(Jar jar) {
        try {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return false;
            }
            Attributes.Name symbolicName = new Attributes.Name(BUNDLE_SYMBOLIC_NAME_ATTR_NAME);
            Attributes attributes = manifest.getMainAttributes();
            return attributes != null && attributes.containsKey(symbolicName);
        } catch (IOException e) {
            return false;
        }
    }

}

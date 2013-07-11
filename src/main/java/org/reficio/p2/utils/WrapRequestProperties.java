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
import org.reficio.p2.P2Artifact;

import java.io.IOException;

public class WrapRequestProperties {

    private final String name;
    private final String symbolicName;
    private final String version;

    private final String sourceName;
    private final String sourceSymbolicName;
    private final String sourceVersion;

    private final BundleUtils bundleUtils = new BundleUtils();
    private final ResolvedArtifact resolvedArtifact;
    private final P2Artifact p2artifact;

    public WrapRequestProperties(ResolvedArtifact resolvedArtifact, P2Artifact p2artifact) {
        this.resolvedArtifact = resolvedArtifact;
        this.p2artifact = p2artifact;
        try {
            this.symbolicName = calculateSymbolicName();
            this.name = calculateName(symbolicName);
            this.version = calculateVersion();

            this.sourceSymbolicName = calculateSourceSymbolicName(symbolicName);
            this.sourceName = calculateSourceName(name, symbolicName);
            this.sourceVersion = version;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String calculateName(String symbolicName) {
        return symbolicName;
    }

    private String calculateSymbolicName() throws IOException {
        String symbolicName = null;
        if (resolvedArtifact.isRoot()) {
            Object symbolicNameValue = p2artifact.getInstructions().get(Analyzer.BUNDLE_SYMBOLICNAME);
            symbolicName = symbolicNameValue != null ? symbolicNameValue.toString() : null;
        }
        if (symbolicName == null) {
            symbolicName = bundleUtils.getBundleSymbolicName(new Jar(resolvedArtifact.getArtifact().getFile()));
        }
        if (symbolicName == null) {
            symbolicName = bundleUtils.calculateBundleSymbolicName(resolvedArtifact.getArtifact());
        }
        return symbolicName;
    }

    private String calculateVersion() throws IOException {
        String version = null;
        if (resolvedArtifact.isRoot()) {
            Object versionValue = p2artifact.getInstructions().get(Analyzer.BUNDLE_VERSION);
            version = versionValue != null ? versionValue.toString() : null;
        }
        if (version == null) {
            version = bundleUtils.getBundleVersion(new Jar(resolvedArtifact.getArtifact().getFile()));
        }
        if (version == null) {
            version = bundleUtils.calculateBundleVersion(resolvedArtifact.getArtifact());
        }
        return JarUtils.tweakVersion(resolvedArtifact, version);
    }

    public static  String calculateSourceSymbolicName(String symbolicName) {
        return symbolicName + ".source";
    }

    public static String calculateSourceName(String name, String symbolicName) {
        String sourceName = null;
        if (name == null) {
            sourceName = symbolicName + ".source";
        } else {
            sourceName = name.trim();
            if (sourceName.matches(".*\\s+.*")) {
                sourceName += " ";
            } else {
                sourceName += ".";
            }
            if (sourceName.matches(".*[A-Z].*")) {
                sourceName += "Source";
            } else {
                sourceName += "source";
            }
        }
        return sourceName;
    }

    public String getName() {
        return name;
    }

    public String getSymbolicName() {
        return symbolicName;
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

    public String getSourceVersion() {
        return sourceVersion;
    }
}

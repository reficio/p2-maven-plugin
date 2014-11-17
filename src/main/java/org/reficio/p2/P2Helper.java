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

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;
import org.apache.commons.lang.StringUtils;
import org.reficio.p2.bundler.ArtifactBundlerInstructions;
import org.reficio.p2.bundler.ArtifactBundlerRequest;
import org.reficio.p2.bundler.impl.AquteHelper;
import org.reficio.p2.resolver.ResolvedArtifact;
import org.reficio.p2.utils.BundleUtils;
import org.reficio.p2.utils.JarUtils;

import java.io.File;
import java.io.IOException;

/**
 * Glues together the following independent modules that know nothing about the
 * plugin and the p2-maven-plugin model:
 * <ul>
 * <li>artifact resolver module</li>
 * <li>artifact bundler module</li>
 * </ul>
 * Wires up all the internal logic.
 *
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public class P2Helper {

    public static ArtifactBundlerRequest createBundlerRequest(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact, File outputFolder) {
        File binaryInputFile = resolvedArtifact.getArtifact().getFile();
        File binaryOutputFile = new File(outputFolder, resolvedArtifact.getArtifact().getFile().getName());
        File sourceInputFile = null;
        File sourceOutputFile = null;
        if (resolvedArtifact.getSourceArtifact() != null) {
            sourceInputFile = resolvedArtifact.getSourceArtifact().getFile();
            sourceOutputFile = new File(outputFolder, resolvedArtifact.getSourceArtifact().getFile().getName());
        }
        boolean bundle = BundleUtils.INSTANCE.isBundle(resolvedArtifact.getArtifact().getFile());
        boolean shouldBundle = shouldBundle(p2Artifact, resolvedArtifact, bundle);
        return new ArtifactBundlerRequest(binaryInputFile, binaryOutputFile, sourceInputFile, sourceOutputFile, shouldBundle);
    }

    private static boolean shouldBundle(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact, boolean resolvedArtifactIsBundle) {
        if (resolvedArtifactIsBundle) {
            if (p2Artifact.shouldOverrideManifest() && resolvedArtifact.isRoot()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static ArtifactBundlerInstructions createBundlerInstructions(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
        try {
            String symbolicName = calculateSymbolicName(p2Artifact, resolvedArtifact);
            String symbolicNameWithOptions = calculateSymbolicNameWithOptions(p2Artifact, resolvedArtifact, symbolicName);
            String name = calculateName(symbolicName);
            String version = calculateVersion(p2Artifact, resolvedArtifact);
            String proposedVersion = calculateProposedVersion(resolvedArtifact);

            String sourceSymbolicName = calculateSourceSymbolicName(symbolicName);
            String sourceName = calculateSourceName(name, symbolicName);

            ArtifactBundlerInstructions.Builder builder = ArtifactBundlerInstructions.builder()
                    .name(name)
                    .symbolicName(symbolicName)
                    .symbolicNameWithOptions(symbolicNameWithOptions)
                    .version(version)
                    .snapshot(resolvedArtifact.isSnapshot())
                    .sourceName(sourceName)
                    .sourceSymbolicName(sourceSymbolicName)
                    .proposedVersion(proposedVersion);

            if (resolvedArtifact.isRoot()) {
                // Instructions are propagated only to the root dependency
                // and not to the transitive dependencies.
                builder.instructions(p2Artifact.getInstructions());
            }
            return builder.build();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static String calculateName(String symbolicName) {
        return symbolicName;
    }

    private static String calculateSymbolicName(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) throws IOException {
        String symbolicName = null;
        if (resolvedArtifact.isRoot()) {
            Object symbolicNameValue = p2Artifact.getInstructions().get(Analyzer.BUNDLE_SYMBOLICNAME);
            symbolicName = symbolicNameValue != null ? symbolicNameValue.toString() : null;
        }
        if (symbolicName == null) {
            symbolicName = BundleUtils.INSTANCE.getBundleSymbolicName(new Jar(resolvedArtifact.getArtifact().getFile()));
        }
        if (symbolicName == null) {
            symbolicName = BundleUtils.INSTANCE.calculateBundleSymbolicName(resolvedArtifact.getArtifact());
        }
        // bug28 - handle classifiers
        String classifier = resolvedArtifact.getArtifact().getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            symbolicName += "." + classifier;
        }
        return symbolicName;
    }

    private static String calculateVersion(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) throws IOException {
        String version = getUserDefinedVersion(p2Artifact, resolvedArtifact);
        if (version != null) {
            return BundleUtils.INSTANCE.cleanupVersion(version);
        } else {
            return calculateProposedVersion(resolvedArtifact);
        }
    }

    private static String getUserDefinedVersion(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
        String version = null;
        // in case of root artifact try to take the version from the instructions
        if (resolvedArtifact.isRoot()) {
            Object versionValue = p2Artifact.getInstructions().get(Analyzer.BUNDLE_VERSION);
            version = versionValue != null ? JarUtils.replaceSnapshotWithTimestamp(versionValue.toString()) : null;
        }
        // if contains snapshot (manually set by the user) -> "SNAPSHOT" will be manually replaced
        return version;
    }

    private static String calculateProposedVersion(ResolvedArtifact resolvedArtifact) throws IOException {
        String version;
        // otherwise calculate the proper version for snapshot and non-snapshot
        if (resolvedArtifact.isSnapshot()) {
            version = calculateSnapshotVersion(resolvedArtifact);
        } else {
            version = BundleUtils.INSTANCE.getBundleVersion(new Jar(resolvedArtifact.getArtifact().getFile()));
            if (version == null) {
                version = BundleUtils.INSTANCE.calculateBundleVersion(resolvedArtifact.getArtifact());
            }
        }
        // if still contains snapshot (manually set by the user) -> "SNAPSHOT" will be manually replaced
        return BundleUtils.INSTANCE.cleanupVersion(JarUtils.replaceSnapshotWithTimestamp(version));
    }

    private static String calculateSnapshotVersion(ResolvedArtifact resolvedArtifact) {
        // attempt to take the proper snapshot version from the artifact's version
        String version = resolvedArtifact.getArtifact().getVersion();
        if (isProperSnapshotVersion(version)) {
            return version;
        }
        // attempt to take the proper snapshot version from the artifact's baseVersion
        String baseVersion = resolvedArtifact.getArtifact().getBaseVersion();
        if (isProperSnapshotVersion(baseVersion)) {
            return baseVersion;
        }
        // otherwise manually add the SNAPSHOT postfix that will be automatically changed to timestamp
        if (!baseVersion.contains("SNAPSHOT")) {
            baseVersion += ".SNAPSHOT";
        }
        return baseVersion;
    }

    public static boolean isProperSnapshotVersion(String version) {
        return version.matches(".*[0-9\\.]{13,16}-[0-9]{3}");
    }

    public static String calculateSourceSymbolicName(String symbolicName) {
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

    public static String calculateSymbolicNameWithOptions(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact, String symbolicName) {
        String fullSymbolicName = symbolicName;
        if (resolvedArtifact.isRoot() && p2Artifact.isSingleton()) {
            // singleton may be also specified manually by the user
            if (!fullSymbolicName.contains(AquteHelper.SINGLETON)) {
                fullSymbolicName = fullSymbolicName + ";" + AquteHelper.SINGLETON;
            }
        }
        return fullSymbolicName;
    }

}

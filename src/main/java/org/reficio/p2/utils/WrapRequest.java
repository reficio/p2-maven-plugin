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

import org.reficio.p2.P2Artifact;

import java.io.File;

public class WrapRequest {

    private final P2Artifact p2artifact;
    private final ResolvedArtifact resolvedArtifact;
    private final File inputFile;
    private final File outputFile;
    private final boolean bundle;
    private final BundleUtils bundleUtils;

    private final WrapRequestProperties properties;

    private boolean shouldWrap;

    public WrapRequest(P2Artifact p2artifact, ResolvedArtifact resolvedArtifact, File inputFile, File outputFile) {
        this.p2artifact = p2artifact;
        this.resolvedArtifact = resolvedArtifact;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.bundleUtils = new BundleUtils();
        this.bundle = bundleUtils.isBundle(inputFile);
        this.shouldWrap = shouldWrap();
        this.properties = new WrapRequestProperties(resolvedArtifact, p2artifact);
    }

    public boolean shouldNotWrap() {
        return !shouldWrap;
    }

    private boolean shouldWrap() {
        if (bundle) {
            if (p2artifact.shouldOverrideManifest()) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public P2Artifact getP2artifact() {
        return p2artifact;
    }

    public ResolvedArtifact getResolvedArtifact() {
        return resolvedArtifact;
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public WrapRequestProperties getProperties() {
        return properties;
    }

    public void validate() {
        if (resolvedArtifact.isRoot() && bundle) {
            // artifact is a bundle and somebody specified instructions without override
            if (!p2artifact.shouldOverrideManifest() && !p2artifact.getInstructions().isEmpty()) {
                String message = String.format("p2-maven-plugin misconfiguration" +
                        "\n\n\tJar [%s] is already a bundle. " +
                        "\n\tBND instructions are specified, but the <override> flag is set to false." +
                        "\n\tEither remove the instructions or set the <override> flag to true." +
                        "\n\tWATCH OUT! Setting <override> to true will re-bundle the artifact!\n", resolvedArtifact.getArtifact().toString());
                throw new RuntimeException(message);
            }
            if (!p2artifact.shouldOverrideManifest() && p2artifact.isSingleton()) {
                String message = String.format("p2-maven-plugin misconfiguration" +
                        "\n\n\tJar [%s] is already a bundle. " +
                        "\n\tsingleton is set to true, but the <override> flag is set to false." +
                        "\n\tEither set the singleton flag to false or set the <override> flag to true." +
                        "\n\tWATCH OUT! Setting <override> to true will re-bundle the artifact!\n", resolvedArtifact.getArtifact().toString());
                throw new RuntimeException(message);
            }
        }
    }


}

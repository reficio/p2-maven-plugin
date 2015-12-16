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
package org.reficio.p2.bundler.impl;

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import org.apache.commons.io.FileUtils;
import org.reficio.p2.bundler.ArtifactBundlerInstructions;
import org.reficio.p2.bundler.ArtifactBundlerRequest;
import org.reficio.p2.utils.BundleUtils;

import java.io.File;
import java.util.UUID;
import java.util.jar.Manifest;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public class AquteHelper {

    public static final String SINGLETON = "singleton:=true";
    public static final String TOOL_KEY = "Tool";
    public static final String TOOL = "p2-maven-plugin (reficio.org)";

    public static Analyzer buildAnalyzer(ArtifactBundlerRequest request, ArtifactBundlerInstructions instructions, boolean pedantic) throws Exception {
        Analyzer analyzer = instantiateAnalyzer(request);
        setAnalyzerOptions(analyzer, pedantic);
        setPackageOptions(analyzer);
        setInstructions(analyzer, instructions);
        // they are set later as they may overwrite some instructions
        setBundleOptions(analyzer, instructions);
        setManifest(analyzer);
        return analyzer;
    }

    private static Analyzer instantiateAnalyzer(ArtifactBundlerRequest request) throws Exception {
        Analyzer analyzer = new Analyzer();
        analyzer.setJar(getInputJarWithBlankManifest(request));
        return analyzer;
    }

    private static void setAnalyzerOptions(Analyzer analyzer, boolean pedantic) {
        analyzer.setPedantic(pedantic);
    }

    private static void setPackageOptions(Analyzer analyzer) {
        analyzer.setProperty(Analyzer.IMPORT_PACKAGE, "*;resolution:=optional");
        String export = analyzer.calculateExportsFromContents(analyzer.getJar());
        analyzer.setProperty(Analyzer.EXPORT_PACKAGE, export);
    }

    private static void setBundleOptions(Analyzer analyzer, ArtifactBundlerInstructions instructions) {
        analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, instructions.getSymbolicNameWithOptions());
        if (analyzer.getProperty(Analyzer.BUNDLE_NAME) == null) {
            // in case name was not set in the instructions
            analyzer.setProperty(Analyzer.BUNDLE_NAME, instructions.getName());
        }
        analyzer.setProperty(Analyzer.BUNDLE_VERSION, instructions.getVersion());
        analyzer.setProperty(TOOL_KEY, TOOL);
    }

    private static void setInstructions(Analyzer analyzer, ArtifactBundlerInstructions instructions) {
        if (!instructions.getInstructions().isEmpty()) {
            analyzer.setProperties(BundleUtils.transformDirectives(instructions.getInstructions()));
        }
    }

    private static Jar getInputJarWithBlankManifest(ArtifactBundlerRequest request) throws Exception {
        File parentFolder = request.getBinaryInputFile().getParentFile();
        File jarBlankManifest = new File(parentFolder, request.getBinaryInputFile().getName() + "." + UUID.randomUUID());
        Jar jar = new Jar(request.getBinaryInputFile());
        try {
            jar.setManifest(new Manifest());
            jar.write(jarBlankManifest);
            return new Jar(jarBlankManifest);
        } finally {
            FileUtils.deleteQuietly(jarBlankManifest);
            // do not close the newly created jar, analyzer will do it
        }
    }

    private static void setManifest(Analyzer analyzer) throws Exception {
        analyzer.mergeManifest(analyzer.getJar().getManifest());
    }

}

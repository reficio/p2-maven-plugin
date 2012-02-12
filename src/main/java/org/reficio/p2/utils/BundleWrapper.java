/**
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
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2012-02-09
 * Time: 9:46 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class BundleWrapper {

    protected BundleUtils bundleUtils;
    private boolean pedantic;

    private Set<Artifact> artifacts;
    private File artifactsDestinationFolder;
    private File bundlesDestinationFolder;

    public BundleWrapper(boolean pedantic) {
        this.bundleUtils = new BundleUtils();
        this.pedantic = pedantic;
    }

    public boolean execute(Set<Artifact> artifacts, File artifactsDestinationFolder, File bundlesDestinationFolder) throws Exception {
        this.artifacts = artifacts;
        this.artifactsDestinationFolder = artifactsDestinationFolder;
        this.bundlesDestinationFolder = bundlesDestinationFolder;

        fetchArtifacts();
        if (hasArtifactsStateChanged()) {
            wrapArtifacts();
            return true;
        }
        return false;
    }

    private void fetchArtifacts() throws IOException {
        artifactsDestinationFolder.mkdirs();
        for (Artifact artifact : artifacts) {
            File unwrappedArtifact = new File(artifactsDestinationFolder, artifact.getFile().getName());
            if (unwrappedArtifact.exists() == false) {
                FileUtils.copyFile(artifact.getFile(), unwrappedArtifact);
            }
        }
    }

    private boolean hasArtifactsStateChanged() {
        for (Artifact artifact : artifacts) {
            File wrappedArtifact = new File(bundlesDestinationFolder, artifact.getFile().getName());
            if (wrappedArtifact.exists() == false) {
                return true;
            }
        }
        return false;
    }

    private void wrapArtifacts() throws Exception {
        bundlesDestinationFolder.mkdirs();
        for (File unwrappedFile : artifactsDestinationFolder.listFiles()) {
            File wrappedFile = new File(bundlesDestinationFolder, unwrappedFile.getName());
            if (wrappedFile.exists()) {
                continue;
            }
            doWrap(unwrappedFile, wrappedFile);
        }
    }

    private void doWrap(File inputFile, File outputFile) throws Exception {
        if (isAlreadyBundle(inputFile)) {
            handleBundleJarWrap(inputFile, outputFile);
        } else {
            handleVanillaJarWrap(inputFile, outputFile);
        }
    }

    private boolean isAlreadyBundle(File inputJarFile) throws IOException {
        Jar inputJar = new Jar(inputJarFile);
        return bundleUtils.isBundle(inputJar);
    }

    private void handleBundleJarWrap(File inputFile, File outputFile) throws IOException {
        FileUtils.copyFile(inputFile, outputFile);
    }

    private void handleVanillaJarWrap(File inputFile, File outputFile) throws Exception {
        Analyzer analyzer = initializeAnalyzer(inputFile);
        try {
            analyzer.calcManifest();
            Jar jar = analyzer.getJar();
            jar.write(outputFile);
            jar.close();
            bundleUtils.reportErrors(analyzer);
        } finally {
            analyzer.close();
        }
    }

    private Analyzer initializeAnalyzer(File fileToWrap) throws IOException {
        Analyzer analyzer = instantiateAnalyzer(fileToWrap);
        setAnalyzerOptions(analyzer);
        setPackageOptions(analyzer);
        setBundleOptions(fileToWrap, analyzer);
        setManifest(analyzer);
        return analyzer;
    }

    private Analyzer instantiateAnalyzer(File fileToWrap) throws IOException {
        Analyzer analyzer = new Analyzer();
        analyzer.setJar(fileToWrap);
        return analyzer;
    }

    private void setAnalyzerOptions(Analyzer analyzer) {
        analyzer.setPedantic(pedantic);
    }

    private void setPackageOptions(Analyzer analyzer) {
        analyzer.setProperty(Analyzer.IMPORT_PACKAGE, "*");
        String export = analyzer.calculateExportsFromContents(analyzer.getJar());
        analyzer.setProperty(Analyzer.EXPORT_PACKAGE, export);
    }

    private void setBundleOptions(File fileToWrap, Analyzer analyzer) {
        Artifact artifact = getArtifact(fileToWrap.getName());
        String version = bundleUtils.getBundleVersion(artifact);
        analyzer.setProperty(Analyzer.BUNDLE_VERSION, version);
        String symbolicName = bundleUtils.getBundleSymbolicName(artifact);
        analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, symbolicName);
    }

    private Artifact getArtifact(String jarName) {
        for (Artifact artifact : artifacts) {
            if (artifact.getFile().getName().equals(jarName)) {
                return artifact;
            }
        }
        throw new RuntimeException("Artifact not found");
    }

    private void setManifest(Analyzer analyzer) throws IOException {
        analyzer.mergeManifest(analyzer.getJar().getManifest());
    }

}

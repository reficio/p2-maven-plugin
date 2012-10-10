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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.logging.Log;
import org.reficio.p2.P2Artifact;
import org.reficio.p2.log.Logger;
import org.sonatype.aether.artifact.Artifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
public class BundleWrapper {

    private static final Log log = Logger.getLog();
    private static final String ECLIPSE_SOURCE_BUNDLE = "Eclipse-SourceBundle";
    private static final String MANIFEST_VERSION = "Manifest-Version";
    private static final String IMPLENTATION_TITLE = "Implementation-Title";
    private static final String SPECIFICATION_TITLE = "Specification-Title";
    private static final String TOOL_KEY = "Tool";
    private static final String TOOL = "p2-maven-plugin (reficio.org)";


    protected final BundleUtils bundleUtils;
    private final boolean pedantic;
    private final File bundlesDestinationFolder;

    public BundleWrapper(boolean pedantic, File bundlesDestinationFolder) {
        this.bundleUtils = new BundleUtils();
        this.pedantic = pedantic;
        this.bundlesDestinationFolder = bundlesDestinationFolder;
    }

    public void execute(P2Artifact artifact) throws Exception {
        wrapArtifacts(artifact);
    }

    private void wrapArtifacts(P2Artifact p2Artifact) throws Exception {
        validateConfig(p2Artifact);
        for (ResolvedArtifact artifact : p2Artifact.getArtifacts()) {
            File wrappedArtifact = new File(bundlesDestinationFolder, artifact.getArtifact().getFile().getName());
            doWrap(p2Artifact, artifact.getArtifact().getFile(), wrappedArtifact);
            doSourceWrap(artifact);
        }
    }

    private void validateConfig(P2Artifact p2Artifact) {
        if (p2Artifact.shouldOverrideManifest() && p2Artifact.shouldIncludeTransitive()) {
            String message = "[%s] <override>true</override> cannot be specified together with <transitive>true</transitive> ";
            message += "If you want to override the original MANIFEST.MF file you have to exclude the transitive dependencies (and add them separately if you wish). ";
            message += "It's not recommended, however, to override the original MANIFEST.MF file, unless you really know what you are doing! ";
            throw new RuntimeException(String.format(message, p2Artifact.getId()));
        }
        if (p2Artifact.shouldIncludeTransitive() && !p2Artifact.getInstructions().isEmpty()) {
            String message = "Instructions specified together <transitive>true</transitive>; ";
            message += "Make sure to specify proper wildcards export-packages directives.";
            log.warn(String.format("[%s] %s", p2Artifact.getId(), message));
        }
    }

    private void doWrap(P2Artifact p2Artifact, File inputFile, File outputFile) throws Exception {
        if (isAlreadyBundle(inputFile)) {
            if (p2Artifact.shouldOverrideManifest()) {
                handleVanillaJarWrap(p2Artifact, inputFile, outputFile);
            } else {
                handleBundleJarWrap(inputFile, outputFile);
            }
        } else {
            handleVanillaJarWrap(p2Artifact, inputFile, outputFile);
        }
    }

    private boolean isAlreadyBundle(File inputJarFile) throws IOException {
        Jar inputJar = new Jar(inputJarFile);
        return bundleUtils.isBundle(inputJar);
    }

    private void handleBundleJarWrap(File inputFile, File outputFile) throws IOException {
        FileUtils.copyFile(inputFile, outputFile);
    }

    private void handleVanillaJarWrap(P2Artifact p2Artifact, File inputFile, File outputFile) throws Exception {
        Analyzer analyzer = initializeAnalyzer(p2Artifact, inputFile);
        try {
            analyzer.calcManifest();
            Jar jar = analyzer.getJar();
            jar.write(outputFile);
            jar.close();
            bundleUtils.reportErrors(analyzer);
        } finally {
            analyzer.close();
            unsignJar(outputFile);
        }
    }

    private void unsignJar(File jarToUnsign) {
        try {
            File unsignedJar = new File(jarToUnsign.getParent(), jarToUnsign.getName() + ".tmp");
            log.debug("Unsign Jar: " + jarToUnsign.getName());
            unsignedJar.createNewFile();
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(unsignedJar));
            ZipFile zip = new ZipFile(jarToUnsign);
            for (Enumeration list = zip.entries(); list.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) list.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".SF")) {
                    continue;
                }
                zipOutputStream.putNextEntry(entry);
                IOUtils.copy(zip.getInputStream(entry), zipOutputStream);
            }
            zipOutputStream.close();
            FileUtils.copyFile(unsignedJar, jarToUnsign);
            unsignedJar.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Analyzer initializeAnalyzer(P2Artifact p2Artifact, File fileToWrap) throws IOException {
        Analyzer analyzer = instantiateAnalyzer(fileToWrap);
        setAnalyzerOptions(analyzer);
        setPackageOptions(analyzer);
        setBundleOptions(analyzer, p2Artifact, fileToWrap);
        setManifest(analyzer);
        setInstructions(analyzer, p2Artifact);
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
        analyzer.setProperty(Analyzer.IMPORT_PACKAGE, "*;resolution:=optional");
        String export = analyzer.calculateExportsFromContents(analyzer.getJar());
        analyzer.setProperty(Analyzer.EXPORT_PACKAGE, export);
    }

    private void setBundleOptions(Analyzer analyzer, P2Artifact p2Artifact, File fileToWrap) {
        Artifact artifact = getArtifact(fileToWrap.getName(), p2Artifact.getArtifacts());
        String version = bundleUtils.calculateBundleVersion(artifact);
        analyzer.setProperty(Analyzer.BUNDLE_VERSION, version);
        String symbolicName = bundleUtils.calculateBundleSymbolicName(artifact);
        analyzer.setProperty(Analyzer.BUNDLE_SYMBOLICNAME, symbolicName);
        analyzer.setProperty(TOOL_KEY, TOOL);
    }

    private void setInstructions(Analyzer analyzer, P2Artifact p2Artifact) {
        if (!p2Artifact.getInstructions().isEmpty()) {
            analyzer.setProperties(BundleUtils.transformDirectives(p2Artifact.getInstructions()));
        }
    }

    private Artifact getArtifact(String jarName, List<ResolvedArtifact> artifacts) {
        for (ResolvedArtifact artifact : artifacts) {
            if (artifact.getArtifact().getFile().getName().equals(jarName)) {
                return artifact.getArtifact();
            }
        }
        throw new RuntimeException(String.format("Internal error - artifact [%s] not found", jarName));
    }

    private void setManifest(Analyzer analyzer) throws IOException {
        analyzer.mergeManifest(analyzer.getJar().getManifest());
    }

    private void doSourceWrap(ResolvedArtifact resolvedArtifact) throws Exception {
        if (resolvedArtifact.getSourceArtifact() == null) {
            return;
        }
        File wrappedSource = new File(bundlesDestinationFolder, resolvedArtifact.getSourceArtifact().getFile().getName());
        String symbolicName = getSymbolicNameForSourceBundle(resolvedArtifact);
        String version = getVersionForSourceBundle(resolvedArtifact);
        String name = getNameForSourceBundle(resolvedArtifact, symbolicName);
        Jar jar = new Jar(resolvedArtifact.getSourceArtifact().getFile());
        Manifest manifest = getManifest(jar);
        decorateSourceManifest(manifest, name, symbolicName, version);
        jar.setManifest(manifest);
        jar.write(wrappedSource);
        jar.close();
    }


    private String getSymbolicNameForSourceBundle(ResolvedArtifact resolvedArtifact) throws IOException {
        String symbolicName = bundleUtils.getBundleSymbolicName(new Jar(resolvedArtifact.getArtifact().getFile()));
        if (symbolicName == null) {
            symbolicName = bundleUtils.calculateBundleSymbolicName(resolvedArtifact.getArtifact());
        }
        return symbolicName;
    }

    private String getVersionForSourceBundle(ResolvedArtifact resolvedArtifact) throws IOException {
        String version = bundleUtils.getBundleVersion(new Jar(resolvedArtifact.getArtifact().getFile()));
        if (version == null) {
            version = bundleUtils.calculateBundleVersion(resolvedArtifact.getArtifact());
        }
        return version;
    }

    private String getNameForSourceBundle(ResolvedArtifact resolvedArtifact, String symbolicName) throws IOException {
        String name = bundleUtils.getBundleName(new Jar(resolvedArtifact.getArtifact().getFile()));
        if (name == null) {
            name = symbolicName + ".source";
        } else {
            if (name.matches(".*\\s+.*")) {
                name += " ";
            }
            if (name.matches(".*[A-Z].*")) {
                name += "Source";
            } else {
                name += "source";
            }
        }
        return name;
    }

    private Manifest getManifest(Jar jar) throws IOException {
        Manifest manifest = jar.getManifest();
        if (manifest == null) {
            manifest = new Manifest();
        }
        return manifest;
    }

    private void decorateSourceManifest(Manifest manifest, String name, String symbolicName, String version) {
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue(Analyzer.BUNDLE_SYMBOLICNAME, symbolicName + ".source");
        attributes.putValue(ECLIPSE_SOURCE_BUNDLE, symbolicName + ";version=\"" + version + "\";roots:=\".\"");
        attributes.putValue(Analyzer.BUNDLE_VERSION, version);
        attributes.putValue(Analyzer.BUNDLE_LOCALIZATION, "plugin");
        attributes.putValue(MANIFEST_VERSION, "1.0");
        attributes.putValue(Analyzer.BUNDLE_MANIFESTVERSION, "2");
        attributes.putValue(Analyzer.BUNDLE_NAME, name);
        attributes.putValue(IMPLENTATION_TITLE, name);
        attributes.putValue(SPECIFICATION_TITLE, name);
        attributes.putValue(TOOL_KEY, TOOL);
    }

}

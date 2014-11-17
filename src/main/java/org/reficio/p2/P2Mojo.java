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

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.reficio.p2.bundler.ArtifactBundler;
import org.reficio.p2.bundler.ArtifactBundlerInstructions;
import org.reficio.p2.bundler.ArtifactBundlerRequest;
import org.reficio.p2.bundler.impl.AquteBundler;
import org.reficio.p2.logger.Logger;
import org.reficio.p2.publisher.BundlePublisher;
import org.reficio.p2.publisher.CategoryPublisher;
import org.reficio.p2.resolver.ArtifactResolutionRequest;
import org.reficio.p2.resolver.ArtifactResolutionResult;
import org.reficio.p2.resolver.ArtifactResolver;
import org.reficio.p2.resolver.ResolvedArtifact;
import org.reficio.p2.resolver.impl.AetherResolver;
import org.reficio.p2.utils.JarUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Main plugin class
 *
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
@Mojo(
		name = "site",
		defaultPhase=LifecyclePhase.COMPILE,
		requiresDependencyResolution=ResolutionScope.RUNTIME,
		requiresDependencyCollection=ResolutionScope.RUNTIME
)
public class P2Mojo extends AbstractMojo implements Contextualizable {

    private static final String BUNDLES_TOP_FOLDER = "/source";
    private static final String FEATURES_DESTINATION_FOLDER = BUNDLES_TOP_FOLDER + "/features";
    private static final String BUNDLES_DESTINATION_FOLDER = BUNDLES_TOP_FOLDER + "/plugins";
    private static final String DEFAULT_CATEGORY_FILE = "category.xml";
    private static final String DEFAULT_CATEGORY_CLASSPATH_LOCATION = "/";

    @Parameter(defaultValue="${project}", required=true, readonly=true)
    private MavenProject project;

    @Parameter(defaultValue="${session}", required=true, readonly=true)
    private MavenSession session;

    /**
     * @component
     * @required
     */
    @Component
    @Requirement
    private BuildPluginManager pluginManager;

    @Parameter(defaultValue="${project.build.directory}", required=true)
    private String buildDirectory;

    @Parameter(defaultValue="${project.build.directory}/repository", required=true)
    private String destinationDirectory;

    /**
     * @component
     * @required
     */
    @Component
    @Requirement
    private EquinoxServiceFactory p2;

    /**
     * @component
     * @required
     */
    @Component
    @Requirement
    private P2ApplicationLauncher launcher;


    /**
     * Specifies a file containing category definitions.
     *
     */
    @Parameter(defaultValue="")
    private String categoryFileURL;

    /**
     * Optional line of additional arguments passed to the p2 application launcher.
     *
     */
    @Parameter(defaultValue="false")
    private boolean pedantic;

    /**
     * Specifies whether to compress generated update site.
     *
     */
    @Parameter(defaultValue="true")
    private boolean compressSite;

    /**
     * Kill the forked process after a certain number of seconds. If set to 0, wait forever for the
     * process, never timing out.
     *
     */
    @Parameter(defaultValue="0", alias="p2.timeout")
    private int forkedProcessTimeoutInSeconds;

    /**
     * Specifies additional arguments to p2Launcher, for example -consoleLog -debug -verbose
     *
     * @parameter default-value=""
     */
    @Parameter(defaultValue="")
    private String additionalArgs;

    /**
     * Dependency injection container - used to get some components programatically
     */
    private PlexusContainer container;

    /**
     * Aether Repository System
     * Declared as raw Object type as different objects are injected in different Maven versions:
     * * 3.0.0 and above -> org.sonatype.aether...
     * * 3.1.0 and above -> org.eclipse.aether...
     */
    private Object repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     */
    @Parameter(defaultValue="${repositorySystemSession}", readonly=true, required=true)
    private Object repoSession;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @required
     * @readonly
     */
    @Parameter(defaultValue="${project.remoteProjectRepositories}", readonly=true, required=true)
    private List<Object> projectRepos;

    /**
     */
    @Parameter(readonly=true)
    private List<P2Artifact> artifacts;

    /**
     * A list of artifacts that define eclipse features
     * 
     */
    @Parameter(readonly=true)
    private List<P2Artifact> features;
    
    /**
     * Logger retrieved from the Maven internals.
     * It's the recommended way to do it...
     */
    private Log log = getLog();

    /**
     * Folder which the jar files bundled by the ArtifactBundler will be copied to
     */
    private File bundlesDestinationFolder;

    /**
     * Folder which the feature jar files bundled by the ArtifactBundler will be copied to
     */
    private File featuresDestinationFolder;
    
    /**
     * Processing entry point.
     * Method that orchestrates the execution of the plugin.
     */
    @Override
    public void execute() {
        try {
            initializeEnvironment();
            initializeRepositorySystem();
            processArtifacts();
            processFeatures();
            executeP2PublisherPlugin();
            executeCategoryPublisher();
            cleanupEnvironment();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeEnvironment() throws IOException {
        Logger.initialize(log);
        bundlesDestinationFolder = new File(buildDirectory, BUNDLES_DESTINATION_FOLDER);
        featuresDestinationFolder = new File(buildDirectory, FEATURES_DESTINATION_FOLDER);
        FileUtils.deleteDirectory(new File(buildDirectory, BUNDLES_TOP_FOLDER));
        bundlesDestinationFolder.mkdirs();
        featuresDestinationFolder.mkdirs();

    }

    private void initializeRepositorySystem() {
        if (repoSystem == null) {
            repoSystem = lookup("org.eclipse.aether.RepositorySystem");
        }
        if (repoSystem == null) {
            repoSystem = lookup("org.sonatype.aether.RepositorySystem");
        }
        Preconditions.checkNotNull(repoSystem, "Could not initialize RepositorySystem");
    }

    private Object lookup(String role) {
        try {
            return container.lookup(role);
        } catch (ComponentLookupException ex) {
        }
        return null;
    }

    private void processArtifacts() {
        // first resolve all artifacts
        Multimap<P2Artifact, ResolvedArtifact> resolvedArtifacts = resolveArtifacts();
        // then bundle the artifacts including the transitive dependencies (if specified so)
        if (null!=artifacts) {
	        for (P2Artifact p2Artifact : artifacts) {
	            for (ResolvedArtifact resolvedArtifact : resolvedArtifacts.get(p2Artifact)) {
	                bundleArtifact(p2Artifact, resolvedArtifact);
	            }
	        }
        }
    }

    private void processFeatures() {
        // artifacts should already have been resolved by processArtifacts()
        Multimap<P2Artifact, ResolvedArtifact> resolvedFeatures = resolveFeatures();
        // then bundle the artifacts including the transitive dependencies (if specified so)
        log.info("Resolved "+resolvedFeatures.size()+ "features");
        if (null!=features) {
	        for (P2Artifact p2Artifact : features) {
	            for (ResolvedArtifact resolvedArtifact : resolvedFeatures.get(p2Artifact)) {
	                handleFeature(p2Artifact, resolvedArtifact);
	            }
	        }
        }
    }
    
    private Multimap<P2Artifact, ResolvedArtifact> resolveArtifacts() {
        Multimap<P2Artifact, ResolvedArtifact> resolvedArtifacts = ArrayListMultimap.create();
        if (null!=artifacts) {
	        for (P2Artifact p2Artifact : artifacts) {
	            logResolving(p2Artifact);
	            ArtifactResolutionResult resolutionResult = resolveArtifact(p2Artifact);
	            resolvedArtifacts.putAll(p2Artifact, resolutionResult.getResolvedArtifacts());
	        }
        }
        return resolvedArtifacts;
    }

    private Multimap<P2Artifact, ResolvedArtifact> resolveFeatures() {
        Multimap<P2Artifact, ResolvedArtifact> resolvedArtifacts = ArrayListMultimap.create();
        if (null!=features) {
	        for (P2Artifact p2Artifact : features) {
	            logResolving(p2Artifact);
	            ArtifactResolutionResult resolutionResult = resolveArtifact(p2Artifact);
	            resolvedArtifacts.putAll(p2Artifact, resolutionResult.getResolvedArtifacts());
	        }
        }
        return resolvedArtifacts;
    }
    
    private void logResolving(P2Artifact p2) {
        log.info(String.format("Resolving artifact=[%s] transitive=[%s] source=[%s]", p2.getId(), p2.shouldIncludeTransitive(),
                p2.shouldIncludeSources()));
    }

    private ArtifactResolutionResult resolveArtifact(P2Artifact p2Artifact) {
        ArtifactResolutionRequest resolutionRequest = ArtifactResolutionRequest.builder()
                .rootArtifactId(p2Artifact.getId())
                .resolveSource(p2Artifact.shouldIncludeSources())
                .resolveTransitive(p2Artifact.shouldIncludeTransitive())
                .excludes(p2Artifact.getExcludes())
                .build();
        ArtifactResolutionResult resolutionResult = getArtifactResolver().resolve(resolutionRequest);
        logResolved(resolutionRequest, resolutionResult);
        return resolutionResult;
    }

    private ArtifactResolver getArtifactResolver() {
        return new AetherResolver(repoSystem, repoSession, projectRepos);
    }

    private void logResolved(ArtifactResolutionRequest resolutionRequest, ArtifactResolutionResult resolutionResult) {
        for (ResolvedArtifact resolvedArtifact : resolutionResult.getResolvedArtifacts()) {
            log.info("\t [JAR] " + resolvedArtifact.getArtifact());
            if (resolvedArtifact.getSourceArtifact() != null) {
                log.info("\t [SRC] " + resolvedArtifact.getSourceArtifact().toString());
            } else if (resolutionRequest.isResolveSource()) {
                log.warn("\t [SRC] Failed to resolve source for artifact " + resolvedArtifact.getArtifact().toString());
            }
        }
    }

    private void handleFeature(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
    	log.debug("Handling feature "+p2Artifact.getId());
        ArtifactBundlerRequest bundlerRequest = P2Helper.createBundlerRequest(p2Artifact, resolvedArtifact, featuresDestinationFolder);
    	try {
    		File inputFile = bundlerRequest.getBinaryInputFile();
    		File outputFile = bundlerRequest.getBinaryOutputFile();
    		//This will also copy the input to the output
   			JarUtils.adjustFeatureQualifierVersionWithTimestamp(inputFile, outputFile);
	    	log.info("Copied "+inputFile+ " to "+outputFile);
        } catch (Exception ex) {
            throw new RuntimeException("Error while bundling jar or source: " + bundlerRequest.getBinaryInputFile().getName(), ex);
        }
    }
    
    private void bundleArtifact(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
        P2Validator.validateBundleRequest(p2Artifact, resolvedArtifact);
        ArtifactBundler bundler = getArtifactBundler();
        ArtifactBundlerInstructions bundlerInstructions = P2Helper.createBundlerInstructions(p2Artifact, resolvedArtifact);
        ArtifactBundlerRequest bundlerRequest = P2Helper.createBundlerRequest(p2Artifact, resolvedArtifact, bundlesDestinationFolder);
        bundler.execute(bundlerRequest, bundlerInstructions);
    }

    private ArtifactBundler getArtifactBundler() {
        return new AquteBundler(pedantic);
    }

    private void executeP2PublisherPlugin() throws IOException, MojoExecutionException {
        prepareDestinationDirectory();
        BundlePublisher publisher = BundlePublisher.builder()
                .mavenProject(project)
                .mavenSession(session)
                .buildPluginManager(pluginManager)
                .compressSite(compressSite)
                .additionalArgs(additionalArgs)
                .build();
        publisher.execute();
    }

    private void prepareDestinationDirectory() throws IOException {
        FileUtils.deleteDirectory(new File(destinationDirectory));
    }

    private void executeCategoryPublisher() throws AbstractMojoExecutionException, IOException {
        prepareCategoryLocationFile();
        CategoryPublisher publisher = CategoryPublisher.builder()
                .p2ApplicationLauncher(launcher)
                .additionalArgs(additionalArgs)
                .forkedProcessTimeoutInSeconds(forkedProcessTimeoutInSeconds)
                .categoryFileLocation(categoryFileURL)
                .metadataRepositoryLocation(destinationDirectory)
                .build();
        publisher.execute();
    }

    private void prepareCategoryLocationFile() throws IOException {
        if (StringUtils.isBlank(categoryFileURL)) {
            InputStream is = getClass().getResourceAsStream(DEFAULT_CATEGORY_CLASSPATH_LOCATION + DEFAULT_CATEGORY_FILE);
            File destinationFolder = new File(destinationDirectory);
            destinationFolder.mkdirs();
            File categoryDefinitionFile = new File(destinationFolder, DEFAULT_CATEGORY_FILE);
            FileWriter writer = new FileWriter(categoryDefinitionFile);
            IOUtils.copy(is, writer, "UTF-8");
            IOUtils.closeQuietly(writer);
            categoryFileURL = categoryDefinitionFile.getAbsolutePath();
        }
    }

    private void cleanupEnvironment() throws IOException {
        File workFolder = new File(buildDirectory, BUNDLES_TOP_FOLDER);
        try {
            FileUtils.deleteDirectory(workFolder);
        } catch (IOException ex) {
            log.warn("Cannot cleanup the work folder " + workFolder.getAbsolutePath());
        }
    }

    @Override
    public void contextualize(Context context) throws ContextException {
        this.container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

}

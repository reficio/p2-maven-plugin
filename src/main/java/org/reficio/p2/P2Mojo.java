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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.reficio.p2.log.Logger;
import org.reficio.p2.repo.Artifact;
import org.reficio.p2.repo.ArtifactResolver;
import org.reficio.p2.repo.aether.AetherResolver;
import org.reficio.p2.utils.BundleWrapper;
import org.reficio.p2.utils.CategoryPublisher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @goal site
 * @phase compile
 * @requiresDependencyResolution
 * @requiresDependencyCollection
 * @since 1.1.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
public class P2Mojo extends AbstractMojo implements Contextualizable {

    private static final String TYCHO_VERSION = "0.18.1";

    private static final String BUNDLES_TOP_FOLDER = "/source";
    private static final String BUNDLES_DESTINATION_FOLDER = BUNDLES_TOP_FOLDER + "/plugins";
    private static final String DEFAULT_CATEGORY_FILE = "category.xml";
    private static final String DEFAULT_CATEGORY_CLASSPATH_LOCATION = "/";

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession session;

    /**
     * @component
     * @required
     */
    protected BuildPluginManager pluginManager;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected String buildDirectory;

    /**
     * @parameter expression="${project.build.directory}/repository"
     * @required
     */
    private String destinationDirectory;

    /**
     * @component
     * @required
     */
    private EquinoxServiceFactory p2;

    /**
     * @component
     * @required
     */
    private P2ApplicationLauncher launcher;


    /**
     * Specifies a file containing category definitions.
     *
     * @parameter default-value=""
     */
    private String categoryFileURL;

    /**
     * Optional line of additional arguments passed to the p2 application launcher.
     *
     * @parameter default-value="false"
     */
    private boolean pedantic;

    /**
     * Specifies whether to compress generated update site.
     *
     * @parameter default-value="true"
     */
    private boolean compressSite;

    /**
     * Kill the forked process after a certain number of seconds. If set to 0, wait forever for the
     * process, never timing out.
     *
     * @parameter expression="${p2.timeout}" default-value="0"
     */
    private int forkedProcessTimeoutInSeconds;

    /**
     * Specifies additional arguments to p2Launcher, for example -consoleLog -debug -verbose
     *
     * @parameter default-value=""
     */
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
     * @parameter default-value="${repositorySystemSession}"
     * @required
     * @readonly
     */
    private Object repoSession;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @required
     * @readonly
     */
    private List<Object> projectRepos;

    /**
     * @parameter
     * @required
     * @readonly
     */
    private List<P2Artifact> artifacts;

    protected Log log = getLog();

    private File bundlesDestinationFolder;

    public void execute() {
        try {
            initializeEnvironment();
            initializeRepositorySystem();
            resolveArtifacts();
            executeBndWrapper();
            executeP2PublisherPlugin();
            executeCategoryPublisher();
            cleanupEnvironment();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeEnvironment() throws IOException {
        Logger.initialize(getLog());
        bundlesDestinationFolder = new File(buildDirectory, BUNDLES_DESTINATION_FOLDER);
        FileUtils.deleteDirectory(new File(buildDirectory, BUNDLES_TOP_FOLDER));
        bundlesDestinationFolder.mkdirs();
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

    public void resolveArtifacts() {
        ArtifactResolver resolver = getArtifactResolver();
        for (P2Artifact p2Artifact : artifacts) {
            log.info("Processing artifacts for " + p2Artifact.getId());
            List<Artifact> result = resolver.resolve(p2Artifact.getId(), p2Artifact.getExcludes(), !p2Artifact.shouldIncludeTransitive());
            for (Artifact resolved : result) {
                log.info("\t [JAR] " + resolved.toString());
                Artifact resolvedSource = resolveSource(p2Artifact, resolver, resolved);
                if (resolvedSource != null) {
                    log.info("\t [SRC] " + resolvedSource.toString());
                }
                p2Artifact.addResolvedArtifact(resolved, resolvedSource);
            }
        }
    }

    private ArtifactResolver getArtifactResolver() {
        return new AetherResolver(repoSystem, repoSession, projectRepos);
    }

    public Artifact resolveSource(P2Artifact p2Artifact, ArtifactResolver resolver, Artifact artifact) {
        Artifact resolvedSource = null;
        if (p2Artifact.shouldIncludeSources()) {
            try {
                resolvedSource = resolver.resolveSource(artifact);
            } catch (Exception ex) {
                log.warn("\t [SRC] Failed to resolve source for artifact " + artifact.toString());
            }
        }
        return resolvedSource;
    }

    protected void executeBndWrapper() throws Exception {
        BundleWrapper wrapper = new BundleWrapper(pedantic, bundlesDestinationFolder);
        for (P2Artifact artifact : artifacts) {
            wrapper.execute(artifact);
        }
    }

    protected void executeP2PublisherPlugin() throws MojoExecutionException, IOException {
        File repositoryDirectory = new File(destinationDirectory);
        FileUtils.deleteDirectory(repositoryDirectory);
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho.extras"),
                        artifactId("tycho-p2-extras-plugin"),
                        version(TYCHO_VERSION)
                ),
                goal("publish-features-and-bundles"),
                configuration(
                        element(name("compress"), Boolean.toString(compressSite)),
                        element(name("additionalArgs"), additionalArgs)
                ),
                executionEnvironment(
                        project,
                        session,
                        pluginManager
                )
        );
    }

    private void executeCategoryPublisher() throws AbstractMojoExecutionException, IOException {
        prepareCategoryLocationFile();
        CategoryPublisher publisher = CategoryPublisher.factory()
                .p2ApplicationLauncher(launcher)
                .additionalArgs(additionalArgs)
                .forkedProcessTimeoutInSeconds(forkedProcessTimeoutInSeconds)
                .create();
        publisher.execute(categoryFileURL, destinationDirectory);
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
            getLog().warn("Cannot cleanup the work folder " + workFolder.getAbsolutePath());
        }
    }

    @Override
    public void contextualize(Context context) throws ContextException {
        this.container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
    }

}

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
package org.reficio.p2;

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
import org.eclipse.sisu.equinox.EquinoxServiceFactory;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.reficio.p2.utils.ArtifactResolver;
import org.reficio.p2.utils.BundleWrapper;
import org.reficio.p2.utils.CategoryPublisher;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;


/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2012-02-09
 * Time: 9:46 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 *
 * @goal site
 * @phase compile
 * @requiresDependencyResolution
 * @requiresDependencyCollection
 */
public class P2Mojo extends AbstractMojo {

    private static final String BUNDLES_DESTINATION_FOLDER = "/source/plugins";
    private static final String VANILLA_DESTINATION_FOLDER = "/jars";
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

    // private TargetPlatformBuilder platformBuilder;

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
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> projectRepos;

    /**
     * @parameter default-value=""
     */
    private List<String> artifacts;

    protected Log log = getLog();



    public void execute() {
        try {
            Set<Artifact> resolvedArtifacts = resolveArtifacts(artifacts);
            boolean executionProceeded = executeBndWrapper(resolvedArtifacts);
            if (executionProceeded == false) {
                return;
            }
            executeP2PublisherPlugin();
            executeCategoryPublisher();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Artifact> resolveArtifacts(List<String> artifacts) throws RepositoryException {
        ArtifactResolver resolver = new ArtifactResolver(repoSystem, repoSession, projectRepos);
        Set<Artifact> resolvedArtifacts = new HashSet<Artifact>();
        for(String artifact : artifacts) {
            resolvedArtifacts.addAll(resolver.resolve(artifact));
        }
        return resolvedArtifacts;
    }

    protected boolean executeBndWrapper(Set<Artifact> artifactsToWrap) throws Exception {
        File bundlesDestinationFolder = new File(buildDirectory, BUNDLES_DESTINATION_FOLDER);
        File artifactsDestinationFolder = new File(buildDirectory, VANILLA_DESTINATION_FOLDER);
        bundlesDestinationFolder.mkdirs();
        artifactsDestinationFolder.mkdirs();
        BundleWrapper wrapper = new BundleWrapper(pedantic);
        return wrapper.execute(artifactsToWrap, artifactsDestinationFolder, bundlesDestinationFolder);
    }

    protected void executeP2PublisherPlugin() throws MojoExecutionException, IOException {
        File repositoryDirectory = new File(destinationDirectory);
        FileUtils.deleteDirectory(repositoryDirectory);
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho.extras"),
                        artifactId("tycho-p2-extras-plugin"),
                        version("0.14.0")
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

}

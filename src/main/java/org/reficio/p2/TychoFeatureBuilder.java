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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.FileSet;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.shared.BuildProperties;
import org.eclipse.tycho.core.osgitools.DefaultReactorProject;
import org.eclipse.tycho.core.osgitools.targetplatform.DefaultDependencyArtifacts;
import org.eclipse.tycho.core.utils.MavenSessionUtils;
import org.eclipse.tycho.model.Feature;
import org.eclipse.tycho.packaging.LicenseFeatureHelper;
import org.reficio.p2.publisher.BundlePublisher;
import org.reficio.p2.publisher.BundlePublisher.Builder;
import org.twdata.maven.mojoexecutor.MojoExecutor;

public class TychoFeatureBuilder {
    private static final String TYCHO_VERSION = "0.23.1";

    File featureFile;
    private final String outputDirectory;
    String featureId;
    String featureVersion;
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final BuildPluginManager buildPluginManager;

    public TychoFeatureBuilder(File featureFile, String outputDirectory, String featureId, String featureVersion,
                           MavenProject mavenProject, MavenSession mavenSession, BuildPluginManager buildPluginManager) {
        this.featureFile = featureFile;
        this.outputDirectory = outputDirectory;
        this.featureId = featureId;
        this.featureVersion = featureVersion;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.buildPluginManager = buildPluginManager;
    }

    public void execute() throws MojoExecutionException, IOException {
    	MavenProject mp =new MavenProject();
    	mp.getModel().setGroupId(this.mavenProject.getGroupId());
    	mp.getModel().setArtifactId(featureId);
    	mp.getModel().setVersion(featureVersion);
    	mp.getModel().setPackaging("eclipse-feature");
    	mp.setPluginArtifactRepositories(this.mavenProject.getPluginArtifactRepositories());
    	mp.setFile(featureFile); //sets the basedir for the MavenProject
    	org.eclipse.tycho.artifacts.DependencyArtifacts da = new DefaultDependencyArtifacts();
    	mp.setContextValue(TychoConstants.CTX_DEPENDENCY_ARTIFACTS, da);
    	mavenSession.setCurrentProject(mp);
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho"),
                        artifactId("tycho-packaging-plugin"),
                        version(TYCHO_VERSION)
                ),
                goal("package-feature"),
                configuration(
                        element(name("finalName"), this.featureId+"_"+this.featureVersion+".jar"),
                        element(name("basedir"), this.featureFile.getParent()),
                        element(name("outputDirectory"), outputDirectory)
                ),
                executionEnvironment(
                        mp,
                        mavenSession,
                        buildPluginManager
                )
        );

    }

}

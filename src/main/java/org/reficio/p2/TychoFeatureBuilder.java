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

import java.io.File;
import java.io.IOException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.core.TychoConstants;
import org.eclipse.tycho.core.osgitools.targetplatform.DefaultDependencyArtifacts;
import org.reficio.p2.utils.Utils;

public class TychoFeatureBuilder {

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

    private MavenProject prepareMavenProject() {
    	MavenProject mp =new MavenProject();
    	mp.getModel().setGroupId(this.mavenProject.getGroupId());
    	mp.getModel().setArtifactId(featureId);
    	mp.getModel().setVersion(featureVersion);
    	mp.getModel().setPackaging("eclipse-feature");
    	mp.setPluginArtifactRepositories(this.mavenProject.getPluginArtifactRepositories());
    	mp.setFile(featureFile); //sets the basedir for the MavenProject
    	org.eclipse.tycho.artifacts.DependencyArtifacts da = new DefaultDependencyArtifacts();
    	mp.setContextValue(TychoConstants.CTX_DEPENDENCY_ARTIFACTS, da);
    	return mp;
	}

	public void execute() throws MojoExecutionException, IOException {
    	MavenProject mp = prepareMavenProject();
    	mavenSession.setCurrentProject(mp);
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho"),
                        artifactId("tycho-packaging-plugin"),
                        version(Utils.TYCHO_VERSION)
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

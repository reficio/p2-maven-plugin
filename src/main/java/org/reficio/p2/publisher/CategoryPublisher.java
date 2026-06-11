/*
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
package org.reficio.p2.publisher;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;
import org.reficio.p2.utils.Utils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
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

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class CategoryPublisher {

    private final String categoryFileLocation;
    private final String metadataRepositoryLocation;
    private MavenProject mavenProject;
    private MavenSession mavenSession;
    private BuildPluginManager buildPluginManager;

    public CategoryPublisher(String categoryFileLocation, String metadataRepositoryLocation, MavenProject mavenProject, MavenSession mavenSession, BuildPluginManager buildPluginManager) {
        this.categoryFileLocation = categoryFileLocation;
        this.metadataRepositoryLocation = metadataRepositoryLocation;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.buildPluginManager = buildPluginManager;
    }

    public void execute() throws AbstractMojoExecutionException, IOException {
        executeMojo( //
                plugin( //
                        groupId("org.eclipse.tycho"), //
                        artifactId("tycho-p2-plugin"), //
                        version(Utils.tychoVersion()) //
                ), //
                goal("category-p2-metadata"), //
                configuration( //
                        element(name("target"), metadataRepositoryLocation), //
                        element( //
                                name("categoryDefinition"), //
                                categoryFileLocation //
                        ), //
                        element(name("metadataRepositoryName"), "Dependencies aggregated by org.reficio:p2-maven-plugin") //
                ), //
                executionEnvironment( //
                        mavenProject, //
                        mavenSession, //
                        buildPluginManager //
                ));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String categoryFileLocation;
        private String metadataRepositoryLocation;

        private MavenProject mavenProject;
        private MavenSession mavenSession;
        private BuildPluginManager buildPluginManager;


        public Builder mavenProject(MavenProject mavenProject) {
            this.mavenProject = mavenProject;
            return this;
        }

        public Builder mavenSession(MavenSession mavenSession) {
            this.mavenSession = mavenSession;
            return this;
        }

        public Builder buildPluginManager(BuildPluginManager buildPluginManager) {
            this.buildPluginManager = buildPluginManager;
            return this;
        }



        public Builder categoryFileLocation(String categoryFileLocation) {
            requireNonNull(categoryFileLocation, "categoryFileLocation cannot be null");
            this.categoryFileLocation = categoryFileLocation;
            return this;
        }

        public Builder metadataRepositoryLocation(String metadataRepositoryLocation) {
            requireNonNull(metadataRepositoryLocation, "metadataRepositoryLocation cannot be null");
            this.metadataRepositoryLocation = metadataRepositoryLocation;
            return this;
        }

        public CategoryPublisher build() {
            requireNonNull(mavenProject, "mavenProject cannot be null");
            requireNonNull(mavenSession, "mavenSession cannot be null");
            requireNonNull(buildPluginManager, "buildPluginManager cannot be null");
            requireNonNull(categoryFileLocation, "categoryFileLocation cannot be null");
            requireNonNull(metadataRepositoryLocation, "metadataRepositoryLocation cannot be null");
            return new CategoryPublisher(categoryFileLocation,
                    metadataRepositoryLocation, mavenProject, mavenSession , buildPluginManager );
        }



    }

}

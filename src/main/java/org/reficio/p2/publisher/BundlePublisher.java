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
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.reficio.p2.utils.Utils;

import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
@SuppressFBWarnings(value = "EI_EXPOSE_REP2")
public class BundlePublisher {

    private final Boolean compressSite;
    private final String additionalArgs;
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final BuildPluginManager buildPluginManager;

    public BundlePublisher(Boolean compressSite, String additionalArgs,
                           MavenProject mavenProject, MavenSession mavenSession, BuildPluginManager buildPluginManager) {
        this.compressSite = compressSite;
        this.additionalArgs = additionalArgs;
        this.mavenProject = mavenProject;
        this.mavenSession = mavenSession;
        this.buildPluginManager = buildPluginManager;
    }

    public void execute() throws MojoExecutionException, IOException {
        executeMojo(
                plugin(
                        groupId("org.eclipse.tycho.extras"),
                        artifactId("tycho-p2-extras-plugin"),
                        version(Utils.TYCHO_VERSION)
                ),
                goal("publish-features-and-bundles"),
                configuration(
                        element(name("compress"), Boolean.toString(compressSite)),
                        element(name("additionalArgs"), additionalArgs)
                ),
                executionEnvironment(
                        mavenProject,
                        mavenSession,
                        buildPluginManager
                )
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean compressSite;
        private String additionalArgs;
        private MavenProject mavenProject;
        private MavenSession mavenSession;
        private BuildPluginManager buildPluginManager;

        public Builder compressSite(Boolean compressSite) {
            this.compressSite = compressSite;
            return this;
        }

        public Builder additionalArgs(String additionalArgs) {
            this.additionalArgs = additionalArgs;
            return this;
        }

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

        public BundlePublisher build() {
            return new BundlePublisher(compressSite, additionalArgs, requireNonNull(mavenProject),
                    requireNonNull(mavenSession), requireNonNull(buildPluginManager));
        }
    }
}

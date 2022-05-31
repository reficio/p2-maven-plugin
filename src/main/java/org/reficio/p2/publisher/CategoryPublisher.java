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
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;

import java.io.File;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public class CategoryPublisher {

    private static final String CATEGORY_PUBLISHER_APP_NAME = "org.eclipse.equinox.p2.publisher.CategoryPublisher";

    private final P2ApplicationLauncher launcher;
    private final int forkedProcessTimeoutInSeconds;
    private final String[] additionalArgs;
    private final String categoryFileLocation;
    private final String metadataRepositoryLocation;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public CategoryPublisher(P2ApplicationLauncher launcher, int forkedProcessTimeoutInSeconds, String[] additionalArgs,
                             String categoryFileLocation, String metadataRepositoryLocation) {
        this.launcher = launcher;
        this.forkedProcessTimeoutInSeconds = forkedProcessTimeoutInSeconds;
        this.additionalArgs = additionalArgs;
        this.categoryFileLocation = categoryFileLocation;
        this.metadataRepositoryLocation = metadataRepositoryLocation;
    }

    public void execute() throws AbstractMojoExecutionException, IOException {
        configureLauncher(categoryFileLocation, metadataRepositoryLocation);
        executeLauncher();
    }

    private void configureLauncher(String categoryFileLocation, String metadataRepositoryLocation) throws AbstractMojoExecutionException, IOException {
        File metadataRepositoryDir = new File(metadataRepositoryLocation).getCanonicalFile();
        File categoryDefinitionFileSource = new File(categoryFileLocation);
        File categoryDefinitionFileTarget = new File(metadataRepositoryDir, "category.xml");
        FileUtils.copyFile(categoryDefinitionFileSource, categoryDefinitionFileTarget);

        launcher.setWorkingDirectory(metadataRepositoryDir);
        launcher.setApplicationName(CATEGORY_PUBLISHER_APP_NAME);

        launcher.addArguments("-categoryDefinition", "file:/" + new File(categoryDefinitionFileTarget.toURI()).getAbsolutePath());
        launcher.addArguments("-metadataRepository", "file:/" + new File(metadataRepositoryDir.toURI()).getAbsolutePath());
        launcher.addArguments(additionalArgs);
    }

    private void executeLauncher() throws MojoFailureException {
        int result = launcher.execute(forkedProcessTimeoutInSeconds);
        if (result != 0) {
            throw new MojoFailureException("P2 publisher return code was " + result);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private P2ApplicationLauncher launcher;
        private int forkedProcessTimeoutInSeconds = 0;
        private String[] additionalArgs;
        private String categoryFileLocation;
        private String metadataRepositoryLocation;

        public Builder p2ApplicationLauncher(P2ApplicationLauncher launcher) {
            requireNonNull(launcher, "p2ApplicationLauncher cannot be null");
            this.launcher = launcher;
            return this;
        }

        public Builder forkedProcessTimeoutInSeconds(int forkedProcessTimeoutInSeconds) {
            if (forkedProcessTimeoutInSeconds < 0) {
                throw new IllegalArgumentException("forkedProcessTimeoutInSeconds cannot be negative but was: " + forkedProcessTimeoutInSeconds);
            }

            this.forkedProcessTimeoutInSeconds = forkedProcessTimeoutInSeconds;
            return this;
        }

        public Builder additionalArgs(String additionalArgs) {
            try {
                this.additionalArgs = CommandLineUtils.translateCommandline(additionalArgs);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to translate additional arguments into command line array", e);
            }
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
            requireNonNull(launcher, "p2ApplicationLauncher cannot be null");
            requireNonNull(categoryFileLocation, "categoryFileLocation cannot be null");
            requireNonNull(metadataRepositoryLocation, "metadataRepositoryLocation cannot be null");
            return new CategoryPublisher(launcher, forkedProcessTimeoutInSeconds, additionalArgs, categoryFileLocation,
                    metadataRepositoryLocation);
        }

    }

}

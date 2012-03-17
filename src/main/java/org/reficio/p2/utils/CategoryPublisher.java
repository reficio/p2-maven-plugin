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

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
// import org.eclipse.tycho.p2.facade.internal.P2ApplicationLauncher;

import java.io.File;
import java.io.IOException;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2012-02-09
 * Time: 9:46 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class CategoryPublisher {

    private static String CATEGORY_PUBLISHER_APP_NAME = "org.eclipse.equinox.p2.publisher.CategoryPublisher";

    private P2ApplicationLauncher launcher;
    private int forkedProcessTimeoutInSeconds = 0;
    private String[] additionalArgs;

    private CategoryPublisher() {
    }

    public void execute(String categoryFileLocation, String metadataRepositoryLocation) throws AbstractMojoExecutionException, IOException {
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
        launcher.addArguments("-categoryDefinition", categoryDefinitionFileTarget.toURI().toString());
        launcher.addArguments("-metadataRepository", metadataRepositoryDir.toURI().toString());
        launcher.addArguments(additionalArgs);
    }

    private void executeLauncher() throws MojoFailureException {
        int result = launcher.execute(forkedProcessTimeoutInSeconds);
        if (result != 0) {
            throw new MojoFailureException("P2 publisher return code was " + result);
        }
    }

    public static Factory factory() {
        return new Factory();
    }

    public static class Factory {
        private CategoryPublisher publisher;

        private Factory() {
            publisher = new CategoryPublisher();
        }

        public Factory p2ApplicationLauncher(P2ApplicationLauncher launcher) {
            if (launcher == null) {
                throw new IllegalArgumentException("launcher cannot be null");
            }
            publisher.launcher = launcher;
            return this;
        }

        public Factory forkedProcessTimeoutInSeconds(int forkedProcessTimeoutInSeconds) {
            if (forkedProcessTimeoutInSeconds < 0) {
                throw new IllegalArgumentException("forkedProcessTimeoutInSeconds cannot be negative");
            }
            publisher.forkedProcessTimeoutInSeconds = forkedProcessTimeoutInSeconds;
            return this;
        }

        public Factory additionalArgs(String additionalArgs) {
            try {
                publisher.additionalArgs = CommandLineUtils.translateCommandline(additionalArgs);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to translate additional arguments into command line array", e);
            }
            return this;
        }

        public CategoryPublisher create() {
            if (publisher.launcher == null) {
                throw new RuntimeException("p2ApplicationLauncher cannot be null");
            }
            return publisher;
        }
    }
}

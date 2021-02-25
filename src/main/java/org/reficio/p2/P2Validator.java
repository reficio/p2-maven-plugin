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

import org.reficio.p2.logger.Logger;
import org.reficio.p2.resolver.maven.ResolvedArtifact;
import org.reficio.p2.utils.BundleUtils;

import java.util.Locale;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
public class P2Validator {

    public static void validateBundleRequest(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
        validateGeneralConfig(p2Artifact);
        validateArtifactConfig(p2Artifact, resolvedArtifact);
    }

    private static void validateGeneralConfig(P2Artifact p2Artifact) {
        if (p2Artifact.shouldIncludeTransitive() && !p2Artifact.getCombinedInstructions().isEmpty()) {
            String message = String.format(Locale.ENGLISH,"BND instructions are NOT applied to the transitive dependencies of %s",
                    p2Artifact.getId());
            Logger.getLog().warn(message);
        }

        if (p2Artifact.getCombinedInstructions().size() != p2Artifact.getInstructions().size()) {
            for (String propertyName : p2Artifact.getInstructionsProperties().stringPropertyNames()) {
                if (!p2Artifact.getInstructions().containsKey(propertyName))
                    continue;

                String message = String.format(Locale.ENGLISH,"BND instruction <%s> from <instructions> " +
                        "is overridden in <instructionsProperties>", propertyName);
                Logger.getLog().warn(message);
            }
        }
    }

    public static void validateArtifactConfig(P2Artifact p2Artifact, ResolvedArtifact resolvedArtifact) {
        boolean bundle = BundleUtils.INSTANCE.isBundle(resolvedArtifact.getArtifact().getFile());
        if (resolvedArtifact.isRoot() && bundle) {
            // artifact is a bundle and somebody specified instructions without override
            if (!p2Artifact.shouldOverrideManifest() && !p2Artifact.getCombinedInstructions().isEmpty()) {
                String message = String.format(Locale.ENGLISH, "p2-maven-plugin misconfiguration" +
                        "%n%n\tJar [%s] is already a bundle. " +
                        "%n\tBND instructions are specified, but the <override> flag is set to false." +
                        "%n\tEither remove the instructions or set the <override> flag to true." +
                        "%n\tWATCH OUT! Setting <override> to true will re-bundle the artifact!%n", resolvedArtifact.getArtifact().toString());
                throw new RuntimeException(message);
            }
            // artifact is a bundle and somebody specified singleton flag without override
            if (!p2Artifact.shouldOverrideManifest() && p2Artifact.isSingleton()) {
                String message = String.format(Locale.ENGLISH,"p2-maven-plugin misconfiguration" +
                        "%n%n\tJar [%s] is already a bundle. " +
                        "%n\tsingleton is set to true, but the <override> flag is set to false." +
                        "%n\tEither remove the singleton flag or set the <override> flag to true." +
                        "%n\tWATCH OUT! Setting <override> to true will re-bundle the artifact!%n", resolvedArtifact.getArtifact().toString());
                throw new RuntimeException(message);
            }
        }
    }

}

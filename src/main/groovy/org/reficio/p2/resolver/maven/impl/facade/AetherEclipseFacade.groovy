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
package org.reficio.p2.resolver.maven.impl.facade

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

import org.eclipse.aether.artifact.Artifact as AetherArtifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.util.artifact.SubArtifact
import org.eclipse.aether.util.filter.PatternExclusionsDependencyFilter
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import org.reficio.p2.resolver.maven.Artifact

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
@SuppressFBWarnings(value = [ "EI_EXPOSE_REP2" , "EI_EXPOSE_REP" , "SE_NO_SERIALVERSIONID" ])
class AetherEclipseFacade implements AetherFacade {

    @Override
    def newDependencyRequest(dependencyNode, dependencyFilter) {
        new DependencyRequest((DependencyNode) dependencyNode, (DependencyFilter) dependencyFilter)
    }

    @Override
    def newPreorderNodeListGenerator() {
        new PreorderNodeListGenerator()
    }

    @Override
    def newDependency(defaultArtifact, String scope) {
        new Dependency((DefaultArtifact) defaultArtifact, scope)
    }

    @Override
    def newCollectRequest() {
        new CollectRequest()
    }

    @Override
    def newDefaultArtifact(artifact) {
        new DefaultArtifact(artifact)
    }

    @Override
    def newArtifactRequest() {
        new ArtifactRequest()
    }

    @Override
    def newSubArtifact(artifact, String classifier, String extension) {
        new SubArtifact(artifact, classifier, extension)
    }

    @Override
    def newPatternExclusionsDependencyFilter(List<String> excludes) {
        new PatternExclusionsDependencyFilter(excludes)
    }

    @Override
    def newDependencyFilter(filterClosure) {
        new DependencyFilter() {
            @Override
            boolean accept(DependencyNode node, List<DependencyNode> parents) {
                filterClosure(node, parents)
            }
        }
    }

    @Override
    Artifact translateArtifactAetherToGeneric(artifact) {
        AetherArtifact aetherArtifact = (AetherArtifact) artifact
        aetherArtifact.with {
            return new Artifact(groupId, artifactId, baseVersion, extension, classifier,
                    snapshot, version, file)
        }
    }

    @Override
    def translateArtifactGenericToAether(Artifact a) {
        // baseVersion and snapshot properties are internal fields calculated on the basis of the others
        new DefaultArtifact(a.groupId, a.artifactId, a.classifier, a.extension, a.version, null, a.file)
    }

}

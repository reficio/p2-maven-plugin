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
package org.reficio.p2.repo.aether.facade

import org.reficio.p2.repo.Artifact
import org.sonatype.aether.artifact.Artifact as AetherArtifact
import org.sonatype.aether.collection.CollectRequest
import org.sonatype.aether.graph.Dependency
import org.sonatype.aether.graph.DependencyFilter
import org.sonatype.aether.graph.DependencyNode
import org.sonatype.aether.resolution.ArtifactRequest
import org.sonatype.aether.resolution.DependencyRequest
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.util.artifact.SubArtifact
import org.sonatype.aether.util.filter.PatternExclusionsDependencyFilter
import org.sonatype.aether.util.graph.PreorderNodeListGenerator

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.3
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
class AetherSonatypeFacade implements AetherFacade {

    @Override
    def newDependengyRequest(dependencyNode, dependencyFilter) {
        new DependencyRequest((DependencyNode) dependencyNode, (DependencyFilter) dependencyFilter)
    }

    @Override
    def newPreorderNodeListGenerator() {
        new PreorderNodeListGenerator()
    }

    @Override
    def newCollectRequest() {
        new CollectRequest()
    }

    @Override
    def newDependency(defaultArtifact, String scope) {
        new Dependency((DefaultArtifact) defaultArtifact, scope)
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
        Artifact p2Artifact = new Artifact()
        AetherArtifact aetherArtifact = (AetherArtifact) artifact
        p2Artifact.with {
            groupId = aetherArtifact.groupId
            artifactId = aetherArtifact.artifactId
            baseVersion = aetherArtifact.baseVersion
            extension = aetherArtifact.extension
            classifier = aetherArtifact.classifier
            snapshot = aetherArtifact.snapshot
            version = aetherArtifact.version
            file = aetherArtifact.file
        }
        p2Artifact
    }

    @Override
    def translateArtifactGenericToAether(Artifact a) {
        // baseVersion and snapshot properties are internal fields calculated on the basis of the others
        new DefaultArtifact(a.groupId, a.artifactId, a.classifier, a.extension, a.version, null, a.file)
    }

}

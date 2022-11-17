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
package org.reficio.p2.resolver.maven.impl

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

import org.reficio.p2.logger.Logger
import org.reficio.p2.resolver.maven.impl.facade.AetherFacade
import org.reficio.p2.resolver.maven.Artifact
import org.reficio.p2.resolver.maven.ArtifactResolutionRequest
import org.reficio.p2.resolver.maven.ArtifactResolutionResult
import org.reficio.p2.resolver.maven.ArtifactResolver
import org.reficio.p2.resolver.maven.ResolvedArtifact

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
@SuppressFBWarnings(value = ["EI_EXPOSE_REP", "SE_NO_SERIALVERSIONID"])
class AetherResolver implements ArtifactResolver {

    static final String DEFAULT_SCOPE = "compile"

    final repositorySystem
    final repositorySystemSession
    final List<?> remoteRepositories
    final String scope
    final AetherFacade aether

    AetherResolver(repositorySystem, repositorySystemSession, List<?> repos) {
        this(repositorySystem, repositorySystemSession, repos, DEFAULT_SCOPE)
    }

    AetherResolver(repositorySystem, repositorySystemSession, List<?> remoteRepositories, String scope) {
        this.repositorySystem = repositorySystem
        this.repositorySystemSession = repositorySystemSession
        this.remoteRepositories = remoteRepositories
        this.scope = scope
        this.aether = Aether.facade(repositorySystemSession)
    }

    @Override
    ArtifactResolutionResult resolve(ArtifactResolutionRequest request) {
        List<ResolvedArtifact> result = []
        List<Artifact> resolvedBinaries = resolveBinaries(request)
        for (Artifact resolvedBinary : resolvedBinaries) {
            Artifact resolvedSource = null;
            if (request.resolveSource) {
                try {
                    resolvedSource = resolveSourceForArtifact(resolvedBinary);
                } catch (Exception ex) {
                    // will not fail if the source not resolved
                }
            }
            ResolvedArtifact resolvedArtifact = new ResolvedArtifact(resolvedBinary, resolvedSource, isRoot(request, resolvedBinary))
            result += resolvedArtifact
        }
        return new ArtifactResolutionResult(result)
    }

    private static boolean isRoot(ArtifactResolutionRequest request, Artifact artifact) {
        String rootId = request.getRootArtifactId();
        return rootId == artifact.getShortId() || rootId == artifact.getExtendedId() || rootId == artifact.getLongId()
    }

    private List<Artifact> resolveBinaries(ArtifactResolutionRequest request) {
        if (request.resolveTransitive) {
            return translateArtifactsAetherToGeneric(resolveWithTransitive(request.getRootArtifactId(), request.getExcludes()))
        } else {
            return translateArtifactsAetherToGeneric(Arrays.asList(resolveNoTransitive(request.getRootArtifactId())))
        }
    }

    private Artifact resolveSourceForArtifact(Artifact artifact) {
        def artifactRequest = populateSourceRequest(artifact)
        def artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest).artifact
        return aether.translateArtifactAetherToGeneric(artifactResult)
    }

    private resolveNoTransitive(String artifact) {
        def artifactRequest = populateArtifactRequest(artifact)
        return repositorySystem.resolveArtifact(repositorySystemSession, artifactRequest).artifact
    }

    private List<?> resolveWithTransitive(String artifact, List<String> excludes) {
        def collectRequest = populateCollectRequest(artifact)
        def dependencyNode = repositorySystem.collectDependencies(repositorySystemSession, collectRequest).root
        def dependencyRequest = aether.newDependencyRequest(dependencyNode, null)
        dependencyRequest.filter = getFilter(artifact, transformExcludes(artifact, excludes))
        repositorySystem.resolveDependencies(repositorySystemSession, dependencyRequest)
        def preorderNodeListGenerator = aether.newPreorderNodeListGenerator()
        dependencyNode.accept(preorderNodeListGenerator)
        return preorderNodeListGenerator.getArtifacts(false)
    }

    private getFilter(final String artifactName, List<String> excludes) {
        def filter = aether.newPatternExclusionsDependencyFilter(excludes)
        def filterClosure = { node, List<?> parents ->
            boolean accepted = filter.accept(node, parents)
            if (!accepted) {
                def artifact = node.dependency.artifact
                String pattern = "${artifact.groupId}:${artifact.artifactId}:${artifact.baseVersion}"
                if (pattern == artifactName) {
                    return true
                }
            }
            return accepted
        }
        return aether.newDependencyFilter(filterClosure)
    }

    private static List<String> transformExcludes(String artifact, List<String> excludes) {
        List<String> transformedExcludes = []
        for (String exclude : excludes) {
            if (exclude == null || exclude.trim().isEmpty()) {
                // aether bug fix
                Logger.getLog().warn("Empty exclude counts as exclude-all wildcard '*' [${artifact}]")
                transformedExcludes += "*"
            } else {
                transformedExcludes += exclude
            }
        }
        return transformedExcludes
    }


    private populateCollectRequest(String artifact) {
        def collectRequest = aether.newCollectRequest()
        for (def remoteRepository : remoteRepositories) {
            collectRequest.addRepository(remoteRepository)
        }
        collectRequest.addDependency(aether.newDependency(aether.newDefaultArtifact(artifact), scope))
        return collectRequest
    }

    private populateArtifactRequest(String artifact) {
        def artifactRequest = populateRepos(aether.newArtifactRequest())
        artifactRequest.artifact = aether.newDefaultArtifact(artifact)
        return artifactRequest
    }

    private populateSourceRequest(Artifact artifact) {
        def artifactRequest = populateRepos(aether.newArtifactRequest())
        def aetherArtifact = aether.translateArtifactGenericToAether(artifact)
        def sourceArtifact = aether.newSubArtifact(aetherArtifact, "sources", "jar")
        artifactRequest.artifact = sourceArtifact
        return artifactRequest
    }

    private populateRepos(artifactRequest) {
        for (def remoteRepository : remoteRepositories) {
            artifactRequest.addRepository(remoteRepository)
        }
        return artifactRequest
    }

    private List<Artifact> translateArtifactsAetherToGeneric(List<?> artifacts) {
        artifacts.collect() { artifact -> aether.translateArtifactAetherToGeneric(artifact) }
    }

}

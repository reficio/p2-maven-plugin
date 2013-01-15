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
package org.reficio.p2.utils;

import org.apache.commons.lang.StringUtils;
import org.reficio.p2.log.Logger;
import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.filter.PatternExclusionsDependencyFilter;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.0.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
public class ArtifactResolver {

    private static final String DEFAULT_SCOPE = "compile";

    private final RepositorySystem system;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> repos;
    private final String scope;

    public ArtifactResolver(RepositorySystem system, RepositorySystemSession session, List<RemoteRepository> repos) {
        this(system, session, repos, DEFAULT_SCOPE);
    }

    public ArtifactResolver(RepositorySystem system, RepositorySystemSession session, List<RemoteRepository> repos, String scope) {
        this.system = system;
        this.session = session;
        this.repos = repos;
        this.scope = scope;
    }

    public List<Artifact> resolve(String artifact, List<String> excludes, boolean skipTransitive) throws RepositoryException {
        if (skipTransitive) {
            return Arrays.asList(resolveNoTransitive(artifact));
        } else {
            return resolveWithTransitive(artifact, excludes);
        }
    }

    public Artifact resolveSource(Artifact artifact) throws RepositoryException {
        ArtifactRequest request = populateSourceRequest(artifact);
        Artifact result = system.resolveArtifact(session, request).getArtifact();
        return result;
    }

    private Artifact resolveNoTransitive(String artifact) throws RepositoryException {
        ArtifactRequest request = populateArtifactRequest(artifact);
        return system.resolveArtifact(session, request).getArtifact();
    }

    private List<Artifact> resolveWithTransitive(String artifact, List<String> excludes) throws RepositoryException {
        CollectRequest collectRequest = populateCollectRequest(artifact);
        DependencyNode node = system.collectDependencies(session, collectRequest).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        dependencyRequest.setFilter(getFilter(artifact, transformExcludes(artifact, excludes)));
        system.resolveDependencies(session, dependencyRequest);
        PreorderNodeListGenerator nodeGenerator = new PreorderNodeListGenerator();
        node.accept(nodeGenerator);
        return nodeGenerator.getArtifacts(false);
    }

    private DependencyFilter getFilter(final String artifact, List<String> excludes) {
        return new PatternExclusionsDependencyFilter(excludes) {
            @Override
            public boolean accept(final DependencyNode node, List<DependencyNode> parents) {
                boolean result = super.accept(node, parents);
                if (result == false) {
                    Artifact art = node.getDependency().getArtifact();
                    String pattern = String.format("%s:%s:%s", art.getGroupId(), art.getArtifactId(), art.getBaseVersion());
                    if (pattern.equals(artifact)) {
                        return true;
                    }
                }
                return result;
            }
        };
    }

    private List<String> transformExcludes(String artifact, List<String> excludes) {
        List<String> transformedExcludes = new ArrayList<String>();
        for (String exclude : excludes) {
            if (StringUtils.isBlank(exclude)) {
                // aether bug fix
                Logger.getLog().warn("Empty exclude counts as exclude-all wildcard '*' [" + artifact + "]");
                transformedExcludes.add("*");
            } else {
                transformedExcludes.add(exclude);
            }
        }
        return transformedExcludes;
    }


    private CollectRequest populateCollectRequest(String artifact) {
        CollectRequest collectRequest = new CollectRequest();
        for (RemoteRepository r : repos) {
            collectRequest.addRepository(r);
        }
        collectRequest.addDependency(new Dependency(new DefaultArtifact(artifact), scope));
        return collectRequest;
    }

    private ArtifactRequest populateArtifactRequest(String artifact) {
        ArtifactRequest artifactRequest = populateRepos(new ArtifactRequest());
        artifactRequest.setArtifact(new DefaultArtifact(artifact));
        return artifactRequest;
    }

    private ArtifactRequest populateSourceRequest(Artifact artifact) {
        ArtifactRequest artifactRequest = populateRepos(new ArtifactRequest());
        Artifact sourceArtifact = new SubArtifact(artifact, "sources", "jar");
        artifactRequest.setArtifact(sourceArtifact);
        return artifactRequest;
    }

    private ArtifactRequest populateRepos(ArtifactRequest artifactRequest) {
        for (RemoteRepository r : repos) {
            artifactRequest.addRepository(r);
        }
        return artifactRequest;
    }


}

/**
 * Copyright (c) 2012 centeractive ag. All Rights Reserved.
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

import org.sonatype.aether.RepositoryException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import javax.security.auth.DestroyFailedException;
import java.util.List;

/**
 * User: Tom Bujok (tom.bujok@reficio.org)
 * Date: 2012-03-18
 * Time: 4:02 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
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

    public List<Artifact> resolve(String artifact) throws RepositoryException {
        CollectRequest collectRequest = populateCollectRequest();
        Dependency dependency = new Dependency(new DefaultArtifact(artifact), scope);
        collectRequest.addDependency(dependency);
        DependencyNode node = system.collectDependencies(session, collectRequest).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        system.resolveDependencies(session, dependencyRequest);
        PreorderNodeListGenerator nodeGenerator = new PreorderNodeListGenerator();
        node.accept(nodeGenerator);
        return nodeGenerator.getArtifacts(false);
    }

    private CollectRequest populateCollectRequest() {
        CollectRequest collectRequest = new CollectRequest();
        for (RemoteRepository r : repos) {
            collectRequest.addRepository(r);
        }
        return collectRequest;
    }

}

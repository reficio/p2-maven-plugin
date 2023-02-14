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
package org.reficio.p2.bundler;

import org.junit.jupiter.api.Test;
import org.reficio.p2.P2Artifact;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class P2ArtifactMapTest {

    @Test
    void getNoFoundValueReturnsEmptyList() {
        P2ArtifactMap<P2Artifact> mapUnderTest = new P2ArtifactMap<>();

        Collection<P2Artifact> value = mapUnderTest.get(new P2Artifact());

        assertNotNull(value);
    }
}

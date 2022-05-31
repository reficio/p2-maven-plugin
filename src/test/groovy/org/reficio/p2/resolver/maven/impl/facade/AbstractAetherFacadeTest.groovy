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

import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.0
 */
abstract class AbstractAetherFacadeTest {

    abstract AetherFacade facade();

    abstract String expectedPackage();

    void assertCorrectType(object) {
        assertEquals(expectedPackage(),
                object.getClass().getPackage().getName().substring(0, expectedPackage().length()));
    }

    @Test
    void newDependencyRequest() {
        assertCorrectType(facade().newDependencyRequest(null, null))
    }

    @Test
    void newPreorderNodeListGenerator() {
        assertCorrectType(facade().newPreorderNodeListGenerator())
    }

    @Test
    void newCollectRequest() {
        assertCorrectType(facade().newCollectRequest())
    }

    @Test
    void newDependency() {
        assertCorrectType(facade().newDependency(facade().newDefaultArtifact("org.reficio:p2:1.0.0"), null))
    }

    @Test
    void newDefaultArtifact() {
        assertCorrectType(facade().newDefaultArtifact("org.reficio:p2:1.0.0"))
    }

    @Test
    void newArtifactRequest() {
        assertCorrectType(facade().newArtifactRequest())
    }

    @Test
    void newSubArtifact() {
        assertCorrectType(facade().newSubArtifact(facade().newDefaultArtifact("org.reficio:p2:1.0.0"), null, null))
    }

    @Test
    void newPatternExclusionsDependencyFilter() {
        assertCorrectType(facade().newPatternExclusionsDependencyFilter([]))
    }

}

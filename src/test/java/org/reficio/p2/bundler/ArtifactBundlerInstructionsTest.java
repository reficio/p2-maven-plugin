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

package org.reficio.p2.bundler;

import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class ArtifactBundlerInstructionsTest {

    @Test
    public void useDefaultInstruction(){
        ArtifactBundlerInstructions artifactBundlerInstructions = ArtifactBundlerInstructions.builder().build();

        assertThat(artifactBundlerInstructions.getInstructions())
                .hasSize(1)
                .containsEntry("_fixupmessages", "\"Classes found in the wrong directory\";is:=warning");

    }

    @Test
    public void ignoreDefaultInstruction(){
        ArtifactBundlerInstructions artifactBundlerInstructions = ArtifactBundlerInstructions.builder()
                .instructions(Collections.singletonMap("-noee", "true"))
                .build();

        assertThat(artifactBundlerInstructions.getInstructions())
                .hasSize(1)
                .containsEntry("-noee", "true");
    }

}
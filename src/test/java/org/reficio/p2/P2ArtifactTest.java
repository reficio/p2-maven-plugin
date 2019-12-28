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

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

/**
 * @since 1.4.0
 */
public class P2ArtifactTest {

    @Test
    public void testCombinedInstructions() {
        // GIVEN
        P2Artifact artifact = new P2Artifact();
        artifact.setInstructions(Collections.singletonMap("Import-Package", "package.one"));
        Properties instructionsProperties = new Properties();
        instructionsProperties.setProperty("Export-Package", "package.two");
        artifact.setInstructionsProperties(instructionsProperties);

        // WHEN
        Map<String, String> combinedInstructions = artifact.getCombinedInstructions();

        // THEN
        assertEquals(2, combinedInstructions.size());
        assertEquals("package.one", combinedInstructions.get("Import-Package"));
        assertEquals("package.two", combinedInstructions.get("Export-Package"));
    }

    @Test
    public void testInstructionsPropertiesOverrideInstructions() {
        // GIVEN
        P2Artifact artifact = new P2Artifact();
        artifact.setInstructions(Collections.singletonMap("Export-Package", "package.one"));
        Properties instructionsProperties = new Properties();
        instructionsProperties.setProperty("Export-Package", "package.two");
        artifact.setInstructionsProperties(instructionsProperties);

        // WHEN
        Map<String, String> combinedInstructions = artifact.getCombinedInstructions();

        // THEN
        assertEquals(1, combinedInstructions.size());
        assertEquals("package.two", combinedInstructions.get("Export-Package"));
    }

    @Test
    public void testInstructionsPropertiesOverrideInstructionsWhenPropertiesSetFirst() {
        // GIVEN
        P2Artifact artifact = new P2Artifact();
        Properties instructionsProperties = new Properties();
        instructionsProperties.setProperty("Export-Package", "package.two");
        artifact.setInstructionsProperties(instructionsProperties);
        artifact.setInstructions(Collections.singletonMap("Export-Package", "package.one"));

        // WHEN
        Map<String, String> combinedInstructions = artifact.getCombinedInstructions();

        // THEN
        assertEquals(1, combinedInstructions.size());
        assertEquals("package.two", combinedInstructions.get("Export-Package"));
    }
}

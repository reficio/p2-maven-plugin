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
package org.reficio.p2.bundler.impl;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.assertj.core.api.ThrowableAssert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.reficio.p2.bundler.AquteAnalyzerException;
import org.reficio.p2.bundler.ArtifactBundlerInstructions;
import org.reficio.p2.bundler.ArtifactBundlerRequest;
import org.reficio.p2.logger.Logger;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;

@SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
public class AquteBundlerTest {


    @BeforeClass
    public static void setUpClass() {
        Logger.initialize(new SystemStreamLog());
    }

    @AfterClass
    public static void cleanUp() {
        Logger.initialize(null);
    }

    @Test
    public void bndAnalyzerProduceErrorThenExceptionIsExpected() {
        try (MockedStatic<AquteHelper> theMock = mockStatic(AquteHelper.class)) {
            theMock.when(() -> AquteHelper.buildAnalyzer(any(ArtifactBundlerRequest.class), any(ArtifactBundlerInstructions.class), anyBoolean()))
                    .thenReturn(new AnalyzerStub());

            AquteBundler bundlerUnderTest = new AquteBundler(true, false, new BundleUtilsStub());
            ThrowableAssert.ThrowingCallable methodUnderTest = () -> bundlerUnderTest.execute(new ArtifactBundlerRequest(new File(""), new File("target/tmp.jar"), null, null, true), ArtifactBundlerInstructions.builder().build());

            assertThatThrownBy(methodUnderTest).isInstanceOf(RuntimeException.class).hasRootCauseInstanceOf(AquteAnalyzerException.class);
        }
    }

    @Test
    public void bndAnalyzerProduceErrorThenExceptionIsUnexpected() {
        try (MockedStatic<AquteHelper> theMock = mockStatic(AquteHelper.class)) {
            theMock.when(() -> AquteHelper.buildAnalyzer(any(ArtifactBundlerRequest.class), any(ArtifactBundlerInstructions.class), anyBoolean()))
                    .thenReturn(new AnalyzerStub());

            AquteBundler bundlerUnderTest = new AquteBundler(true, true, new BundleUtilsStub());

            bundlerUnderTest.execute(new ArtifactBundlerRequest(new File(""), new File("target/tmp.jar"), null, null, true), ArtifactBundlerInstructions.builder().build());
        }
    }
}
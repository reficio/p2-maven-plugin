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
package org.reficio.p2.publisher;

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public class CategoryPublisherTest {

    @Test(expected = NullPointerException.class)
    public void nullLauncher() {
        CategoryPublisher.builder().p2ApplicationLauncher(null);
    }

    @Test(expected = NullPointerException.class)
    public void emptyBuilder() {
        CategoryPublisher.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongTimeout() {
        CategoryPublisher.builder().forkedProcessTimeoutInSeconds(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongArgs() {
        CategoryPublisher.builder().additionalArgs("--zcx.vzxc.v§';s.dcxz-1-aods[vzmcxvlkzndofahsdpf");
    }

    @Test(expected = MojoFailureException.class)
    public void exceptionThrownInCaseOfLauncherFailure() throws IOException, AbstractMojoExecutionException {
        // given
        P2ApplicationLauncher launcher = Mockito.mock(P2ApplicationLauncher.class, Mockito.RETURNS_DEEP_STUBS);
        when(launcher.execute(Mockito.anyInt())).thenReturn(137);
        File file = File.createTempFile(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        file.deleteOnExit();

        // when
        CategoryPublisher publisher = CategoryPublisher.builder()
                .p2ApplicationLauncher(launcher)
                .categoryFileLocation(file.getPath())
                .additionalArgs("-args")
                .metadataRepositoryLocation("target/tmp")
                .build();
        publisher.execute();
    }

}

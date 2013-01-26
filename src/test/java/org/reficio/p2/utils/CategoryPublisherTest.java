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

import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.sisu.equinox.launching.internal.P2ApplicationLauncher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * @author: Tom Bujok (tom.bujok@gmail.com)
 * <p/>
 * Reficio™ - Reestablish your software!
 * www.reficio.org
 */
public class CategoryPublisherTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test(expected = IllegalArgumentException.class)
    public void factoryLauncher() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(new StringContains("p2ApplicationLauncher cannot be null"));

        CategoryPublisher.factory().p2ApplicationLauncher(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void factoryEmptyCreate() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(new StringContains("p2ApplicationLauncher cannot be null"));

        CategoryPublisher.factory().create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void factoryTimeout() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(new StringContains("forkedProcessTimeoutInSeconds cannot be negative"));

        CategoryPublisher.factory().forkedProcessTimeoutInSeconds(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void factoryAdditionalArgs() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(new StringContains("Unable to translate additional arguments into command line array"));

        CategoryPublisher.factory().additionalArgs("--zcx.vzxc.v§';s.dcxz-1-aods[vzmcxvlkzndofahsdpf");
    }

    @Test(expected = MojoFailureException.class)
    public void execute() throws IOException, AbstractMojoExecutionException {
        thrown.expect(MojoFailureException.class);
        thrown.expectMessage(new StringContains("P2 publisher return code was 137"));

        P2ApplicationLauncher launcher = Mockito.mock(P2ApplicationLauncher.class, Mockito.RETURNS_DEEP_STUBS);
        when(launcher.execute(Mockito.anyInt())).thenReturn(137);

        CategoryPublisher publisher = CategoryPublisher.factory().p2ApplicationLauncher(launcher).create();
        File file = File.createTempFile(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        file.deleteOnExit();;
        publisher.execute(file.getPath(), Mockito.anyString());
    }


}

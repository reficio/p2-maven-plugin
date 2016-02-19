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

import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Jar;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public class BundleUtilsTest {

    private BundleUtils utils;

    @Before
    public void setup() {
        utils = new BundleUtils();
    }

    @Test
    public void isBundle_nonExistingJar() {
        // given
        Jar jar = new Jar("non-existing.jar");

        // when
        boolean isBundle = utils.isBundle(jar);

        // then
        assertFalse(isBundle);
    }

    @Test
    public void isBundle_cannotOpenManifest() throws Exception {
        // given
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest()).thenThrow(new IOException());

        // when
        boolean isBundle = utils.isBundle(jar);

        // then
        assertFalse(isBundle);
    }

    @Test
    public void isBundle_emptyManifest() throws Exception {
        // given
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes()).thenReturn(null);

        // when
        boolean isBundle = utils.isBundle(jar);

        // then
        assertFalse(isBundle);
    }

    @Test
    public void isBundle_manifestWithAttributes() throws Exception {
        // given
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes().getValue(any(Attributes.Name.class))).thenReturn("org.apache.commons");

        // when
        boolean isBundle = utils.isBundle(jar);

        // then
        assertTrue(isBundle);
    }

    @Test
    public void newManifest_hasMainAttributes() {
        assertNotNull(new Manifest().getMainAttributes());
    }

    @Test(expected = RuntimeException.class)
    public void isBundle_nonExistingFile() {
        // given
        File file = new File(UUID.randomUUID().toString());

        // when
        utils.isBundle(file);
    }

    @Test
    public void getBundleName_correctBundleNameFromMainAttribuets() throws Exception {
        // given
        String bundleName = "org.reficio.example.bundle";
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes().getValue(new Attributes.Name(Analyzer.BUNDLE_NAME))).thenReturn(bundleName);

        // when
        String bundleNameFromManifest = utils.getBundleName(jar);

        // then
        assertEquals(bundleName, bundleNameFromManifest);
    }

}

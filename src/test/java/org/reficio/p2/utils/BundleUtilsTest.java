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

import aQute.lib.osgi.Analyzer;
import aQute.lib.osgi.Jar;
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
 * User: Tom Bujok (tom.bujok@gmail.com)
 * Date: 2012-02-12
 * Time: 2:04 PM
 * <p/>
 * Reficio (TM) - Reestablish your software!
 * http://www.reficio.org
 */
public class BundleUtilsTest {

    @Test
    public void testIsBundleNoJar() {
        Jar jar = new Jar("non-existing.jar");
        BundleUtils utils = new BundleUtils();
        assertFalse(utils.isBundle(jar));
    }

    @Test
    public void testIsBundleIOException() throws IOException {
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest()).thenThrow(new IOException());
        BundleUtils utils = new BundleUtils();
        assertFalse(utils.isBundle(jar));
    }

    @Test
    public void testIsBundleNoAttributes() throws IOException {
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes()).thenReturn(null);
        BundleUtils utils = new BundleUtils();
        assertFalse(utils.isBundle(jar));
    }

    @Test
    public void testIsBundleManifestContainsKey() throws IOException {
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes().getValue(any(Attributes.Name.class))).thenReturn("org.apache.commons");
        BundleUtils utils = new BundleUtils();
        assertTrue(utils.isBundle(jar));
    }

    @Test
    public void testManifestAttributes() {
        assertNotNull(new Manifest().getMainAttributes());
    }

    @Test
    public void testMatches() {
        String name = "Commons Lang";
        assertTrue(name.matches(".*\\s+.*"));
        assertTrue(name.matches(".*[A-Z].*"));
    }

    @Test(expected = RuntimeException.class)
    public void testIsBundleNoFile() {
        File file = new File(UUID.randomUUID().toString());
        BundleUtils utils = new BundleUtils();
        utils.isBundle(file);
    }

    @Test
    public void testBundleName() throws IOException {
        String bundleName = "org.reficio.example.bundle";
        Jar jar = mock(Jar.class, Mockito.RETURNS_DEEP_STUBS);
        when(jar.getManifest().getMainAttributes().getValue(new Attributes.Name(Analyzer.BUNDLE_NAME))).thenReturn(bundleName);
        BundleUtils utils = new BundleUtils();
        assertEquals(bundleName, utils.getBundleName(jar));
    }

}

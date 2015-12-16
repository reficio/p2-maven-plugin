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
package org.reficio.p2.utils

import aQute.bnd.osgi.Analyzer
import aQute.bnd.osgi.Jar
import org.reficio.p2.bundler.impl.AquteBundler

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
class TestUtils {

    static String symbolicName(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.BUNDLE_SYMBOLICNAME)
    }

    static String version(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.BUNDLE_VERSION)
    }

    static void validateVersion(Jar jar, String versionString) {
        assert version(jar) == versionString
    }

    static void validateSnapshotVersionPrefix(Jar jar, String versionStringPrefix) {
        assert version(jar).startsWith(versionStringPrefix)
    }

    static boolean isVersionOriginalSnapshot(String version) {
        return version.matches(".*\\.[0-9_]{13,15}-[0-9]{3}")
    }

    static boolean isVersionOriginalSnapshot(Jar jar) {
        String version = version(jar)
        return isVersionOriginalSnapshot(version)
    }

    static boolean isVersionRepackedSnapshot(String version) {
        return version.matches(".*\\.[0-9]{14}")
    }

    static boolean isVersionRepackedSnapshot(Jar jar) {
        String version = version(jar)
        return isVersionRepackedSnapshot(version)
    }

    static void validateOriginalSnapshot(Jar jar, String versionPrefix) {
        if (!isVersionOriginalSnapshot(jar) || isVersionRepackedSnapshot(jar)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version=" + version(jar) + " " + jar)
        }
        if (!version(jar).startsWith(versionPrefix)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version prefix do not match=" + version(jar) + " " + jar)
        }
    }

    static void validateRepackedSnapshot(Jar jar, String versionPrefix) {
        if (isVersionOriginalSnapshot(jar) || !isVersionRepackedSnapshot(jar)) {
            throw new RuntimeException("validateRepackedSnapshot failed version=" + version(jar) + " " + jar)
        }
        if (!version(jar).startsWith(versionPrefix)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version prefix do not match=" + version(jar) + " " + jar)
        }
    }

    static Jar jar(File target, String fileNamePrefix) {
        def files = target.listFiles()
        File file = files.find { file -> file.name.startsWith(fileNamePrefix) }
        if (!file || !file.exists()) {
            throw new RuntimeException("No file like " + fileNamePrefix + " in target folder")
        }
        return new Jar(file)
    }

    static String tool(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.TOOL)
    }

    static String eclipseSourceBundle(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(AquteBundler.ECLIPSE_SOURCE_BUNDLE)
    }

    static String attr(Jar jar, String key) {
        jar?.getManifest()?.getMainAttributes()?.getValue(key)
    }

}

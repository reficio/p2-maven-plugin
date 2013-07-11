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

import aQute.lib.osgi.Analyzer
import aQute.lib.osgi.Jar

/**
 * @author: Tom Bujok (tom.bujok@gmail.com)
 *
 * Reficio™ - Reestablish your software!
 * www.reficio.org
 */
class JarUtil {

    def static String symbolicName(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.BUNDLE_SYMBOLICNAME)
    }

    def static String version(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.BUNDLE_VERSION)
    }

    def static void validateVersion(Jar jar, String versionString) {
        assert version(jar) == versionString
    }

    def static void validateSnapshotVersionPrefix(Jar jar, String versionStringPrefix) {
        assert version(jar).startsWith(versionStringPrefix)
    }

    def static boolean isVersionOriginalSnapshot(String version) {
        return version.matches(".*\\.[0-9]{14}-[0-9]{3}")
    }

    def static boolean isVersionOriginalSnapshot(Jar jar) {
        String version = version(jar)
        return isVersionOriginalSnapshot(version)
    }

    def static boolean isVersionRepackedSnapshot(String version) {
        return version.matches(".*\\.[0-9]{14}")
    }

    def static boolean isVersionRepackedSnapshot(Jar jar) {
        String version = version(jar)
        return isVersionRepackedSnapshot(version)
    }

    def static void validateOriginalSnapshot(Jar jar, String versionPrefix) {
        if (!isVersionOriginalSnapshot(jar) || isVersionRepackedSnapshot(jar)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version=" + version(jar) + " " + jar)
        }
        if (!version(jar).startsWith(versionPrefix)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version prefix do not match=" + version(jar) + " " + jar)
        }
    }

    def static void validateRepackedSnapshot(Jar jar, String versionPrefix) {
        if (isVersionOriginalSnapshot(jar) || !isVersionRepackedSnapshot(jar)) {
            throw new RuntimeException("validateRepackedSnapshot failed version=" + version(jar) + " " + jar)
        }
        if (!version(jar).startsWith(versionPrefix)) {
            throw new RuntimeException("validateOriginalSnapshot failed; version prefix do not match=" + version(jar) + " " + jar)
        }
    }

    def static Jar jar(File target, String fileNamePrefix) {
        def files = target.listFiles()
        File file = files.find { file -> file.name.startsWith(fileNamePrefix) }
        if (!file || !file.exists()) {
            throw new RuntimeException("No file like " + fileNamePrefix + " in target folder")
        }
        return new Jar(file)
    }

    def static String tool(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(Analyzer.TOOL)
    }

    def static String eclipseSourceBundle(Jar jar) {
        jar?.getManifest()?.getMainAttributes()?.getValue(BundleWrapper.ECLIPSE_SOURCE_BUNDLE)
    }

    def static String attr(Jar jar, String key) {
        jar?.getManifest()?.getMainAttributes()?.getValue(key)
    }

}

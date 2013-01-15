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

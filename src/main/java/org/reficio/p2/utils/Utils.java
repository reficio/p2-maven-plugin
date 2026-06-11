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
package org.reficio.p2.utils;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class Utils {

    private static final String JAR_SNAPSHOT_POSTFIX = "-SNAPSHOT";
    private static final String OSGI_SNAPSHOT_POSTFIX = ".SNAPSHOT";
    private static final String ECLIPSE_QUALIFIER_POSTFIX = ".qualifier";
    private static final String TYCHO_VERSION = "5.0.3";

    public static String getTimeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }
    
    public static String eclipseQualifierToTimeStamp(String version, String timestamp) {
        String tweakedVersion = version;
        if (version.contains(ECLIPSE_QUALIFIER_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(ECLIPSE_QUALIFIER_POSTFIX, "." + timestamp );
        }
        return tweakedVersion;
    }
    
    public static String snapshotToTimestamp(String version, String timestamp) {
        String tweakedVersion = version;
        if (version.contains(JAR_SNAPSHOT_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(JAR_SNAPSHOT_POSTFIX, "-" + timestamp);
        } else if (version.contains(OSGI_SNAPSHOT_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(OSGI_SNAPSHOT_POSTFIX, "." + timestamp);
        }
        return tweakedVersion;
    }
    
    public static String mavenToEclipse(String version, String timestamp) {
    	 String tweakedVersion = version;
         if (version.contains(JAR_SNAPSHOT_POSTFIX)) {
             tweakedVersion = tweakedVersion.replace(JAR_SNAPSHOT_POSTFIX, "." + timestamp);
         }
         return tweakedVersion;
    }

    public static String tychoVersion() {
        String tychoVersion;
        try (InputStream is = Utils.class.getClassLoader().getResourceAsStream("build.properties")) {
            Properties p = new Properties();
            p.load(is);
            tychoVersion = p.getProperty("tycho.version");
            if (null == tychoVersion) {
                throw new RuntimeException("Failed getting tycho version from loaded properties: " + p);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed reading tycho version: ", e);
        }
        return tychoVersion;
    }
}

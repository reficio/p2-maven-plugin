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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author: Tom Bujok (tom.bujok@gmail.com)
 * <p/>
 * Reficio™ - Reestablish your software!
 * www.reficio.org
 */
public class JarUtils {

    private static final String SNAPSHOT_POSTFIX = "SNAPSHOT";

    public static void adjustOutputVersion(ResolvedArtifact artifact, File inputFile, File outputFile) {
        if (artifact.isSnapshot()) {
            Jar jar = null;
            try {
                jar = new Jar(inputFile);
                Manifest manifest = jar.getManifest();
                Attributes attributes = manifest.getMainAttributes();
                String version = attributes.getValue(Analyzer.BUNDLE_VERSION);
                version = tweakVersion(artifact, version);
                attributes.putValue(Analyzer.BUNDLE_VERSION, version);
                jar.write(outputFile);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open jar " + outputFile);
            } catch (Exception e) {
                throw new RuntimeException("Cannot open jar " + outputFile);
            } finally {
                if (jar != null) {
                    jar.close();
                }
            }
        }
    }

    public static void removeSignature(File jar) {
        File unsignedJar = new File(jar.getParent(), jar.getName() + ".tmp");
        try {
            if (unsignedJar.exists()) {
                FileUtils.deleteQuietly(unsignedJar);
                unsignedJar = new File(jar.getParent(), jar.getName() + ".tmp");
            }
            if (!unsignedJar.createNewFile()) {
                throw new RuntimeException("Cannot create file " + unsignedJar);
            }

            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(unsignedJar));
            try {
                ZipFile zip = new ZipFile(jar);
                for (Enumeration list = zip.entries(); list.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) list.nextElement();
                    String name = entry.getName();
                    if (entry.isDirectory()) {
                        continue;
                    } else if (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".SF")) {
                        continue;
                    }

                    InputStream zipInputStream = zip.getInputStream(entry);
                    zipOutputStream.putNextEntry(entry);
                    try {
                        IOUtils.copy(zipInputStream, zipOutputStream);
                    } finally {
                        zipInputStream.close();
                    }
                }
                IOUtils.closeQuietly(zipOutputStream);
                FileUtils.copyFile(unsignedJar, jar);
            } finally {
                IOUtils.closeQuietly(zipOutputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(unsignedJar);
        }
    }

    public static boolean containsSignature(File jarToUnsign) {
        try {
            ZipFile zip = new ZipFile(jarToUnsign);
            try {
                for (Enumeration list = zip.entries(); list.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) list.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && (name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".SF"))) {
                        return true;
                    }
                }
                return false;
            } finally {
                zip.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTimeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
    }

    public static String tweakVersion(ResolvedArtifact artifact, String version) {
        String tweakedVersion = version;
        if (version.contains(SNAPSHOT_POSTFIX)) {
            String postfix = getSnapshotPostfix(artifact);
            if (StringUtils.isBlank(postfix)) {
                postfix = getTimeStamp();
            }
            tweakedVersion = tweakedVersion.replace("-" + SNAPSHOT_POSTFIX, "." + postfix);
            tweakedVersion = tweakedVersion.replace("." + SNAPSHOT_POSTFIX, "." + postfix);
        }
        return tweakedVersion;
    }

    private static String getSnapshotPostfix(ResolvedArtifact artifact) {
        if (artifact.isSnapshot()) {
            String version = artifact.getArtifact().getVersion();
            int dashIndex = version.indexOf("-");
            if (dashIndex > 0) {
                String postfix = version.substring(dashIndex + 1);
                postfix = postfix.replace(".", "");
                if (postfix.contains(SNAPSHOT_POSTFIX)) {
                    return "";
                }
                return postfix;
            }
        }
        return "";
    }

}

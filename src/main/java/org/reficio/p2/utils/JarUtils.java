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
import aQute.bnd.osgi.FileResource;
import aQute.bnd.osgi.Jar;
import aQute.bnd.osgi.Resource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.0.0
 */
public class JarUtils {

    private static final String JAR_SNAPSHOT_POSTFIX = "-SNAPSHOT";
    private static final String OSGI_SNAPSHOT_POSTFIX = ".SNAPSHOT";
    private static final String ECLIPSE_QUALIFIER_POSTFIX = ".qualifier";

    public static void adjustSnapshotOutputVersion(File inputFile, File outputFile, String version) {
        Jar jar = null;
        try {
            jar = new Jar(inputFile);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.putValue(Analyzer.BUNDLE_VERSION, version);
            jar.write(outputFile);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open jar " + outputFile, e);
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
    }

    public static void adjustFeatureQualifierVersionWithTimestamp(File inputFile, File outputFile) {
        Jar jar = null;
        try {
            jar = new Jar(inputFile);
            Resource res = jar.getResource("feature.xml");
            Document featureSpec = parseXml(res.openInputStream());
            String version = featureSpec.getDocumentElement().getAttributeNode("version").getValue();
            String newVersion = replaceQualifierWithTimestamp(version);
            featureSpec.getDocumentElement().getAttributeNode("version").setValue(newVersion);
            File newXml = new File(inputFile.getParentFile(), "feature.xml");
            writeXml(featureSpec, newXml);
            FileResource newRes = new FileResource(newXml);
            jar.putResource("feature.xml", newRes, true);
            jar.write(outputFile);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open jar " + outputFile, e);
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
    }

    public static Document parseXml(InputStream input) {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(false);
            DocumentBuilder docBuilder = fac.newDocumentBuilder();
            Document doc = docBuilder.parse(input);
            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse XML input", e);
        }
    }

    public static void writeXml(Document doc, File outputFile) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(outputFile);
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write XML document to file " + outputFile.getName(), e);
        }
    }

    public static String replaceQualifierWithTimestamp(String version) {
        String tweakedVersion = version;
        if (version.contains(ECLIPSE_QUALIFIER_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(ECLIPSE_QUALIFIER_POSTFIX, "." + getTimeStamp());
        }
        return tweakedVersion;
    }

    public static String replaceSnapshotWithTimestamp(String version) {
        String tweakedVersion = version;
        if (version.contains(JAR_SNAPSHOT_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(JAR_SNAPSHOT_POSTFIX, "-" + getTimeStamp());
        } else if (version.contains(OSGI_SNAPSHOT_POSTFIX)) {
            tweakedVersion = tweakedVersion.replace(OSGI_SNAPSHOT_POSTFIX, "." + getTimeStamp());
        }
        return tweakedVersion;
    }

    public static String getTimeStamp() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        return format.format(new Date());
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

}

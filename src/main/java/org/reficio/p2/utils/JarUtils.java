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
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import clover.retrotranslator.edu.emory.mathcs.backport.java.util.Arrays;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
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

    
    public static void adjustFeatureXml(File inputFile, File outputFile, File pluginDir, Log log, String timestamp) {
        Jar jar = null;
        File newXml = null;
        try {
        	jar = new Jar(inputFile);
	        Resource res = jar.getResource("feature.xml");
	        Document featureSpec = XmlUtils.parseXml(res.openInputStream());
	        
	        adjustFeatureQualifierVersionWithTimestamp(featureSpec, timestamp);
	        adjustFeaturePluginData(featureSpec, pluginDir, log);
            
	        File temp = new File(outputFile.getParentFile(),"temp");
	        temp.mkdir();
	        newXml = new File(temp,"feature.xml");
	        XmlUtils.writeXml(featureSpec, newXml);
            FileResource newRes = new FileResource(newXml);
            jar.putResource("feature.xml", newRes, true);
            jar.write(outputFile);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open jar " + outputFile, e);
        } finally {
            if (jar != null) {
                jar.close();
            }
            if (null!=newXml) {
            	newXml.delete();
            }
        }
    }
    
    public static void adjustFeatureQualifierVersionWithTimestamp(Document featureSpec, String timestamp) {
	        String version = featureSpec.getDocumentElement().getAttributeNode("version").getValue();
	        String newVersion = Utils.eclipseQualifierToTimeStamp(version, timestamp); 
	        featureSpec.getDocumentElement().getAttributeNode("version").setValue(newVersion);
    }

    public static void adjustFeaturePluginData(Document featureSpec, File pluginDir, Log log) throws IOException {
	        //get list of all plugins
	        NodeList plugins = featureSpec.getElementsByTagName("plugin");
	        for(int i=0; i<plugins.getLength(); ++i) {
	        	Node n = plugins.item(i);
	        	if (n instanceof Element) {
		        	Element el = (Element)n;
		        	final String pluginId = el.getAttribute("id");
		        	File[] files = pluginDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith(pluginId) && name.endsWith(".jar");
						}
					});
		        	if (files.length < 0) {
		        		log.error("Cannot find plugin "+pluginId);
		        	} else {
		        		//in case more than one plugin with same id
		        		Arrays.sort(files,new Comparator<File>() {
							@Override
							public int compare(File arg0, File arg1) {
								return arg0.getName().compareTo(arg1.getName());
							}
						});
		        		//File firstFile = files[0];
		        		File lastFile = files[files.length-1];
		        		//String firstVersion = BundleUtils.INSTANCE.getBundleVersion(new Jar(firstFile));
		        		String lastVersion = BundleUtils.INSTANCE.getBundleVersion(new Jar(lastFile)); //may throw IOException
		        		log.info("Adjusting version for plugin "+pluginId+" to "+lastVersion);
		        		el.setAttribute("version", lastVersion);
		        	}
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

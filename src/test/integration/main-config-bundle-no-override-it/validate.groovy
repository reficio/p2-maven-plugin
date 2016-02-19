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

//
// $Id$
//

import aQute.bnd.osgi.Jar
import org.reficio.p2.utils.TestUtils as Util;

File target = new File(basedir, 'target/repository/plugins')
assert target.exists()
assert target.listFiles().size() == 6

def files = target.listFiles().collect { it.name }

assert files.contains("org.mockito.mockito-core_1.9.0.jar")
assert files.contains("org.mockito.mockito-core.source_1.9.0.jar")

assert files.contains("org.objenesis_1.0.0.jar")
assert files.contains("org.objenesis.source_1.0.0.jar")

assert files.contains("org.hamcrest.core_1.1.0.jar")
assert files.contains("org.hamcrest.core.source_1.1.0.jar")

// check the hamcrest dependency
String hamcrestName = "org.hamcrest.core_1.1.0.jar"
assert files.contains(hamcrestName)
Jar hamcrestJar = new Jar(new File(target, hamcrestName));
assert Util.symbolicName(hamcrestJar) == "org.hamcrest.core"
assert Util.version(hamcrestJar) == "1.1.0"

// check the hamcrest dependency source
String hamcrestSourceName = "org.hamcrest.core.source_1.1.0.jar"
assert files.contains(hamcrestSourceName)
Jar hamcrestSourceJar = new Jar(new File(target, hamcrestSourceName));
assert Util.eclipseSourceBundle(hamcrestSourceJar) == "org.hamcrest.core;version=\"1.1.0\";roots:=\".\""
assert Util.symbolicName(hamcrestSourceJar) == "org.hamcrest.core.source"
assert Util.version(hamcrestSourceJar) == "1.1.0"

// check the root jar with its properties
String jarName = "org.mockito.mockito-core_1.9.0.jar";
assert files.contains(jarName)
Jar jar = new Jar(new File(target, jarName));

assert Util.symbolicName(jar) == "org.mockito.mockito-core"
assert Util.version(jar) == "1.9.0"

assert Util.attr(jar, "Export-Package") ==
        "org.mockito.exceptions;uses:=\"org.mockito.exceptions.b" +
        "ase,org.mockito.exceptions.verification.junit,org.mockito.listeners,o" +
        "rg.mockito.exceptions.misusing,org.mockito.exceptions.verification\";v" +
        "ersion=\"1.9.0\",org.mockito.stubbing.answers;uses:=\"org.mockito.except" +
        "ions.base,org.mockito.invocation,org.mockito.stubbing\";version=\"1.9.0" +
        "\",org.mockito.listeners;uses:=\"org.mockito.exceptions\";version=\"1.9.0" +
        "\",org.mockito.runners;uses:=\"org.junit.runner,org.junit.runner.notifi" +
        "cation,org.junit.runner.manipulation\";version=\"1.9.0\",org.mockito.exc" +
        "eptions.base;version=\"1.9.0\",org.mockito.exceptions.verification;uses" +
        ":=\"org.mockito.exceptions.base\";version=\"1.9.0\",org.mockito.configura" +
        "tion;uses:=\"org.mockito,org.mockito.stubbing\";version=\"1.9.0\",org.moc" +
        "kito.invocation;version=\"1.9.0\",org.mockito.exceptions.verification.j" +
        "unit;uses:=\"junit.framework,org.mockito.exceptions.verification\";vers" +
        "ion=\"1.9.0\",org.mockito;uses:=\"org.hamcrest,org.mockito.stubbing,org." +
        "mockito.verification,org.mockito.listeners,org.mockito.exceptions.bas" +
        "e,org.mockito.exceptions,org.mockito.configuration,org.mockito.invoca" +
        "tion\";version=\"1.9.0\",org.mockito.exceptions.misusing;uses:=\"org.mock" +
        "ito.exceptions.base\";version=\"1.9.0\",org.mockito.verification;uses:=\"" +
        "org.mockito.exceptions\";version=\"1.9.0\",org.mockito.stubbing;uses:=\"o" +
        "rg.mockito.invocation\";version=\"1.9.0\""

assert Util.attr(jar, "Import-Package") ==
        "junit.framework;resolution:=optional,org.apache.tools." +
        "ant;resolution:=optional,org.apache.tools.ant.types;resolution:=optio" +
        "nal,org.hamcrest;version=\"[1.0,2.0)\",org.junit;resolution:=optional,o" +
        "rg.junit.internal.runners;resolution:=optional,org.junit.runner;resol" +
        "ution:=optional,org.junit.runner.manipulation;resolution:=optional,or" +
        "g.junit.runner.notification;resolution:=optional,org.junit.runners;re" +
        "solution:=optional,org.junit.runners.model;resolution:=optional,org.m" +
        "ockito;version=\"[1.9,2)\",org.mockito.configuration;version=\"[1.9,2)\"," +
        "org.mockito.exceptions;version=\"[1.9,2)\",org.mockito.exceptions.base;" +
        "version=\"[1.9,2)\",org.mockito.exceptions.misusing;version=\"[1.9,2)\",o" +
        "rg.mockito.exceptions.verification;version=\"[1.9,2)\",org.mockito.exce" +
        "ptions.verification.junit;version=\"[1.9,2)\",org.mockito.invocation;ve" +
        "rsion=\"[1.9,2)\",org.mockito.listeners;version=\"[1.9,2)\",org.mockito.r" +
        "unners;version=\"[1.9,2)\",org.mockito.stubbing;version=\"[1.9,2)\",org.m" +
        "ockito.stubbing.answers;version=\"[1.9,2)\",org.mockito.verification;ve" +
        "rsion=\"[1.9,2)\",org.objenesis;version=\"[1.0,2.0)\""

assert Util.attr(jar, "Manifest-Version") == "1.0"
assert Util.attr(jar, "Bundle-Version") == "1.9.0"

assert Util.attr(jar, "Tool") == "Bnd-0.0.313"
assert Util.attr(jar, "Bnd-LastModified") == "1324069630548"
assert Util.attr(jar, "Bundle-Name").startsWith("Mockito Mock Library for Java. Core bundle requires Hamcrest-core and Objenesis.")
assert Util.attr(jar, "Bundle-ManifestVersion") == "2"
assert Util.attr(jar, "Created-By") == "1.6.0_26 (Apple Inc.)"
assert Util.attr(jar, "Bundle-SymbolicName") == "org.mockito.mockito-core"

// check the root jar source
String jarSourceName = "org.mockito.mockito-core.source_1.9.0.jar"
assert files.contains(jarSourceName)
Jar jarSourceJar = new Jar(new File(target, jarSourceName));
assert Util.eclipseSourceBundle(jarSourceJar) == "org.mockito.mockito-core;version=\"1.9.0\";roots:=\".\""
assert Util.symbolicName(jarSourceJar) == "org.mockito.mockito-core.source"
assert Util.version(jarSourceJar) == "1.9.0"


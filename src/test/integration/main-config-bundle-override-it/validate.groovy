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

assert files.contains("org.test.bundle_1.2.3.jar")
assert files.contains("org.test.bundle.source_1.2.3.jar")

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
String jarName = "org.test.bundle_1.2.3.jar";
assert files.contains(jarName)
Jar jar = new Jar(new File(target, jarName));

assert Util.symbolicName(jar) == "org.test.bundle"
assert Util.version(jar) == "1.2.3"

assert Util.attr(jar, "Export-Package") == "org.mockito.asm"
assert Util.attr(jar, "Import-Package") == "org.test.util"
assert Util.attr(jar, "Export-Service") == "org.mockit.asm.Service"
assert Util.attr(jar, "Bundle-Activator") == "org.mockit.asm.Activator"

assert Util.attr(jar, "Bundle-Name") == "bundle.name"
assert Util.attr(jar, "Bundle-Description") == "bundle.description"
assert Util.attr(jar, "Bundle-License") == "bundle.license"
assert Util.attr(jar, "Bundle-Vendor") == "bundle.vendor"
assert Util.attr(jar, "Bundle-DocURL") == "bundle.docurl"

assert Util.attr(jar, "Implementation-Vendor") == "implementation.vendor"
assert Util.attr(jar, "Implementation-Title") == "implementation.title"
assert Util.attr(jar, "Implementation-Version") == "implementation.version"

assert Util.attr(jar, "Specification-Vendor") == "specification.vendor"
assert Util.attr(jar, "Specification-Title") == "specification.title"
assert Util.attr(jar, "Specification-Version") == "specification.version"

// check the root jar source
String jarSourceName = "org.test.bundle.source_1.2.3.jar"
assert files.contains(jarSourceName)
Jar jarSourceJar = new Jar(new File(target, jarSourceName));
assert Util.eclipseSourceBundle(jarSourceJar) == "org.test.bundle;version=\"1.2.3\";roots:=\".\""
assert Util.symbolicName(jarSourceJar) == "org.test.bundle.source"
assert Util.version(jarSourceJar) == "1.2.3"
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
assert target.listFiles().size() == 2

def files = target.listFiles().collect { it.name }

assert files.contains("org.hibernate.symbolic_2.3.4.jar")
assert files.contains("org.hibernate.symbolic.source_2.3.4.jar")

// check the root jar
String hibernateName = "org.hibernate.symbolic_2.3.4.jar"
assert files.contains(hibernateName)
Jar hibernateJar = new Jar(new File(target, hibernateName));
assert Util.symbolicName(hibernateJar) == "org.hibernate.symbolic"
assert Util.version(hibernateJar) == "2.3.4"

// check the source jar
String hibernateSourceName = "org.hibernate.symbolic.source_2.3.4.jar"
assert files.contains(hibernateSourceName)
Jar hibernateSourceJar = new Jar(new File(target, hibernateSourceName));
assert Util.eclipseSourceBundle(hibernateSourceJar) == "org.hibernate.symbolic;version=\"2.3.4\";roots:=\".\""
assert Util.symbolicName(hibernateSourceJar) == "org.hibernate.symbolic.source"
assert Util.version(hibernateSourceJar) == "2.3.4"

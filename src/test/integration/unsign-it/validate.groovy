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

// verify target
File target = new File(basedir, 'target/repository/plugins')
assert target.exists()
assert target.listFiles().size() == 1

// verify number of artifacts
def files = target.listFiles().collect { it.name }

// verify binary artifact
String jarName = "bcprov-ext_1.46.0.jar"
assert files.contains(jarName)

Jar jar = new Jar(new File(target, jarName));
assert jar.getResource("META-INF/MANIFEST.MF") != null

// proof that the jar was unsigned
assert jar.getResource("META-INF/BCKEY.DSA") == null
assert jar.getResource("META-INF/BCKEY.SF") == null

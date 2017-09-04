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
assert target.listFiles().size() == 3

def files = target.listFiles().collect { it.name }

assert files.contains("slf4j.api_1.7.6.jar")
assert files.contains("ch.qos.logback.classic_1.1.2.jar")

// check the core jar has the custom manifest entries specified in the instructions
logbackCoreName = "ch.qos.logback.core_1.1.2.jar"
assert files.contains(logbackCoreName)
Jar logbackCoreJar = new Jar(new File(target, logbackCoreName));
assert Util.attr(logbackCoreJar, "Foo") == "Bar"

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
import static org.reficio.p2.utils.TestUtils.*;

File target = new File(basedir, 'target/repository/plugins')
assert target.exists()
assert target.listFiles().size() == 12

// <!-- snapshot to snapshot conversion jar -->
validateOriginalSnapshot(jar(target, "org.hibernate.core_4.3.0."), "4.3.0.")
validateOriginalSnapshot(jar(target, "org.hibernate.core.source_4.3.0."), "4.3.0.")

// <!-- snapshot to non-snapshot conversion with override -->
validateVersion(jar(target, "com.hibernato.poolo_1.2.3"), "1.2.3")
validateVersion(jar(target, "com.hibernato.poolo.source_1.2.3"), "1.2.3")

// <!-- non-snapshot to snapshot conversion with override -->
validateRepackedSnapshot(jar(target, "com.hibernato.cacho_3.6.9."), "3.6.9.")
validateRepackedSnapshot(jar(target, "com.hibernato.cacho.source_3.6.9"), "3.6.9.")

// <!-- snapshot to snapshot conversion with override -->
validateRepackedSnapshot(jar(target, "com.hibernato.versiono_2.4.6."), "2.4.6.")
validateRepackedSnapshot(jar(target, "com.hibernato.versiono.source_2.4.6."), "2.4.6.")

// <!-- non-bundle non-snapshot to snapshot conversion with override -->
validateRepackedSnapshot(jar(target, "com.commonso.lango_10.2.2"), "10.2.2.")
validateRepackedSnapshot(jar(target, "com.commonso.lango.source_10.2.2"), "10.2.2.")

// <!-- non-bundle non-snapshot to non-snapshot conversion with override -->
validateVersion(jar(target, "com.commonso.lango_10.2.1"), "10.2.1")
validateVersion(jar(target, "com.commonso.lango.source_10.2.1"), "10.2.1")

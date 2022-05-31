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
package org.reficio.p2.resolver.eclipse;

/**
 * @author Tom Bujok (tom.bujok@gmail.com)<br>
 *         Reficio (TM) - Reestablish your software!<br>
 *         http://www.reficio.org
 * @since 1.1.2
 */
public class EclipseResolutionRequest {

    /**
     * Artifact file name as it appears in the P2 update site.
     * If the artifact file is: "org.junit_4.11.0.v201303080030.jar the id is: "org.junit".
     */
    private final String id;

    /**
     * Artifact file name as it appears in the P2 update site.
     * If the artifact file is: "org.junit_4.11.0.v201303080030.jar the version is:"4.11.0.v201303080030".
     */
    private final String version;

    /**
     * Indicator to include source dependencies
     */
    private final boolean source;

    public EclipseResolutionRequest(String id, String version, boolean source) {
        this.id = id;
        this.version = version;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public boolean isSource() {
        return source;
    }

}

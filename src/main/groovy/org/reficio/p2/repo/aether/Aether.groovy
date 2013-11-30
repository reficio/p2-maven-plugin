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
package org.reficio.p2.repo.aether

import org.reficio.p2.repo.aether.facade.AetherEclipseFacade
import org.reficio.p2.repo.aether.facade.AetherFacade
import org.reficio.p2.repo.aether.facade.AetherSonatypeFacade

/**
 * @author Tom Bujok (tom.bujok@gmail.com)
 * @since 1.1.0
 *        <p/>
 *        Reficio (TM) - Reestablish your software!</br>
 *        http://www.reficio.org
 */
class Aether {

    static AetherFacade facade(def repositorySystemSession) {
        if (repositorySystemSession.getClass().getCanonicalName().contains("org.eclipse")) {
            return new AetherEclipseFacade()
        } else if (repositorySystemSession.getClass().getCanonicalName().contains("org.sonatype")) {
            return new AetherSonatypeFacade()
        } else {
            throw new RuntimeException("Fuck you Maven!")
        }
    }

}

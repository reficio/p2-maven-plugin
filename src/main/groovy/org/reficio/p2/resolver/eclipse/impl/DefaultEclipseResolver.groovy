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
package org.reficio.p2.resolver.eclipse.impl

import org.reficio.p2.logger.Logger
import org.reficio.p2.resolver.eclipse.EclipseResolutionRequest
import org.reficio.p2.resolver.eclipse.EclipseResolutionResponse
import org.reficio.p2.resolver.eclipse.EclipseResolver

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SE_NO_SERIALVERSIONID")
class DefaultEclipseResolver implements EclipseResolver {

    final File target
    final List<?> repositories

    def DefaultEclipseResolver(List<?> repositories, File target) {
        this.target = target
        this.repositories = new ArrayList<?>(repositories)
    }

    @Override
    EclipseResolutionResponse resolve(EclipseResolutionRequest request) {
        List<File> result = []
        result << resolveBundle(request)
        if (request.source) {
            result << resolveSource(request)
        }
        return new EclipseResolutionResponse(result)
    }

    File resolveBundle(EclipseResolutionRequest request) {
        String name = request.id + "_" + request.version + ".jar"
        File result = download(name, target)
        if (!result) {
            throw new RuntimeException("Cannot resolve [$name] from any given repository")
        }
    }

    File resolveSource(EclipseResolutionRequest request) {
        String name = request.id + ".source" + "_" + request.version + ".jar"
        File result = download(name, target)
        if (!result) {
            Logger.getLog().warn("Cannot resolve source [$name] from any given repository")
        }
    }

    File download(String name, File destination) {
        File file = new File(destination, name)
        for (def repository : repositories) {
            if (repository.type == "p2") {
                String url = repository.url + "/plugins/" + name
                Logger.getLog().info("\tDownloading: " + url)
                try {
                    use(FileBinaryCategory)
                            {
                                file << url.toURL()
                            }
                    if (file.exists()) {
                        return file
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

}


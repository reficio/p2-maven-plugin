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
package org.reficio.p2.bundler;

import org.reficio.p2.P2Artifact;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class P2ArtifactMap<T> extends ConcurrentHashMap<P2Artifact, Collection<T>> {

    public void putAll(P2Artifact key, Collection<T> values) {
        this.replace(key, compute(key, (k, oldValues) -> newOrAdd(oldValues, values)));
    }

    public void put(P2Artifact key, T value) {
        this.replace(key, compute(key, (k, oldValues) -> newOrAdd(oldValues, Collections.singleton(value))));
    }

    private Collection<T> newOrAdd(Collection<T> oldValues,
                                   Collection<T> values) {
        if (oldValues == null) {
            return new ArrayList<>(values);
        } else {
            List<T> newList = new ArrayList<>(oldValues);
            newList.addAll(values);
            return newList;
        }
    }

    @Override
    public Collection<T> get(Object key) {
        return Optional.ofNullable(super.get(key))
                .orElseGet(Collections::emptyList);
    }

}

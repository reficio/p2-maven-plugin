#!/bin/bash

#
# Copyright (c) 2020 Reficio (TM) - Reestablish your software! All Rights Reserved.
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e
echo "Running the example projects:"
echo -ne "excludes\t";   mvn -f excludes/pom.xml p2:site > run.log     && echo "[OK]" || echo "[FAILED]"
echo -ne "override\t";   mvn -f override/pom.xml p2:site >> run.log    && echo "[OK]" || echo "[FAILED]"
echo -ne "p2\t";         mvn -f p2/pom.xml p2:site >> run.log          && echo "[OK]" || echo "[FAILED]"
echo -ne "phase   \t";   mvn -f phase/pom.xml compile >> run.log       && echo "[OK]" || echo "[FAILED]"
echo -ne "quickstart\t"; mvn -f quickstart/pom.xml p2:site >> run.log  && echo "[OK]" || echo "[FAILED]"
echo -ne "source  \t";   mvn -f source/pom.xml p2:site >> run.log      && echo "[OK]" || echo "[FAILED]"
echo -ne "transitive\t"; mvn -f transitive/pom.xml p2:site >> run.log  && echo "[OK]" || echo "[FAILED]"

#!/bin/bash
set -e
echo "Running the example projects:"
echo -ne "excludes\t";   mvn -f excludes/pom.xml p2:site > run.log     && echo "[OK]" || echo "[FAILED]"
echo -ne "override\t";   mvn -f override/pom.xml p2:site >> run.log    && echo "[OK]" || echo "[FAILED]"
echo -ne "phase   \t";      mvn -f phase/pom.xml compile >> run.log       && echo "[OK]" || echo "[FAILED]"
echo -ne "quickstart\t"; mvn -f quickstart/pom.xml p2:site >> run.log  && echo "[OK]" || echo "[FAILED]"
echo -ne "source  \t";     mvn -f source/pom.xml p2:site >> run.log      && echo "[OK]" || echo "[FAILED]"
echo -ne "transitive\t"; mvn -f transitive/pom.xml p2:site >> run.log  && echo "[OK]" || echo "[FAILED]"
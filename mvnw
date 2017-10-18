#!/bin/bash
set -e
MVN_DIST_VER=3.5.0
TMP_DIR="/tmp/mvnw/$MVN_DIST_VER"
if [ ! -d "$TMP_DIR" ]; then
    mkdir -p "$TMP_DIR"
    cd "$TMP_DIR" && wget "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$MVN_DIST_VER/apache-maven-$MVN_DIST_VER-bin.tar.gz" -O - | tar -zxv
fi
MVN_PATH=$TMP_DIR/apache-maven-$MVN_DIST_VER/bin
export PATH=$PATH:$MVN_PATH
"$MVN_PATH/mvn" $@


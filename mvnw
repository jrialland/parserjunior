#!/bin/bash
set -e

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export MAVEN_OPTS="-Dhttp.proxyHost=proxypac.si.francetelecom.fr -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxypac.si.francetelecom.fr -Dhttps.proxyPort=8080"
MVN_DIST_VER=3.5.3
TMP_DIR="$HOME/.mvnw/$MVN_DIST_VER"
if [ ! -d "$TMP_DIR" ]; then
    mkdir -p "$TMP_DIR"
    pushd "$TMP_DIR" > /dev/null
    wget "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$MVN_DIST_VER/apache-maven-$MVN_DIST_VER-bin.tar.gz" -O - | tar -zx
    popd > /dev/null
fi
MVN_PATH=$TMP_DIR/apache-maven-$MVN_DIST_VER/bin
export PATH=$PATH:$MVN_PATH
"$MVN_PATH/mvn" $@


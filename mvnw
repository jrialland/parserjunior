#!/bin/bash
set -euo pipefail

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
MVN_DIST_VER=3.5.3

#eventually load global configuration from "$HOME/.mvnwrc"
if [ -f "$HOME/.mvnwrc" ]; then
    source "$HOME/.mvnwrc"
fi

thisscript=$(readlink -m $0)
thisdir=$(dirname $thisscript)

#eventually load project-specific configuration from "./.mvnwrc"
if [ -f "$thisdir/.mvnwrc" ]; then
    source "$thisdir/.mvnwrc"
fi

TMP_DIR="$HOME/.mvnw/$MVN_DIST_VER"
if [ ! -d "$TMP_DIR" ]; then
    mkdir -p "$TMP_DIR"
    pushd "$TMP_DIR" > /dev/null
    wget "https://repo1.maven.org/maven2/org/apache/maven/apache-maven/$MVN_DIST_VER/apache-maven-$MVN_DIST_VER-bin.tar.gz" -O - | tar -zx
    popd > /dev/null
fi

#launch mvn
MVN_PATH=$TMP_DIR/apache-maven-$MVN_DIST_VER/bin
export PATH=$PATH:$MVN_PATH
"$MVN_PATH/mvn" $@


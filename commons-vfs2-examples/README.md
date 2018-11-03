<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# Test Provider(s) with the Shell

## Build modules in the parent folder

    mvn clean install

## Test `http3` and `http3s` providers

    mvn -Pshell -Dhttp3

## Test `http4` and `http4s` providers

    mvn -Pshell -Dhttp4

## Test `http3`, `http3s`, `http4` and `http4s` providers together

    mvn -Pshell -Dhttp3 -Dhttp4

## Example Test Scenario

    mvn -Pshell -Dhttp3 -Dhttp4
    ...
    cd http3://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    cd http3s://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    ...
    cd http4://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    cd http4s://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    ...
    cd http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    cd https://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    cat maven-metadata.xml
    ...


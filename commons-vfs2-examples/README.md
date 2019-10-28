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

## Test `http5` and `http5s` providers

    mvn -Pshell -Dhttp5

## Test all `http*` and `http*s` providers together

    mvn -Pshell -Dhttp3 -Dhttp4 -Dhttp5

## Test `webdav3` providers

    mvn -Pshell -Dwebdav3

## Test `webdav4` and `webdav4s` providers

    mvn -Pshell -Dwebdav4

## Example Test Scenario with HTTP3 / HTTP4 / HTTP5

    mvn -Pshell -Dhttp3 -Dhttp4 -Dhttp5
    ...
    cd http3://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http3s://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http4://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http4s://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http5://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http5s://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    cd https://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    ...
    cat maven-metadata.xml
    ...
    pwd
    > Current folder is https://repo1.maven.org/maven2/org/apache/commons/commons-vfs2
    pwfs
    > FileSystem of current folder is org.apache.commons.vfs2.provider.http.HttpFileSystem@668be11a (root: https://repo1.maven.org/)
    ...
    quit
    


## Test with custom providers configuration

Set `-Dproviders=<custom_providers.xml_resource_name>`.

    mvn -Pshell -Dhttp3 -Dhttp4 -Dproviders=providers-http4-default.xml
    ...
    Custom providers configuration used: file:/tmp/commons-vfs/commons-vfs2-examples/target/classes/providers-http4-default.xml
    VFS Shell null
    cd http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/
    > Current folder is http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2
    pwd
    > Current folder is http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2
    pwfs
    > FileSystem of current folder is org.apache.commons.vfs2.provider.http4.Http4FileSystem@6e012f9b (root: http://repo1.maven.org/)
    ...

## Example Test Scenario with WEBDAV3

(After starting a modern WebDAV server such as Apache Jackrabbit Standalone...)

    mvn -Pshell -Dwebdav3
    cp http3://commons.apache.org/downloads/index.html webdav3://admin:admin@localhost:8080/repository/default/index.html
    cd webdav3://admin:admin@localhost:8080/repository/default/
    ls
    > > Contents of webdav3://admin:admin@localhost:8080/repository/default
    test.txt
    index.html
    cat index.html
    ...

## Example Test Scenario with WEBDAV4

(After starting a modern WebDAV server such as Apache Jackrabbit Standalone...)

    mvn -Pshell -Dwebdav4
    cp http4://commons.apache.org/downloads/index.html webdav4://admin:admin@localhost:8080/repository/default/index.html
    cd webdav4://admin:admin@localhost:8080/repository/default/
    ls
    > > Contents of webdav4://admin:admin@localhost:8080/repository/default
    test.txt
    index.html
    cat index.html
    ...
    cp http4://commons.apache.org/downloads/index.html webdav4s://admin:admin@67d6eab2.ngrok.io/repository/default/index2.html
    cd webdav4://admin:admin@67d6eab2.ngrok.io/repository/default/
    ls
    > > Contents of webdav4://admin:admin@localhost:8080/repository/default
    test.txt
    index.html
    index2.html
    cat index2.html
    ...


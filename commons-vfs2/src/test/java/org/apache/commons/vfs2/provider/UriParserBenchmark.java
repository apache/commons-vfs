/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileSystemException;
import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2)
@Measurement(iterations = 2)
@Fork(2)
public class UriParserBenchmark {

    private static final String PATH_TO_NORMALIZE = "file:///this/../is/a%2flong%2Fpath/./for testing/normlisePath%2fmethod.txt";
    private static final String[] SCHEMES = {"file", "ftp", "ftps", "webdav", "temp", "ram", "http", "https", "sftp", "zip", "jar", "tgz", "gz"};
    private static final String PATH_TO_ENCODE = "file:///this/is/path/to/encode/for/testing/encode.perf";
    private static final char[] ENCODE_RESERVED = new char[] {' ', '#'};

    @Benchmark
    public void normalisePath() throws FileSystemException {
        StringBuilder path = new StringBuilder(PATH_TO_NORMALIZE);
        UriParser.normalisePath(path);
    }

    @Benchmark
    public void extractScheme() throws FileSystemException {
        UriParser.extractScheme(SCHEMES, PATH_TO_NORMALIZE);
    }

    @Benchmark
    public void encode() throws FileSystemException {
        UriParser.encode(PATH_TO_ENCODE, ENCODE_RESERVED);
    }
}

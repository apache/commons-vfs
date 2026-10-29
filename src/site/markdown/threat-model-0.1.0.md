<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--->
# Apache Commons VFS Threat Model

This is a VFS-specific amendment to the
[Apache Commons Threat Model v0.1.0](https://commons.apache.org/threat-model-0.1.0.html),
which governs everything not stated here; report findings per the
[Apache Commons security process](https://commons.apache.org/security.html).

Apache Commons VFS is an in-process Java library that resolves a caller-supplied
URI to a `FileObject` over many backends (local, FTP/FTPS, SFTP, HTTP(S), WebDAV,
HDFS, archive, and in-process schemes). The URI and `FileSystemOptions` are
trusted configuration: connecting to or reading whatever a URI names is doing
what the caller asked, not a vulnerability.

VFS guarantees one thing of its own: `NameScope` subtree confinement — a name
resolved with `CHILD` or `DESCENDENT` cannot escape its base folder. A break of
that is a valid finding; everything else triages against the family model.

VFS adds no resource bounds, no zip-slip defense when a host writes archive
entries to disk, and no sandbox around untrusted URIs (SSRF/LFI is the host's
own choice).

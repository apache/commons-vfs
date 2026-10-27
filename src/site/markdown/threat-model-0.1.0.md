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

**Status:** ACCEPTED 2026-10-27, bound to 2.11.0-SNAPSHOT (`master` @ `08d2eff08`).
This is a VFS-specific amendment to the
[Apache Commons Threat Model v0.1.0](https://svn.apache.org/repos/asf/commons/cms-site/trunk/content/markdown/threat-model-0.1.0.md),
which governs everything not stated here; report findings per the
[Apache Commons security process](https://commons.apache.org/security.html).

Apache Commons VFS is an in-process Java library that resolves a caller-supplied
URI to a `FileObject` over many backends (local, FTP/FTPS, SFTP, HTTP(S),
WebDAV, HDFS, archive, and in-process schemes). By the family model's JDK-parity
principle, connecting to or reading whatever a URI names is doing what the caller
asked and is not a vulnerability: the URI and `FileSystemOptions` are trusted
configuration, while everything a backend returns is untrusted. The only security
properties VFS adds over the JDK are its own `NameScope` subtree confinement
(`CHILD`/`DESCENDENT`; broken by CVE-2025-27553) and the masking of URI passwords
in its own diagnostics such as `getFriendlyURI()` (broken by CVE-2025-30474);
both were fixed in 2.10.0, a break of either is a valid finding, and everything
else is triaged against the family model. VFS adds no resource bounds (archive
bombs and slow servers are the caller's problem), no zip-slip defense when a host
writes archive entries to disk, and no sandbox around untrusted URIs (SSRF/LFI is
the host's own choice). Its SFTP provider leaves `StrictHostKeyChecking` at
`"no"` by default, which the maintainers treat as a developer-only setting, so a
man-in-the-middle report against the bare default is out of model and VFS-867
proposes hardening it. The in-process library is in scope; `commons-vfs2-examples`,
`commons-vfs2-sandbox`, and test code are not. The machine-readable
`threat-model-0.1.0.yaml` beside this page carries the per-entry-point trust
table, security-relevant config knobs, provider tiers, and recurring non-findings
for automated triage.

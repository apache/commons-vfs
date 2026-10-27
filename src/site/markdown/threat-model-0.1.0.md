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

**Status:** ACCEPTED by the maintainers, 2026-10-27. Bound to 2.11.0-SNAPSHOT
(`master` @ `08d2eff08`); a report is triaged against the model as it stood at
the reported version. This is a VFS-specific **amendment** to the
[Apache Commons Threat Model v0.1.0](https://svn.apache.org/repos/asf/commons/cms-site/trunk/content/markdown/threat-model-0.1.0.md)
— it states only what is specific to VFS and relies on the family model for the
shared framework (the JDK-parity principle, the two kinds of component, the
no-untrusted-input rule, and the triage dispositions); where this document is
silent, the family model governs. Report findings privately per the
[Apache Commons security process](https://commons.apache.org/security.html). A
machine-readable triage index — per-entry-point trust, security-relevant config
knobs, claimed and disclaimed properties, known non-findings, and disposition
labels — lives beside this page in `threat-model-0.1.0.yaml`.

**What VFS is.** An in-process Java library that presents one API
(`FileObject`, `FileContent`, `FileSystemManager`) over many backends: local
files, FTP/FTPS, SFTP, HTTP(S), WebDAV, HDFS, layered archives
(zip/jar/tar/gz/bz2), and in-process schemes (ram/res/tmp). The caller names a
resource with a URI and VFS acts on it. It is a library embedded in a host
application, not a daemon or a service.

**Inherited framework.** VFS follows the family model's JDK-parity principle: a
provider that opens the file, connects to the host, or reads the archive its URI
names is doing exactly what the caller asked — like `new File(...)`,
`new URL(...).openConnection()`, or `new ZipInputStream(...)` — and that is not
a vulnerability. The caller, the URI, and the `FileSystemOptions` are trusted
configuration; the API surface is the trust boundary; everything a backend
returns (bytes, directory listings, archive entries, server responses) is
untrusted and is handed back without sanitization. The family rule holds: no
safety against untrusted input by default.

## What VFS itself promises

Two things are VFS's own logic rather than JDK decoration, and both are security
properties — they are where a *real* VFS vulnerability can live, and where both
2025 CVEs (fixed in 2.10.0) landed:

- **`NameScope` subtree confinement.** Resolving a name under `CHILD`,
  `DESCENDENT`, or `DESCENDENT_OR_SELF` must stay within the base subtree, or
  throw (`AbstractFileName.checkName`). CVE-2025-27553 was a bypass. A break is
  path traversal — security-critical, `VALID`.
- **Credential masking.** A password embedded in a URI is shown as `***` in
  VFS's own exception messages and `getFriendlyURI()`
  (`FileSystemException.java:172`, `GenericFileName.appendCredentials`);
  `getURI()` returns the real password by design. CVE-2025-30474 was an FTP path
  that leaked it. A cleartext leak through VFS's own diagnostics is
  security-critical, `VALID`.

Supporting these: `UriParser.normalisePath` collapses `.`/`..`, and FTPS (and
the HttpClient 4/5 providers) validate the server certificate by default.

## What VFS does not promise

- **It is not a sandbox for untrusted URIs.** Resolving a caller-supplied URI
  connects to / reads wherever it points and follows HTTP redirects; SSRF/LFI is
  the host's exposure, by the host's choice (JDK-parity).
- **No resource bounds.** Zip/tar bombs and slow or endless servers can exhaust
  memory or hang, by design; the caller caps size and time.
- **No zip-slip defense on extraction.** VFS normalizes `..` within its own
  namespace, but a host that writes archive entries to disk by raw name must
  verify each destination itself.
- **SFTP host keys are unverified by default.** `StrictHostKeyChecking` defaults
  to `"no"` (`SftpFileSystemConfigBuilder.java:422`). The maintainers treat that
  default as developer-only, so a MITM report against the bare default is
  `OUT-OF-MODEL: non-default-build`; VFS-867 proposes hardening it.
- **No content authentication, no deserialization safety, no symlink jail.** VFS
  does not authenticate fetched bytes, is not a deserialization sink, and follows
  the OS's symlink semantics.
- **False friends.** URI-password "encryption" (`Cryptor`) uses a built-in key —
  obfuscation, not confidentiality; password masking is diagnostic hygiene, not
  access control; `ram`/`res`/`tmp` are naming conveniences, not isolation
  boundaries; `NameScope` confines names, not a backend that lies about what it
  returns.

**Downstream responsibilities.** Allow-list scheme, host, and path when any part
of a URI is untrusted; cap archive entry count, size, and depth and verify
extraction destinations; set timeouts and response-size ceilings; set
`StrictHostKeyChecking` to `"yes"` with a known-hosts source for SFTP on
untrusted networks; log `getFriendlyURI()`, never `getURI()`; do not deserialize
untrusted bytes; treat every backend byte as untrusted.

**Scope.** In-process library only. `commons-vfs2-examples`, `commons-vfs2-sandbox`
(`smb`, `mime`), and test code are out of scope. Provider tiers: **first-class**
(core) — `file`, `ftp`, `ftps`, `sftp`, `http4`/`http4s`, `http5`/`http5s`,
`zip`, `jar`, `tar`/`tgz`/`tbz2`, `gz`, `bz2`, `ram`, `res`, `tmp`, `url`;
**legacy but supported** — `http`/`https` (commons-httpclient-3, superseded by
`http4`/`http5`) and `webdav` (jackrabbit1, superseded by `webdav4`);
**separate optional modules** — `hdfs` and `webdav`/`webdav4` (jackrabbit1/2). A
report against a legacy provider stays in-model unless it is marked
deprecated/out.

**Triage.** Use the family model's disposition set. In short: a break of a
VFS-promised property above is `VALID`; a caller-chosen URI connecting or reading
somewhere, or an HTTP redirect, is `OUT-OF-MODEL: jdk-parity`; an archive bomb or
a `getURI()` password is `BY-DESIGN: property-disclaimed`; the bare SFTP default
is `OUT-OF-MODEL: non-default-build`; a bug rooted in a backend library (Commons
Net, JSch, HttpClient, jackrabbit, Hadoop) is `OUT-OF-MODEL: upstream`; anything
in examples, sandbox, or tests is `OUT-OF-MODEL: unsupported-component`; and a
report that fits none of these is a `MODEL-GAP` that should trigger a revision of
this model. The `threat-model-0.1.0.yaml` sidecar carries the per-entry-point
trust table and the non-findings suppression list for automated triage.

**Revise this model** when a new provider or scheme appears, a security-relevant
default changes (above all the SFTP default, VFS-867), a sandbox provider is
promoted into core, VFS gains a network-service surface, or a report cannot be
routed to exactly one disposition.

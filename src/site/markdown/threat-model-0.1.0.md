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

**Status:** ACCEPTED by the maintainers 2026-10-27. All §14 questions are
resolved; §12 lists what would trigger a revision.

## Header

| Field | Value |
| --- | --- |
| Project | Apache Commons VFS (`org.apache.commons:commons-vfs2` and sibling modules) |
| Version binding | 2.11.0-SNAPSHOT, `master` @ `08d2eff08`. CVE fixes referenced here landed in 2.10.0. |
| Date | 2026-10-26 |
| Author | Drafted via the `threat-model-producer` skill from public artifacts and source, then refined through maintainer review and ratified 2026-10-27. |
| Builds on | the family-level [Apache Commons Threat Model v0.1.0](https://svn.apache.org/repos/asf/commons/cms-site/trunk/content/markdown/threat-model-0.1.0.md). This document is a VFS-specific amendment: where it is silent, the family model governs. Where they conflict, the more recent one wins and the conflict is raised in §14. |

**Version binding.** This model is versioned alongside the project. A report
against VFS version *N* is triaged against the model as it stood at *N*, not at
HEAD.

**Reporting cross-reference.** Findings that fall under §8 (claimed properties)
should be reported privately per the
[Apache Commons security process](https://commons.apache.org/security.html)
(not the public issue tracker). Findings that fall under §3 (out of scope) or
§9 (disclaimed properties) will be closed citing this document. Questions about
whether a published CVE affects your setup go to the
[user mailing list](https://commons.apache.org/mail-lists.html), not the
security address.

**Provenance legend.** Every non-trivial claim carries one tag:

| Tag | Meaning |
| --- | --- |
| *(documented)* | Stated in the project's own docs, Javadoc, source, security page, or a published CVE. Cited inline. |
| *(maintainer)* | Confirmed by a VFS maintainer in response to this draft. Dated, e.g. *(maintainer, 2026-10-27)*. |
| *(inferred)* | Reasoned from code structure or domain knowledge, not yet confirmed. Each has a matching entry in §14. |

The family model is published without these tags because it is ratified. This
VFS model is now ratified too (2026-10-27); the tags are **retained** rather than
dropped, so a report closed as out-of-model can cite a dated *(maintainer)*
authority (e.g. the SFTP-default ruling or a false friend) instead of bare prose.

**Provenance summary.** ~30 *(documented)* / ~39 *(maintainer)* / 0 *(inferred)*.
All §14 questions were ratified on 2026-10-27, so every body claim is now
*(documented)* or *(maintainer)*; the tags are retained for triage citations.

**Purpose (for someone who has never seen the project).** Apache Commons VFS is
an in-process Java library that presents one uniform API — `FileObject`,
`FileContent`, `FileSystemManager` — over many backing file systems. The caller
names a resource with a URI (for example `sftp://user:pass@host/dir/file`,
`zip:file:///tmp/a.zip!/entry`, or `https://host/path`) and VFS resolves it to a
`FileObject` it can read, write, list, copy, delete, or monitor. Backends
include the local file system, FTP/FTPS, SFTP, HTTP(S), WebDAV, HDFS, and
layered archive/compression formats (zip, jar, tar, gzip, bzip2), plus
in-process ones (ram, res, tmp). It is a library embedded in a host application,
not a daemon or a service.

---

## 1. What this model is

The implicit contract between Commons VFS and the application that embeds it:
what VFS assumes about its caller and inputs, the few security properties it
actually promises, the many it does not, and the misuses that are syntactically
possible but outside the intended use. It serves the **downstream integrator**
(which threats VFS has taken on, which are theirs) and the **triager**
(classifying a report, scan hit, or AI finding as valid, out of model, or
disclaimed, with a section to cite).

It is **not** an audit, a bug list, a dependency/CVE register, or a restatement
of the Javadoc. It describes VFS as it *is*, not as it should be.

---

## 2. Scope and intended use

### 2.1 The JDK-parity principle (inherited from the family model)

VFS is governed by the family model's central test *(documented: family model
§"Caller trust and the JDK-parity principle")*: **a call to the VFS API comes
with the same caveat as a call to the JDK — it will usually do what the caller
asks.** A VFS provider that opens the file, connects to the host, or reads the
archive its URI names is behaving like its JDK counterpart
(`new File(path)`, `new URL(url).openConnection()`, `new ZipInputStream(...)`),
not exhibiting a vulnerability. Keeping untrusted content away from an API that
is not documented to accept it is the trusted caller's responsibility. **Do not
report a behavior as a VFS vulnerability if the same behavior would be
considered legitimate for the JDK.** *(documented)*

### 2.2 Two kinds of component, applied to VFS

Using the family model's distinction *(documented)*:

- **Almost all of VFS is a decorator of JDK or backend functionality** (Kind
  one). The providers are thin adapters over `java.io.File`/`java.nio`
  (local, tmp, ram, res), `java.net.URL` (url, http), `java.util.zip` and
  stream codecs (zip/jar/tar/gz/bz2), Apache Commons Net (ftp/ftps), JSch
  (sftp), Apache HttpClient (http4/http5, webdav), and Apache Hadoop (hdfs).
  JDK-parity applies directly to all of these. *(maintainer, 2026-10-27)*
- **Two things in VFS are *not* mere JDK decoration and do make security
  promises:**
  1. **URI/name resolution and `NameScope` confinement** — VFS's own path
     algebra (`UriParser`, `AbstractFileName.checkName`). `NameScope.CHILD` /
     `DESCENDENT` / `DESCENDENT_OR_SELF` promise that a resolved name stays
     within a subtree. *(documented — Javadoc of `NameScope`;
     `AbstractFileName.java:53`)*
  2. **Credential hygiene** — VFS masks passwords embedded in URIs when it
     renders them into its own exceptions and "friendly" URIs.
     *(documented — `FileSystemException.java:172`;
     `GenericFileName.appendCredentials`)*
- **All bytes a provider hands back are raw and untrusted** — file contents,
  directory listings, archive entries, server responses. VFS returns them to
  the caller without sanitization, exactly as the family model describes for
  low-level building blocks. *(maintainer, 2026-10-27)*

These two promise-making exceptions are where VFS can have a *real* VFS
vulnerability, and they are precisely where both 2025 CVEs landed (§8).

### 2.3 Caller trust

The caller is trusted and in-process. The URI string and the
`FileSystemOptions` passed to `FileSystemManager.resolveFile(...)` are treated
as **trusted configuration**, at the same trust level as the argument to
`new File(...)`. *(maintainer, 2026-10-27)* There is no security boundary *inside* a
single `FileSystemManager`; the API surface is the boundary (§4).

### 2.4 Component-family table

| Family | Representative providers / API | Touches outside the process? | Recurring untrusted-input surface | In model? |
| --- | --- | --- | --- | --- |
| Core API & URI/name resolution | `FileSystemManager`, `DefaultFileSystemManager`, `VFS`, `FileObject`, `FileName`, `NameScope`, `UriParser`, `AbstractFileName` | No (pure computation over strings) | The URI string; relative names resolved under `CHILD`/`DESCENDENT` | **Yes — the one family with VFS-authored security promises** |
| Local | `file` (`LocalFileProvider`) | Local filesystem | Paths; symlink targets | Yes (JDK-parity) |
| Remote network | `ftp`, `ftps`, `sftp`, `http`/`https`, `http4`/`http4s`, `http5`/`http5s` | Network | Server responses, listings, redirects; server-presented host keys/certs | Yes (VFS logic); backend bugs → upstream |
| WebDAV | `webdav` (jackrabbit1), `webdav4`/`webdav4s` (jackrabbit2) | Network | Server responses | Yes; separate artifacts, httpclient 3/4 backends |
| HDFS | `hdfs` (commons-vfs2-hdfs) | Network / Hadoop cluster | Cluster responses | Yes (VFS logic); Hadoop → upstream |
| Archive / layered | `zip`, `jar`, `tar`, `tgz`, `tbz2`, `gz`, `bz2` | No (operate on a byte source) | **Archive bytes and entry names** | **Yes — highest untrusted-input surface** |
| In-process | `ram`, `res`, `tmp`, `url` | ram=no; res=classpath; tmp=local FS; url=whatever the URL names | Minimal (but `url` inherits JDK-parity) | Yes |
| Ant tasks | commons-vfs2-ant | Per task | Build script (trusted author) | Yes, at build-time trust |
| Examples | commons-vfs2-examples | Demo | n/a | **No — §3** |
| Sandbox | `mime`, `smb` (commons-vfs2-sandbox) | Varies | n/a | **No — §3** |
| Tests | `src/test/**` across modules | Local | n/a | **No — §3** |

*(documented: module/provider layout; `providers.xml`. In/out classifications
for examples, sandbox, tests confirmed as model policy — maintainer,
2026-10-27.)*

**Support tiers** *(maintainer, 2026-10-27)*: **first-class** (core
`commons-vfs2`) — `file`, `ftp`, `ftps`, `sftp`, `http4`/`http4s`,
`http5`/`http5s`, `zip`, `jar`, `tar`/`tgz`/`tbz2`, `gz`, `bz2`, `ram`, `res`,
`tmp`, `url`; **legacy but supported** — `http`/`https` (original
commons-httpclient-3, superseded by `http4`/`http5`) and `webdav` (jackrabbit1,
superseded by `webdav4`); **separate optional modules** — `hdfs`
(commons-vfs2-hdfs) and `webdav`/`webdav4` (jackrabbit1/2). A report against a
legacy provider stays in-model unless it is marked deprecated/out.

---

## 3. Out of scope (explicit non-goals)

- **Using VFS as a confinement sandbox for an untrusted URI.** VFS is not an
  allow-list, egress filter, or jail. Resolving a caller-supplied URI connects
  to (or reads) whatever scheme/host/path the URI names. If any part of a URI
  can be influenced by an attacker, the resulting SSRF/LFI exposure is the
  host's, by the host's choice — JDK-parity with `new URL(...).openConnection()`.
  *(maintainer, 2026-10-27)*
- **commons-vfs2-examples** — sample code, separately authored, not a supported
  API. Threat-model it separately if ever embedded. *(maintainer, 2026-10-27)*
- **commons-vfs2-sandbox** — the `mime` and `smb` providers ship under an
  artifact explicitly named *sandbox* (component id `vfs-sandbox`); experimental
  and unsupported. *(documented: artifact name; placed out of model —
  maintainer, 2026-10-27)*
- **Test code** across all modules. *(maintainer, 2026-10-27)*
- **Transport-security *management***: choosing/validating host keys, pinning
  certificates, supplying `TrustManager`/`KeyManager`. VFS exposes the knobs
  (§5a) and picks defaults; getting the policy right for a given deployment is
  the caller's. *(maintainer, 2026-10-27)*
- **Dependency and supply-chain hygiene** (backend library versions, pinning,
  signing). Out of scope per the family model and the skill. A vulnerability
  rooted in a backend (Commons Net, JSch, HttpClient, jackrabbit, Hadoop) is
  dispositioned `OUT-OF-MODEL: upstream` (§13), not modeled here.
- **Resource exhaustion from adversarial archives or servers** beyond what VFS
  itself bounds — see §9; VFS makes no resource guarantee.

---

## 4. Trust boundaries and data flow

The **API surface is the trust boundary.** Two distinct kinds of data cross it,
in opposite directions:

1. **Inbound from the caller — trusted configuration.** The URI and
   `FileSystemOptions` handed to `resolveFile(...)`. VFS trusts these the way
   the JDK trusts a path or URL argument. *(maintainer, 2026-10-27)*
2. **Inbound from the backend — untrusted data.** Everything a resolved
   `FileObject` yields: `FileContent` bytes, directory listings, archive entry
   names and sizes, HTTP/WebDAV response bodies and headers, FTP listings.
   VFS passes these to the caller without sanitization. *(maintainer, 2026-10-27)*

**Reachability preconditions per family** (the first test a triager applies to a
tool/AI hit):

- A finding in **URI/name resolution** is in-model only if reachable from a
  relative name resolved under `CHILD`/`DESCENDENT`/`DESCENDENT_OR_SELF` against
  a caller-trusted base — i.e. the host was relying on that scope as a
  containment boundary. *(documented: this is the CVE-2025-27553 shape.)*
- A finding in a **remote provider** is in-model only if reachable from data a
  *malicious backend server* returns (listing, body, redirect, host key/cert),
  **not** from the caller having chosen to point at that server.
- A finding in an **archive/layered provider** is in-model only if reachable
  from archive bytes a trusted caller deliberately opened, and only if it breaks
  a property §8 claims (it almost never does — see §9 on bombs and extraction).
- A finding in **credential handling** is in-model if a password the caller
  embedded in a URI reaches a VFS-produced diagnostic (exception message,
  `getFriendlyURI()`, log) in cleartext. *(documented: CVE-2025-30474 shape.)*

---

## 5. Assumptions about the environment

- **Runtime:** a conformant JVM, Java 8 or later. *(documented:
  `maven.compiler.source=1.8`.)*
- **Thread-safety:** `FileSystemManager` is intended to be shared across
  threads; individual `FileObject`/`FileContent` instances and the caching
  layer carry narrower guarantees and are not guaranteed safe under concurrent
  use. *(maintainer, 2026-10-27)*
- **Memory model:** pure-Java core inherits JVM memory safety — no out-of-bounds
  access on valid or invalid input. Native-backed backends (e.g. compression
  via native libraries pulled by a dependency) are upstream.
  *(maintainer, 2026-10-27)*
- **What VFS *does* to its host (side-effect inventory).** VFS is unusual among
  Commons components in that touching the host is its *purpose*, so the
  inventory is long and partly surprising:
  - Opens local files and directories; creates and deletes temporary files via
    the replicator / `tmp` provider, under `java.io.tmpdir`.
    *(maintainer, 2026-10-27)*
  - Opens TCP sockets for the network providers. *(maintainer, 2026-10-27)*
  - For SFTP, **reads the invoking user's `~/.ssh` material** (known-hosts and,
    depending on configuration, identity/config files) via JSch.
    *(documented: `SftpClientFactory` known-hosts handling; full set
    confirmed — maintainer, 2026-10-27)*
  - Reads system properties and environment used by backends (`user.home`,
    `java.io.tmpdir`, proxy properties, etc.). *(maintainer, 2026-10-27)*
- **What VFS does *not* do to its host (negative claims, confirmed):** no
  process spawning in the core providers (there is no exec/script provider in
  core), no `System.exit`, no signal handlers, no mutation of global locale/FPU
  state. *(maintainer, 2026-10-27)*

### 5a. Configuration variants that matter to security

VFS is really a family of behaviors selected by `FileSystemOptions` and a few
global flags. The knobs that move the security envelope:

| Knob / mode | Component | Default | Effect on the model | Maintainer stance |
| --- | --- | --- | --- | --- |
| `StrictHostKeyChecking` | sftp | **`"no"`** *(documented: `SftpFileSystemConfigBuilder.java:422`)* | With the default, the SSH **host key is not verified**, so SFTP to an untrusted network is open to MITM until the caller sets `"yes"`/`"ask"` and supplies known-hosts. This is the family model's *insecure-default* case. | **Dev-only default (maintainer, 2026-10-27):** not the supported posture, so a report against the bare default is `OUT-OF-MODEL: non-default-build`; a hardening change to the default is proposed ([VFS-867](https://issues.apache.org/jira/browse/VFS-867)). |
| `setKnownHosts` / `userDirIsRoot` | sftp | none / provider-specific | Where host keys are read from; whether the user dir is treated as root (path rooting). | Confirmed — caller's responsibility (maintainer, 2026-10-27) |
| `TrustManager` | ftps | `TrustManagerUtils.getValidateServerCertificateTrustManager()` *(documented: `FtpsFileSystemConfigBuilder.java:120`)* | FTPS **validates** the server certificate by default — a *secure* default. A caller can override with a permissive manager. | Confirmed secure default (maintainer, 2026-10-27) |
| HttpClient defaults | http4/http5, webdav | library defaults | Redirect-following and hostname verification follow the HttpClient version's defaults (verification on). | Confirmed (maintainer, 2026-10-27) |
| `Cryptor` / encrypted URI passwords | core util | off; built-in default key | "Encrypting" a URI password uses a **built-in, non-secret key** unless the caller supplies their own. Obfuscation, not confidentiality (false friend, §9). | Confirmed false friend (maintainer, 2026-10-27) |
| `VFS.isUriStyle()` | core | provider-dependent | Changes trailing-separator handling in name comparison (`checkName` adjusts `baseLen`). Correctness-relevant to the `NameScope` check. | Confirmed; does not weaken `checkName` (maintainer, 2026-10-27) |

This is not build hygiene; it is the recognition that a report is only
meaningful against a named member of this family. The **insecure-default case**
(`StrictHostKeyChecking="no"`) was the wave-1 question that most reshaped the
model (§8, §9, §10, §13); the maintainers ruled it developer-only (§14, Q5.2).

---

## 6. Assumptions about inputs

Default, inherited from the family model: **all input is trusted unless a
specific property says otherwise.** *(documented)* The narrow VFS exceptions are
the `NameScope` confinement and credential-masking promises.

Per-entry-point trust table:

| Component / entry point | Parameter | Attacker-controllable? | Caller must enforce |
| --- | --- | --- | --- |
| `FileSystemManager.resolveFile(uri)` | the URI string (scheme, host, path, userinfo) | **No — trusted configuration** | If any part derives from untrusted input: allow-list scheme, host, and path before calling |
| `FileObject.resolveFile(name, scope)` | relative `name` + `NameScope` | the `name` may be attacker-influenced when used as a containment boundary | Rely on `CHILD`/`DESCENDENT` for confinement; VFS enforces the scope (§8.1) |
| `FileContent.getInputStream()` and friends | backend bytes (file / archive / server) | **Yes** | Treat as untrusted; bound total size and time |
| Archive providers (`zip`/`tar`/…) | archive bytes; entry names, sizes, counts, nesting | **Yes** | Cap entry count / uncompressed size / depth; verify extraction destination (§10) |
| `StaticUserAuthenticator`, URI userinfo | username / password | No — trusted configuration | Keep out of logs; prefer not embedding secrets in URIs |
| sftp handshake | server-presented host key | **Yes (the server chooses it)** | Set `StrictHostKeyChecking` + known-hosts (§5a, §10) |
| ftps/https handshake | server certificate | **Yes (the server chooses it)** | Keep the validating default; supply a `TrustManager` only if you know why |

Size/shape/rate: VFS imposes **no** inherent bound on archive size, entry count,
nesting depth, directory-listing length, or response body size (§9).
*(maintainer, 2026-10-27)*

---

## 7. Adversary model

**In scope:**

- **A malicious backend the caller pointed VFS at** — a hostile FTP/SFTP/HTTP/
  WebDAV/HDFS server returning crafted listings, bodies, redirects, oversized
  or never-ending responses, or a hostile host key/certificate. This is the
  primary VFS-specific adversary. *(maintainer, 2026-10-27)*
- **The author of an archive the host opens** with a layered provider — a
  crafted zip/tar designed to exhaust resources (a "bomb") or to carry
  traversal-shaped entry names. *(maintainer, 2026-10-27)*
- **A party who can influence a relative name** resolved under `CHILD`/
  `DESCENDENT` where the host relies on that scope as a containment boundary.
  *(documented: CVE-2025-27553.)*
- **A party who can read the host's logs/diagnostics**, against the narrow
  promise that VFS does not itself print embedded URI passwords in cleartext.
  *(documented: CVE-2025-30474.)*

**Out of scope:**

- **The in-process caller.** A caller already running in the host JVM has
  trivially full control and is not a meaningful adversary at this layer
  (family model). *(documented)*
- **A party who controls the URI/scheme/host.** Choosing where to connect is
  the host's decision; the resulting SSRF/LFI is the host's exposure, not a VFS
  bug (§3, JDK-parity). *(maintainer, 2026-10-27)*
- **Side-channel and co-tenant adversaries** (timing, memory inspection). Not
  modeled. *(maintainer, 2026-10-27)*

VFS is an in-process library, not a consensus/replicated system, so there is no
Byzantine-participant actor to model. *(maintainer, 2026-10-27)*

---

## 8. Security properties the project provides

For each: the property and its conditions, the violation **symptom**, the
**severity tier** (security-critical vs. correctness-only), and provenance.

### 8.1 `NameScope` subtree confinement
Resolving a name with `NameScope.CHILD`, `DESCENDENT`, or `DESCENDENT_OR_SELF`
returns a `FileObject` only if the (normalized) result lies within the intended
subtree; otherwise it throws. `AbstractFileName.checkName` enforces this on
absolute, normalized paths. *(documented: `NameScope` Javadoc;
`AbstractFileName.java:53-79`.)*
- **Symptom of a break:** a resolved `FileObject` escapes the base subtree —
  path traversal.
- **Tier:** **security-critical.** CVE-2025-27553 (fixed in 2.10.0) was exactly
  a bypass of `DESCENDENT` confinement. *(documented.)*

### 8.2 Credential masking in VFS-produced diagnostics
A password embedded in a URI is replaced by `***` when VFS renders the name
into (a) a `FileSystemException` message — via the `:(?:[^/]+)@` → `:***@`
substitution *(documented: `FileSystemException.java:172-175`, VFS-169)* — and
(b) the "friendly"/public URI from `getFriendlyURI()` /
`appendCredentials(buffer, false)` *(documented:
`GenericFileName.java:66-78`)*. Note the deliberate asymmetry:
`getFriendlyURI()` calls `createURI(false, false)` (masked) while `getURI()`
calls `createURI(false, true)` and returns the **real** password
(round-trippable) by design *(documented: `AbstractFileName.java:148,242,431`)*.
- **Symptom of a break:** a cleartext URI password in a VFS-generated exception,
  friendly URI, or log line.
- **Tier:** **security-critical.** CVE-2025-30474 (fixed in 2.10.0) was an FTP
  path that leaked the URI password in an error message. *(documented.)*

### 8.3 URI/path normalization
`UriParser.normalisePath` collapses `.` and `..` elements so names resolve
deterministically within a file system. *(documented:
`UriParser.java:511-575`.)* This underpins §8.1.
- **Symptom:** inconsistent resolution; a `..` that is not collapsed (can feed a
  §8.1 break).
- **Tier:** correctness, security-relevant (load-bearing for §8.1).

### 8.4 FTPS server-certificate validation by default
FTPS validates the server certificate out of the box. *(documented:
`FtpsFileSystemConfigBuilder.java:120`.)* HTTP(S)/WebDAV delegate verification to
the HttpClient defaults (on). *(maintainer, 2026-10-27)*
- **Symptom:** a connection completing against an invalid/foreign certificate
  with the default configuration.
- **Tier:** security-relevant default.

### 8.5 Memory safety (inherited)
The pure-Java core cannot cause out-of-bounds reads/writes on any input; memory
safety is the JVM's. *(maintainer, 2026-10-27)*
- **Tier:** baseline; a genuine break would be security-critical but would
  almost certainly be a JVM or native-backend (upstream) issue.

### 8.6 Content fidelity
Bytes read through `FileContent` are the backend's bytes, unaltered (round-trip
fidelity for read/write/copy). *(maintainer, 2026-10-27)*
- **Tier:** correctness-only.

**Resource properties — stated as a threshold:** VFS makes **no** resource
guarantee against adversarial input. It does not bound uncompressed archive
size, entry count, nesting depth, directory-listing size, or wall-clock on a
slow/endless server. A hang or OOM on a crafted archive or hostile server is
**not** a violated property (§9). *(maintainer, 2026-10-27)*

---

## 9. Security properties the project does *not* provide

This is the load-bearing section. The family rule quantifies over all of it
*(documented)*: **no safety against untrusted or malicious input, by default; it
is unsafe to feed possibly-malicious input to VFS unless a specific property
above says otherwise, and the host must validate or sanitize at its boundary.**

- **CWE-22 — path traversal on *extraction* ("zip-slip").** VFS normalizes `..`
  inside its *own* name resolution, so a name cannot escape the layered-FS root
  *within VFS's namespace*, and `CHILD`/`DESCENDENT` (§8.1) are available for
  confinement. VFS does **not** protect a host that extracts archive entries to
  the local filesystem with its own logic over raw entry names. Verifying each
  entry's destination before writing is the host's job (family-model Compress
  parity). *(maintainer, 2026-10-27)*
- **CWE-918 — SSRF, and local file disclosure.** Resolving a caller-supplied
  URI connects to / reads whatever it names, and the HTTP providers follow
  redirects per HttpClient. VFS is not an allow-list or egress filter. Attacker
  influence over a URI is the host's SSRF/LFI exposure (§3, §7).
  *(maintainer, 2026-10-27)*
- **Resource exhaustion (archive bombs, hostile/slow servers).** No
  decompression-bomb, entry-count, depth, or response-size defense. The caller
  caps size and time. *(maintainer, 2026-10-27)*
- **SFTP MITM under the default configuration.** `StrictHostKeyChecking`
  defaults to `"no"` (§5a); host keys are unverified until the caller opts in.
  The maintainers rule this default a developer convenience, **not** the
  supported production posture: a MITM report against the bare default is
  `OUT-OF-MODEL: non-default-build`, and a change to harden the default is
  proposed ([VFS-867](https://issues.apache.org/jira/browse/VFS-867), §12).
  *(documented: `SftpFileSystemConfigBuilder.java:422`;
  maintainer, 2026-10-27.)*
- **CWE-502 — deserialization of untrusted data.** VFS is not a deserialization
  sink by design. Do not feed untrusted bytes to Java object deserialization
  regardless of which VFS artifacts are on the classpath. *(maintainer, 2026-10-27)*
- **Authentication/integrity of fetched content.** VFS does not authenticate the
  bytes a remote file system returns; TLS (where used) authenticates the
  transport, not the content's provenance. *(maintainer, 2026-10-27)*
- **Symlink-following policy on the local FS.** The `file` provider follows the
  OS's symlink semantics (JDK-parity); it is not a symlink jail.
  *(maintainer, 2026-10-27)*

### 9a. False friends (features mistaken for security primitives)

- **URI password "encryption" (`Cryptor` / encrypted-password URIs)** looks like
  secret protection but uses a **built-in, non-secret default key** unless the
  caller supplies one. It keeps cleartext out of a config file; it is **not**
  confidentiality against anyone who has the (public) algorithm. *(documented:
  `util/Cryptor`; intent confirmed — maintainer, 2026-10-27)*
- **Password masking (`***`)** is diagnostic hygiene (§8.2), not an access
  control, and not a guarantee that a password never reaches a log by *any*
  path — `getURI()` returns it by design, and CVE-2025-30474 showed a provider
  path that bypassed masking. *(documented.)*
- **`ram` / `res` / `tmp` "virtual" file systems** are naming conveniences, not
  isolation or sandbox boundaries. *(maintainer, 2026-10-27)*
- **`CHILD`/`DESCENDENT` `NameScope`** is a confinement helper for *names*; it
  does not defend against a provider or backend that itself returns out-of-tree
  data (e.g. a malicious server reporting a bogus listing).
  *(maintainer, 2026-10-27)*

**Well-known attack classes for this category, left to the caller** (one line
each, to put integrators on notice): zip-slip and decompression bombs for
archive handling; SSRF via URL resolution and redirect-following; symlink
following on local files; SFTP/FTPS/HTTPS MITM when verification is disabled;
credential leakage via URIs that reach logs. *(maintainer, 2026-10-27)*

---

## 10. Downstream responsibilities

For VFS, "downstream" is the embedding application. For the §5–§7 assumptions to
hold, the host must:

1. **Allow-list** scheme, host, and path whenever any part of a URI derives from
   untrusted input, before calling `resolveFile`. VFS will not do this for you.
2. **Treat all backend bytes as untrusted** — file contents, listings, archive
   entries, response bodies — and validate/encode them for their destination.
3. For **archives**, cap uncompressed size, entry count, and nesting depth, and
   **verify each entry's destination** before writing it to local disk; prefer
   resolving under `DESCENDENT` over trusting raw entry names.
4. Set **timeouts and response-size ceilings** for remote providers; assume a
   server may be slow or endless.
5. For **SFTP to any untrusted network**, set `StrictHostKeyChecking` to `"yes"`
   and supply a known-hosts source; do not ship the `"no"` default into
   production — the maintainers treat it as developer-only, not the supported
   posture (§5a). *(maintainer, 2026-10-27)*
6. Keep the **validating TLS defaults** (FTPS/HTTPS); override a `TrustManager`
   only with a deliberate, understood reason.
7. **Do not embed secrets in URIs** that you then log; if you log a `FileName`,
   use `getFriendlyURI()` (masked), never `getURI()` (cleartext password).
8. **Do not deserialize untrusted bytes**, whatever VFS artifacts are present.
9. Do not rely on **URI password encryption** for confidentiality (§9a).
10. Do not treat VFS as a **sandbox/confinement boundary** for untrusted URIs
    (§3).

---

## 11. Known misuse patterns

1. **Passing an untrusted URI straight to `resolveFile`** — SSRF/LFI. *What it
   looks like:* building a URI from a request parameter. *Why unsafe:* VFS
   connects/reads wherever it points. *Instead:* allow-list scheme/host/path.
2. **Extracting an archive tree to disk with hand-rolled logic over raw entry
   names** — zip-slip. *Instead:* resolve under `DESCENDENT` and/or verify each
   destination path is inside the target directory before writing.
3. **Opening untrusted archives with no entry/size/depth caps** — decompression
   bomb → OOM/hang. *Instead:* impose caps in the host.
4. **SFTP to untrusted hosts at the default `StrictHostKeyChecking="no"`** —
   MITM. *Instead:* enable strict checking with known-hosts.
5. **Logging `getURI()` (or a raw URI built with credentials)** — credential
   leak, bypassing VFS's own masking. *Instead:* log `getFriendlyURI()`.
6. **Relying on URI password encryption as real secret protection** (§9a).
7. **Treating VFS as a confinement sandbox for untrusted URIs** (§3).

---

## 11a. Known non-findings (recurring false positives)

Feed this list to scanners / automated triage as a suppression list.

1. "**`resolveFile(uri)` connects to an attacker-specified host / reads an
   arbitrary local file.**" → `OUT-OF-MODEL: jdk-parity` + `trusted-input`: the
   host chose the URI (§2.1, §6).
2. "**An HTTP provider follows a redirect to another host.**" → `OUT-OF-MODEL:
   jdk-parity`: HttpClient default behavior (§8.4 note, §9).
3. "**A zip/tar provider OOMs or hangs on a crafted archive.**" → `BY-DESIGN:
   property-disclaimed`: VFS makes no resource guarantee (§9).
4. "**`FileSystemException` / a `FileName` is `Serializable`, therefore a
   deserialization gadget.**" → `OUT-OF-MODEL: adversary-not-in-scope`: do not
   deserialize untrusted bytes (§9).
5. "**A `FileName.getURI()` result contains a cleartext password.**" →
   `BY-DESIGN`: `getURI()` is round-trippable by design; the masked
   `getFriendlyURI()` and exception messages are the property (§8.2). *(A leak
   via VFS's **own** exception/friendly-URI path is instead `VALID` — §8.2.)*
6. "**Non-constant-time comparison / non-cryptographic hash / non-CSPRNG.**" →
   `BY-DESIGN`: false friends, not provided (§9a).
7. "**Code in commons-vfs2-examples / -sandbox / tests is insecure.**" →
   `OUT-OF-MODEL: unsupported-component` (§3).
8. "**FTPS/HTTPS connection fails against a self-signed certificate.**" → not a
   finding: that is the validating default working as intended (§8.4).

9. "**SFTP accepts an unknown host key with the default configuration.**" →
   `OUT-OF-MODEL: non-default-build` *(maintainer, 2026-10-27)*: host-key
   verification is opt-in via `StrictHostKeyChecking` (§5a), and the maintainers
   treat the `"no"` default as developer-only. A separate hardening change to
   the default is proposed; that is a §12 model-changing event, not a finding
   against this version.

---

## 12. Conditions that would change this model

- A new provider or URI scheme (especially one that spawns processes or
  evaluates expressions — none exist in core today).
- A **change of default** for a §5a security knob — above all, flipping or
  re-interpreting the SFTP `StrictHostKeyChecking` default.
- Promoting a sandbox provider (`smb`, `mime`) into the supported core.
- Adding built-in archive **extraction** with (or without) traversal defenses.
- Gaining a network-service / daemon surface (VFS is in-process today).
- **Evidence the model is incomplete:** any report that cannot be routed to
  exactly one §13 disposition is a `MODEL-GAP` — the correct response is to
  revise this model (add the property to §8 or §9), not to make an ad-hoc call.

---

## 13. Triage dispositions

The closed set of outcomes for a report, scan hit, or AI finding. Adapted from
the family model's twelve (dropping `no-secure-parsing-yet`, which has no VFS
analog — VFS makes no "secure parsing" promise).

| Disposition | Meaning | Licensed by |
| --- | --- | --- |
| `VALID` | Breaks a property VFS claims (§8), via in-scope input and adversary, and the JDK would not call it legitimate. | §8, §6, §7 |
| `VALID-HARDENING` | No §8 property is broken, but a §11 misuse is easy enough that VFS elects to tighten a default/API. Reported privately; maintainer discretion; usually no CVE. | §11 |
| `OUT-OF-MODEL: trusted-input` | Requires attacker control of a parameter the model marks trusted (a URI the host supplied). | §6, §3 |
| `OUT-OF-MODEL: adversary-not-in-scope` | Requires a capability the model excludes (e.g. the host deserializing hostile bytes, or an in-process caller). | §7 |
| `OUT-OF-MODEL: jdk-parity` | The same behavior would be legitimate for the JDK (connect/read/extract what the arguments name). | §2.1, §11a |
| `OUT-OF-MODEL: unsupported-component` | Lands in examples, sandbox, or test code. | §3 |
| `OUT-OF-MODEL: non-default-build` | Manifests only after a risky, off-by-default option is set (e.g. a permissive `TrustManager`). | §5a |
| `OUT-OF-MODEL: upstream` | Root cause is in a backend library (Commons Net, JSch, HttpClient, jackrabbit, Hadoop), not VFS logic. | §8.5, §3 |
| `BY-DESIGN: property-disclaimed` | Targets a property VFS explicitly does not provide (bombs, SSRF, `getURI()` password, false friends). | §9, §9a |
| `KNOWN-NON-FINDING` | Matches a documented recurring false positive. | §11a |
| `MODEL-GAP` | Fits none of the above — the model needs revision. | §12 |

---

## 14. Maintainer questions (resolved record)

This section is retained as the record of how the model was settled. Each entry
states the answer the maintainers gave and the section it lands in. All waves
were ratified on 2026-10-27; no questions remain open.

### Wave 1 — scope and the insecure default — RATIFIED 2026-10-27

The maintainers ratified the scope framing — Q2.1 (JDK-parity), Q2.2
(examples/sandbox/tests out of model; `commons-vfs2-ant` in model at build-time
trust), Q2.3 (URI and `FileSystemOptions` are trusted configuration) — and ruled
Q5.2 (SFTP `StrictHostKeyChecking="no"`) a **developer convenience, not the
supported posture**: a report against the bare default is
`OUT-OF-MODEL: non-default-build`, and a hardening change to the default is
proposed (§12; [VFS-867](https://issues.apache.org/jira/browse/VFS-867)). The matching body tags are now
*(maintainer, 2026-10-27)*.

### Wave 2 — properties and adversary model — RATIFIED 2026-10-27

The maintainers confirmed the property and adversary-model answers: `NameScope`
confinement is an intended security boundary and a bypass is CVE-worthy, and
`VFS.isUriStyle()` does not weaken `checkName` (Q6.5); credential masking in
VFS's own exceptions / `getFriendlyURI()` is an intended property while
`getURI()` returns the real password by design (Q8.2a); VFS makes no resource
guarantee against adversarial archives or servers (Q7.1); zip-slip destination
checks on extraction are the host's (Q4.1); provider output is raw and
unauthenticated (Q4.3); `Cryptor` URI-password encryption is obfuscation, not
confidentiality (Q8.4); the transport defaults are as described — FTPS and the
HttpClient 4/5 providers verify by default, with host-key/cert policy left to
the caller (Q5.1); and the adversary model (Q7.2) and the disclaimed-property
list (Q9.5) are complete. The matching body tags are now *(maintainer,
2026-10-27)*.

### Wave 3 — environment — RATIFIED 2026-10-27

The maintainers confirmed: the pure-Java core is memory-safe and only a
native-backed backend could break it, as `upstream` (Q9.1); a
`FileSystemManager` is shareable across threads while per-`FileObject` /
`FileContent` state and the cache are not guaranteed safe under concurrent use
(Q9.2); and the host side-effect inventory is correct — VFS opens files and
sockets, creates temp files under `java.io.tmpdir`, and reads the user's
`~/.ssh` material for SFTP, while the core providers spawn no processes, install
no signal handlers, and never call `System.exit` (Q9.3). The matching body tags
are now *(maintainer, 2026-10-27)*.

### Meta

- **Q-meta-1 — provider support tiers.** *Resolved (maintainer, 2026-10-27):*
  the proposed tiering was confirmed and recorded in §2.4 — first-class core
  providers; `http`/`https` and `webdav` legacy-but-supported; `hdfs` and
  `webdav`/`webdav4` as separate optional modules. A report against a legacy
  provider stays in-model unless it is marked deprecated/out.
- **Q-meta-2 — publication venue.** *Resolved (maintainer, 2026-10-27):*
  published at `src/site/markdown/threat-model-0.1.0.md` (versioned filename, to
  match the family model), wired into the site menu (`site.xml`) and linked from
  the security page (`security.xml`).
- **Q-meta-3 — relationship to the family model and `security.xml`.** *Resolved
  (maintainer, 2026-10-27):* this document sits alongside the family model as the
  VFS-specific amendment, and `security.xml` keeps listing fixed CVEs while
  linking here for the model.

---

## 15. Appendix — source back-map

Where each existing security statement lands in this model (per the skill's
§3.1a requirement that the model be a strict superset of prior policy).

| Existing statement | Source | Lands in |
| --- | --- | --- |
| "No safety against untrusted input by default; host validates at its boundary." | Family model §"…does not provide" | §9 (intro) |
| JDK-parity principle; two kinds of component. | Family model §scope | §2.1, §2.2 |
| False friends (digests, RNG, interpolation, crypto). | Family model §9 | §9a (VFS-specific: `Cryptor`, masking, virtual FS, `NameScope`) |
| Triage dispositions (12 labels). | Family model §triage | §13 (11, minus `no-secure-parsing-yet`) |
| CVE-2025-27553 — path traversal via `NameScope.DESCENDENT`. | `security.xml`; CVE record | §8.1 (break of the claimed property) |
| CVE-2025-30474 — FTP error message reveals URI password. | `security.xml`; CVE record | §8.2 (break of the claimed property) |
| "Binary patches are never provided; build from source." | `security.xml`; family model | Reporting cross-reference (Header) |
| Safe-deserialization pointer (Commons IO). | `security.xml` | §9 (CWE-502) |
| Report privately to the Apache Security Team. | `SECURITY.md`; `security.xml` | Header (reporting cross-reference) |

A machine-readable companion for automated triage is provided alongside this
file as `threat-model-0.1.0.yaml` (§13 dispositions, §6 entry-point trust, §5a knobs,
§8/§9 properties, §11a non-findings). The prose here is canonical; the sidecar
is a derived index.

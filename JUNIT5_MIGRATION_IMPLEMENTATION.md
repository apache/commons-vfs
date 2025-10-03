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
-->
# JUnit 5 Migration - Implementation Guide

This document provides step-by-step instructions for implementing the JUnit 5 migration.

## Prerequisites

- Git branch: `feature/junit5-migration`
- Maven 3.6+
- JDK 8+ (test with 8, 11, 17, 21)
- IDE with JUnit 5 support (IntelliJ IDEA, Eclipse, VS Code)

## Phase 1: Preparation

### Step 1.1: Create Feature Branch

```bash
cd /path/to/commons-vfs
git checkout master
git pull origin master
git checkout -b feature/junit5-migration
```

### Step 1.2: Document Baseline

```bash
# Run full test suite
mvn clean test 2>&1 | tee baseline-test-results.txt

# Extract test counts
grep "Tests run:" baseline-test-results.txt > baseline-test-counts.txt

# Run with coverage
mvn clean test jacoco:report
cp -r target/site/jacoco baseline-coverage/

# Record metrics
echo "Baseline captured on $(date)" > baseline-metrics.txt
echo "Test counts:" >> baseline-metrics.txt
cat baseline-test-counts.txt >> baseline-metrics.txt
```

### Step 1.3: Identify Files to Migrate

```bash
# Find JUnit 4 test files
find . -name "*.java" -path "*/test/*" -exec grep -l "import org.junit.Test;" {} \; > junit4-files.txt

# Find JUnit 5 test files (already migrated)
find . -name "*.java" -path "*/test/*" -exec grep -l "import org.junit.jupiter.api.Test;" {} \; > junit5-files.txt

# Find JUnit 3 test files
find . -name "*.java" -path "*/test/*" -exec grep -l "extends TestCase" {} \; > junit3-files.txt

# Find files using junit.framework
find . -name "*.java" -path "*/test/*" -exec grep -l "junit.framework" {} \; > junit-framework-files.txt

# Review the lists
wc -l junit4-files.txt junit5-files.txt junit3-files.txt junit-framework-files.txt
```

## Phase 2: Migrate Standalone Tests

### Step 2.1: Migrate Filter Tests

**Files to migrate (commons-vfs2/src/test/java/org/apache/commons/vfs2/filter/):**

All files in this directory are already JUnit 5! ✅ No work needed.

### Step 2.2: Migrate Utility Tests

**Files to check:**
- `commons-vfs2/src/test/java/org/apache/commons/vfs2/util/`

Most are already JUnit 5. Verify with:
```bash
grep -r "import org.junit.Test;" commons-vfs2/src/test/java/org/apache/commons/vfs2/util/
```

### Step 2.3: Migrate JUnit 4 Test Files

**Identified JUnit 4 files (43 total):**

Create a script to help with migration:

```bash
#!/bin/bash
# migrate-test.sh - Helper script for JUnit 4 to 5 migration

FILE=$1

if [ -z "$FILE" ]; then
    echo "Usage: $0 <test-file.java>"
    exit 1
fi

echo "Migrating $FILE..."

# Backup original
cp "$FILE" "$FILE.backup"

# Replace imports
sed -i.bak 's/import org\.junit\.Test;/import org.junit.jupiter.api.Test;/g' "$FILE"
sed -i.bak 's/import org\.junit\.Before;/import org.junit.jupiter.api.BeforeEach;/g' "$FILE"
sed -i.bak 's/import org\.junit\.After;/import org.junit.jupiter.api.AfterEach;/g' "$FILE"
sed -i.bak 's/import org\.junit\.BeforeClass;/import org.junit.jupiter.api.BeforeAll;/g' "$FILE"
sed -i.bak 's/import org\.junit\.AfterClass;/import org.junit.jupiter.api.AfterAll;/g' "$FILE"
sed -i.bak 's/import org\.junit\.Ignore;/import org.junit.jupiter.api.Disabled;/g' "$FILE"
sed -i.bak 's/import static org\.junit\.Assert\./import static org.junit.jupiter.api.Assertions./g' "$FILE"

# Replace annotations
sed -i.bak 's/@Before$/@BeforeEach/g' "$FILE"
sed -i.bak 's/@After$/@AfterEach/g' "$FILE"
sed -i.bak 's/@BeforeClass$/@BeforeAll/g' "$FILE"
sed -i.bak 's/@AfterClass$/@AfterAll/g' "$FILE"
sed -i.bak 's/@Ignore$/@Disabled/g' "$FILE"

# Clean up backup files
rm "$FILE.bak"

echo "Migration complete. Review changes and test."
echo "Original backed up to: $FILE.backup"
```

**Usage:**
```bash
chmod +x migrate-test.sh

# Migrate one file
./migrate-test.sh commons-vfs2/src/test/java/org/apache/commons/vfs2/UrlStructureTests.java

# Test the migration
mvn test -Dtest=UrlStructureTests

# If successful, commit
git add commons-vfs2/src/test/java/org/apache/commons/vfs2/UrlStructureTests.java
git commit -m "Migrate UrlStructureTests to JUnit 5"
```

**Batch migration:**
```bash
# Migrate all JUnit 4 files in batches
while IFS= read -r file; do
    echo "Processing $file"
    ./migrate-test.sh "$file"
    
    # Extract test class name
    classname=$(basename "$file" .java)
    
    # Run test
    mvn test -Dtest="$classname" -q
    
    if [ $? -eq 0 ]; then
        echo "✅ $classname passed"
        git add "$file"
    else
        echo "❌ $classname failed - reverting"
        git checkout "$file"
    fi
done < junit4-files.txt

# Commit successful migrations
git commit -m "Batch migrate JUnit 4 tests to JUnit 5"
```

### Step 2.4: Handle Exception Testing

**Find tests using expected attribute:**
```bash
grep -r "@Test(expected" commons-vfs2/src/test/java/
```

**Manual migration required for these patterns:**

Before:
```java
@Test(expected = FileSystemException.class)
public void testException() throws Exception {
    fileObject.doSomething();
}
```

After:
```java
@Test
public void testException() {
    assertThrows(FileSystemException.class, () -> {
        fileObject.doSomething();
    });
}
```

### Step 2.5: Verify Standalone Test Migration

```bash
# Run all tests
mvn clean test

# Compare test counts
grep "Tests run:" target/surefire-reports/*.txt | wc -l

# Should match baseline
diff baseline-test-counts.txt <(grep "Tests run:" target/surefire-reports/*.txt)
```

## Phase 3: Migrate Test Suite Infrastructure

### Step 3.1: Analyze Current Infrastructure

**Key files:**
- `commons-vfs2/src/test/java/org/apache/commons/vfs2/AbstractProviderTestCase.java`
- `commons-vfs2/src/test/java/org/apache/commons/vfs2/AbstractTestSuite.java`
- `commons-vfs2/src/test/java/org/apache/commons/vfs2/ProviderTestSuite.java`

**Files that extend AbstractProviderTestCase:**
```bash
grep -r "extends AbstractProviderTestCase" commons-vfs2/src/test/java/ | cut -d: -f1 | sort -u
```

### Step 3.2: Design New Architecture

**Option A: Use @Suite (Recommended)**

Create new base class:
```java
// AbstractProviderTestCase.java (migrated)
package org.apache.commons.vfs2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

public abstract class AbstractProviderTestCase {
    
    private DefaultFileSystemManager manager;
    private ProviderTestConfig providerConfig;
    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    
    @BeforeEach
    void checkCapabilities() {
        Capability[] caps = getRequiredCapabilities();
        if (caps != null) {
            FileSystem fs = getFileSystem();
            for (Capability cap : caps) {
                Assumptions.assumeTrue(
                    fs.hasCapability(cap),
                    () -> "Skipping test - provider does not have capability: " + cap
                );
            }
        }
    }
    
    protected abstract Capability[] getRequiredCapabilities();
    
    // Getters and setters
    protected FileObject getReadFolder() { return readFolder; }
    protected FileObject getWriteFolder() { return writeFolder; }
    protected FileObject getBaseFolder() { return baseFolder; }
    protected DefaultFileSystemManager getManager() { return manager; }
    protected ProviderTestConfig getProviderConfig() { return providerConfig; }
    protected FileSystem getFileSystem() throws FileSystemException {
        return readFolder.getFileSystem();
    }
    
    public void setConfig(DefaultFileSystemManager manager, 
                         ProviderTestConfig providerConfig,
                         FileObject baseFolder, 
                         FileObject readFolder, 
                         FileObject writeFolder) {
        this.manager = manager;
        this.providerConfig = providerConfig;
        this.baseFolder = baseFolder;
        this.readFolder = readFolder;
        this.writeFolder = writeFolder;
    }
}
```

### Step 3.3: Migrate Provider Test Suites

**Example: LocalProviderTestCase**

Before (JUnit 3):
```java
public class LocalProviderTestCase extends AbstractProviderTestConfig {
    public static Test suite() throws Exception {
        final ProviderTestSuite testSuite = new ProviderTestSuite(new LocalProviderTestCase());
        testSuite.addTests(FileNameTests.class);
        testSuite.addTests(UrlTests.class);
        return testSuite;
    }
}
```

After (JUnit 5):
```java
@Suite
@SelectClasses({
    FileNameTests.class,
    UrlTests.class,
    PermissionsTests.class
})
public class LocalProviderTestSuite {
    // Configuration can be done via @BeforeAll in a nested class if needed
}
```

### Step 3.4: Migrate *Tests.java Files

**Files to migrate:**
```bash
find commons-vfs2/src/test/java -name "*Tests.java" | grep -v TestCase
```

**Pattern:**

Before:
```java
public class UrlStructureTests extends AbstractProviderTestCase {
    @Test
    public void testFolderURL() throws Exception {
        // test code
    }
}
```

After (no change needed - just ensure it uses JUnit 5 @Test):
```java
import org.junit.jupiter.api.Test;

public class UrlStructureTests extends AbstractProviderTestCase {
    @Test
    public void testFolderURL() throws Exception {
        // test code
    }
}
```

### Step 3.5: Update Surefire Configuration

**Remove exclusions from all module POMs:**

```bash
# Find all pom.xml files with the exclusion
grep -r "exclude.*Tests.java" */pom.xml

# Edit each file to remove the exclusion
# commons-vfs2/pom.xml
# commons-vfs2-hdfs/pom.xml
# commons-vfs2-jackrabbit1/pom.xml
# commons-vfs2-jackrabbit2/pom.xml
```

**Change:**
```xml
<excludes>
  <exclude>**/RunTest.java</exclude>
  <exclude>**/*$*</exclude>
  <exclude>**/*Tests.java</exclude>  <!-- REMOVE THIS LINE -->
</excludes>
```

**To:**
```xml
<excludes>
  <exclude>**/RunTest.java</exclude>
  <exclude>**/*$*</exclude>
</excludes>
```

### Step 3.6: Test Infrastructure Migration

```bash
# Run full test suite
mvn clean test

# Verify test counts
grep "Tests run:" target/surefire-reports/*.txt | awk '{sum+=$3} END {print "Total tests:", sum}'

# Compare with baseline
# Should be HIGHER because *Tests.java files are now included
```

## Phase 4: Cleanup

### Step 4.1: Remove Vintage Engine

**Edit all module POMs:**
- commons-vfs2/pom.xml
- commons-vfs2-ant/pom.xml
- commons-vfs2-hdfs/pom.xml
- commons-vfs2-jackrabbit1/pom.xml
- commons-vfs2-jackrabbit2/pom.xml
- commons-vfs2-sandbox/pom.xml

**Remove:**
```xml
<dependency>
  <groupId>org.junit.vintage</groupId>
  <artifactId>junit-vintage-engine</artifactId>
  <scope>test</scope>
</dependency>
```

**Test after removal:**
```bash
mvn clean test

# All tests should still pass
# If any fail, they weren't properly migrated
```

### Step 4.2: Remove Remaining JUnit 4 Imports

```bash
# Find any remaining JUnit 4 imports
grep -r "import org.junit.Test;" commons-vfs2/src/test/java/
grep -r "import org.junit.Before;" commons-vfs2/src/test/java/
grep -r "import org.junit.After;" commons-vfs2/src/test/java/

# Should return no results
```

### Step 4.3: Update Documentation

**Files to update:**
- README.md
- BUILDING.txt
- src/site/xdoc/testing.xml

**Changes:**
- Update JUnit version references
- Update test execution examples
- Update developer documentation

## Phase 5: Validation

### Step 5.1: Multi-JDK Testing

```bash
# Java 8
export JAVA_HOME=/path/to/jdk8
mvn clean test

# Java 11
export JAVA_HOME=/path/to/jdk11
mvn clean test

# Java 17
export JAVA_HOME=/path/to/jdk17
mvn clean test

# Java 21
export JAVA_HOME=/path/to/jdk21
mvn clean test
```

### Step 5.2: Profile Testing

```bash
# Test with different profiles
mvn test -Pwebdav
mvn test -Pftp
mvn test -Psftp
mvn test -Phttp
```

### Step 5.3: Coverage Analysis

```bash
mvn clean test jacoco:report

# Compare with baseline
diff -r baseline-coverage/ target/site/jacoco/

# Coverage should be maintained or improved
```

### Step 5.4: Performance Testing

```bash
# Time the test execution
time mvn clean test

# Compare with baseline
# JUnit 5 should be similar or faster
```

## Troubleshooting

### Issue: Tests Silently Skipped

**Symptom:** Test count is lower than baseline

**Solution:**
```bash
# Enable verbose logging
mvn test -X | grep "Skipping"

# Check for Assumptions failures
grep "Assumption" target/surefire-reports/*.txt
```

### Issue: Capability Checking Not Working

**Symptom:** Tests run when they shouldn't

**Solution:**
- Verify `@BeforeEach` is called before tests
- Check `Assumptions.assumeTrue()` logic
- Add logging to capability checking

### Issue: Test Suite Not Running All Tests

**Symptom:** Some tests not executed

**Solution:**
- Verify `@Suite` configuration
- Check test class names match pattern
- Ensure tests are public and have @Test annotation

## Commit Strategy

### Recommended Commits

1. "Prepare for JUnit 5 migration - document baseline"
2. "Migrate filter tests to JUnit 5"
3. "Migrate utility tests to JUnit 5"
4. "Migrate standalone provider tests to JUnit 5"
5. "Refactor AbstractProviderTestCase for JUnit 5"
6. "Migrate test suite infrastructure to JUnit 5"
7. "Remove Surefire exclusions for *Tests.java"
8. "Remove junit-vintage-engine dependency"
9. "Update documentation for JUnit 5"
10. "Final validation and cleanup"

### Pull Request

**Title:** "Migrate from JUnit 4 to JUnit 5"

**Description:**
```
This PR completes the migration from JUnit 4 to JUnit 5 (Jupiter).

## Changes
- Migrated 43 test files from JUnit 4 to JUnit 5
- Refactored custom test suite infrastructure
- Removed junit-vintage-engine dependency
- Updated documentation

## Testing
- All tests pass on Java 8, 11, 17, 21
- Test count matches baseline: XXXX tests
- Code coverage maintained: XX%
- All profiles tested (webdav, ftp, sftp, http)

## Migration Details
See JUNIT5_MIGRATION_PLAN.md for complete details.
```

## Success Checklist

- [ ] All 43 JUnit 4 files migrated
- [ ] Custom test suite infrastructure refactored
- [ ] *Tests.java files no longer excluded
- [ ] junit-vintage-engine removed
- [ ] All tests pass
- [ ] Test count matches or exceeds baseline
- [ ] Coverage maintained
- [ ] Multi-JDK testing passed
- [ ] Profile testing passed
- [ ] Documentation updated
- [ ] Code reviewed
- [ ] CI green

## Next Steps After Merge

1. Monitor CI for any issues
2. Update release notes
3. Consider blog post about migration
4. Share lessons learned with Apache Commons community


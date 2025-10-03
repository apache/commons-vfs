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
# Apache Commons VFS - JUnit 4 to JUnit 5 Migration Plan

## Executive Summary

This document outlines a comprehensive plan to migrate Apache Commons VFS from JUnit 4 to JUnit 5 (JUnit Jupiter). The project currently uses a **hybrid approach** with JUnit Vintage engine supporting legacy JUnit 3/4 tests alongside newer JUnit 5 tests. The goal is to complete the migration to pure JUnit 5.

**Current JUnit Version:** 5.13.4 (via commons-parent 88)  
**Target:** Full JUnit 5 (Jupiter) migration  
**Mockito Version:** 4.11.0

---

## 1. Current State Assessment

### 1.1 Test File Statistics

- **Total test files:** 204 Java test files across all modules
- **JUnit 4 tests:** 43 files using `import org.junit.Test;`
- **JUnit 5 tests:** 83 files using `import org.junit.jupiter.api.Test;`
- **JUnit 3 legacy:** 1 file extending `TestCase` directly (AbstractProviderTestCase)
- **Files using junit.framework:** ~50 files (mostly for custom test suite infrastructure)

### 1.2 Module Breakdown

| Module | Test Files | Status |
|--------|-----------|--------|
| commons-vfs2 | ~170 | Mixed JUnit 3/4/5 |
| commons-vfs2-ant | ~5 | Mostly JUnit 5 |
| commons-vfs2-hdfs | ~8 | Mixed JUnit 4/5 |
| commons-vfs2-jackrabbit1 | ~10 | Mixed JUnit 4/5 |
| commons-vfs2-jackrabbit2 | ~9 | Mixed JUnit 4/5 |
| commons-vfs2-sandbox | ~2 | JUnit 5 |

### 1.3 Current Dependencies

All modules already have JUnit 5 dependencies configured:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.junit.vintage</groupId>
  <artifactId>junit-vintage-engine</artifactId>
  <scope>test</scope>
</dependency>
```

The `junit-vintage-engine` allows running JUnit 3 and 4 tests alongside JUnit 5 tests.

### 1.4 Key Patterns Identified

#### JUnit 4 Patterns Found:
- `import org.junit.Test;` - 43 occurrences
- `@BeforeEach/@AfterEach` already in use (JUnit 5 style)
- Mixed imports: Some files import both `org.junit.Test` and `org.junit.jupiter.api.Assertions`

#### JUnit 3 Legacy Infrastructure:
- **AbstractProviderTestCase** extends `junit.framework.TestCase`
- **AbstractTestSuite** extends `junit.extensions.TestSetup`
- Custom test suite mechanism using reflection to discover `testXxx()` methods
- Files ending with `*Tests.java` are excluded from Surefire (JUnit 3 suite classes)

#### Current Assertion Usage:
- `import static org.junit.jupiter.api.Assertions.*` - 199 occurrences
- `import static org.junit.Assert.*` - 2 occurrences (minimal)

### 1.5 Special Considerations

1. **Custom Test Suite Infrastructure**: The project uses a custom JUnit 3-style test suite mechanism (`AbstractTestSuite`, `ProviderTestSuite`) that dynamically discovers and runs tests. This is a significant architectural component.

2. **Excluded Test Files**: Maven Surefire is configured to exclude `**/*Tests.java` files with the comment:
   ```xml
   <!-- Need to port fully to JUnit 4 or 5. -->
   <!-- *Tests.java files with @Test methods should not be run since these 
        classes are in fact JUnit 3 classes used in custom JUnit 3 test suites. -->
   ```

3. **Test Inheritance Hierarchy**: Many test classes extend `AbstractProviderTestCase`, which provides common test infrastructure and capability checking.

---

## 2. Dependency Changes

### 2.1 Current State (Already Configured)

✅ **Good News:** All modules already have JUnit 5 dependencies configured correctly via the parent POM's dependency management section.

**Parent POM (pom.xml):**
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.junit</groupId>
      <artifactId>junit-bom</artifactId>
      <type>pom</type>
      <scope>import</scope>
      <version>${commons.junit.version}</version>  <!-- 5.13.4 -->
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 2.2 Required Changes

#### Phase 1: Keep Vintage Engine (Incremental Migration)
**No changes needed** - Current configuration supports both JUnit 4 and JUnit 5.

#### Phase 2: Remove Vintage Engine (After Full Migration)

**Action:** Remove `junit-vintage-engine` dependency from all module POMs:

**Files to update:**
- `commons-vfs2/pom.xml`
- `commons-vfs2-ant/pom.xml`
- `commons-vfs2-hdfs/pom.xml`
- `commons-vfs2-jackrabbit1/pom.xml`
- `commons-vfs2-jackrabbit2/pom.xml`
- `commons-vfs2-sandbox/pom.xml`

**Remove:**
```xml
<dependency>
  <groupId>org.junit.vintage</groupId>
  <artifactId>junit-vintage-engine</artifactId>
  <scope>test</scope>
</dependency>
```

### 2.3 Mockito Compatibility

✅ **Current Mockito version (4.11.0) is fully compatible with JUnit 5.**

No changes needed for Mockito dependencies.

---

## 3. Code Migration Strategy

### 3.1 Migration Phases

The migration should be done in **3 phases** due to the custom test suite infrastructure:

#### Phase 1: Migrate Standalone Test Classes (Low Risk)
Migrate test classes that:
- Use `@Test` annotations (JUnit 4 or 5)
- Don't participate in custom test suites
- Are standalone test classes (e.g., filter tests, utility tests)

**Estimated files:** ~80 files

#### Phase 2: Refactor Custom Test Suite Infrastructure (Medium Risk)
Replace JUnit 3-style test suite infrastructure with JUnit 5 alternatives:
- Convert `AbstractProviderTestCase` from extending `TestCase` to using JUnit 5
- Replace `AbstractTestSuite` with JUnit 5 `@Suite` or `@Nested` tests
- Migrate `*Tests.java` classes to proper JUnit 5 test classes

**Estimated files:** ~10 infrastructure files + ~40 test suite classes

#### Phase 3: Final Cleanup (Low Risk)
- Remove vintage engine dependency
- Remove Surefire exclusions for `*Tests.java`
- Update documentation

### 3.2 Annotation Migration Mapping

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `@Test` | `@Test` | Different package: `org.junit.jupiter.api.Test` |
| `@Before` | `@BeforeEach` | Already mostly migrated |
| `@After` | `@AfterEach` | Already mostly migrated |
| `@BeforeClass` | `@BeforeAll` | Method must be static |
| `@AfterClass` | `@AfterAll` | Method must be static |
| `@Ignore` | `@Disabled` | Can add reason: `@Disabled("reason")` |
| `@Test(expected=Ex.class)` | `assertThrows(Ex.class, () -> {...})` | More explicit |
| `@Test(timeout=100)` | `@Timeout(value=100, unit=MILLISECONDS)` | More flexible |
| `@RunWith` | `@ExtendWith` | Extension model |
| `@Rule` | `@ExtendWith` | Extension model |

### 3.3 Assertion Migration

✅ **Good News:** Most assertions are already using JUnit 5 style!

**Current state:**
- 199 files use `org.junit.jupiter.api.Assertions`
- Only 2 files use `org.junit.Assert`

**Remaining work:** Update the 2 files still using JUnit 4 assertions.

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `Assert.assertEquals(expected, actual)` | `Assertions.assertEquals(expected, actual)` | Same signature |
| `Assert.assertTrue(condition)` | `Assertions.assertTrue(condition)` | Same signature |
| `Assert.assertNull(object)` | `Assertions.assertNull(object)` | Same signature |
| `Assert.fail(message)` | `Assertions.fail(message)` | Same signature |

**Static imports:** Change from:
```java
import static org.junit.Assert.*;
```
to:
```java
import static org.junit.jupiter.api.Assertions.*;
```

### 3.4 Exception Testing Migration

**JUnit 4 pattern:**
```java
@Test(expected = FileSystemException.class)
public void testSomething() throws Exception {
    // code that should throw
}
```

**JUnit 5 pattern:**
```java
@Test
public void testSomething() {
    assertThrows(FileSystemException.class, () -> {
        // code that should throw
    });
}
```

**Benefits:**
- Can verify exception message
- Can perform additional assertions after exception
- More explicit and readable

### 3.5 Custom Test Suite Migration

This is the **most complex part** of the migration.

**Current Architecture:**
```
AbstractTestSuite (extends junit.extensions.TestSetup)
  └── ProviderTestSuite
        └── Uses reflection to find testXxx() methods
        └── Creates AbstractProviderTestCase instances dynamically
```

**Migration Options:**

#### Option A: JUnit 5 @Suite (Recommended)
```java
@Suite
@SelectClasses({
    UrlTests.class,
    UriTests.class,
    NamingTests.class,
    // ... other test classes
})
public class ProviderTestSuite {
    // Suite configuration
}
```

#### Option B: JUnit 5 @Nested Tests
```java
public class ProviderTests {
    @Nested
    class UrlTests {
        @Test void testUrl1() { }
        @Test void testUrl2() { }
    }
    
    @Nested
    class UriTests {
        @Test void testUri1() { }
    }
}
```

#### Option C: Parameterized Tests
For tests that run against multiple providers:
```java
@ParameterizedTest
@MethodSource("providerConfigs")
void testWithProvider(ProviderTestConfig config) {
    // Test logic
}
```

**Recommendation:** Use **Option A (@Suite)** for maintaining the current test organization structure, combined with **Option C (Parameterized Tests)** for provider-specific tests.

### 3.6 AbstractProviderTestCase Migration

**Current:**
```java
public abstract class AbstractProviderTestCase extends TestCase {
    // Uses reflection to invoke test methods
    // Capability checking in runTest()
}
```

**Proposed JUnit 5 approach:**
```java
public abstract class AbstractProviderTestCase {
    
    @BeforeEach
    void checkCapabilities(TestInfo testInfo) {
        // Check if provider has required capabilities
        // Skip test if not: Assumptions.assumeTrue(hasCapability)
    }
    
    // Test methods use @Test annotation
}
```

**Key changes:**
- Remove `extends TestCase`
- Use `@BeforeEach` for capability checking
- Use `Assumptions.assumeTrue()` to skip tests conditionally
- Remove reflection-based test method invocation

---

## 4. Build Configuration

### 4.1 Maven Surefire Plugin

**Current version:** Inherited from commons-parent 88 (likely 3.x)

**JUnit 5 Requirements:**
- Maven Surefire Plugin 2.22.0+ (for JUnit 5 support)
- Maven Surefire Plugin 3.0.0+ (recommended for best JUnit 5 support)

**Verification:**
```bash
mvn help:evaluate -Dexpression=maven-surefire-plugin.version -q -DforceStdout
```

### 4.2 Current Surefire Configuration

**commons-vfs2/pom.xml:**
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <trimStackTrace>false</trimStackTrace>
    <systemPropertyVariables>
      <test.basedir>target/test-classes/test-data</test.basedir>
      <test.basedir.res>test-data</test.basedir.res>
      <derby.stream.error.file>target/derby.log</derby.stream.error.file>
    </systemPropertyVariables>
    <excludes>
      <!-- Main class -->
      <exclude>**/RunTest.java</exclude>
      <!-- inner classes -->
      <exclude>**/*$*</exclude>
      <!-- Need to port fully to JUnit 4 or 5. -->
      <exclude>**/*Tests.java</exclude>
    </excludes>
  </configuration>
</plugin>
```

### 4.3 Required Configuration Changes

#### Phase 1: No changes needed
Current configuration works with JUnit 5 via vintage engine.

#### Phase 2: After migrating *Tests.java files
Remove the exclusion:
```xml
<excludes>
  <!-- Main class -->
  <exclude>**/RunTest.java</exclude>
  <!-- inner classes -->
  <exclude>**/*$*</exclude>
  <!-- REMOVE THIS AFTER MIGRATION: -->
  <!-- <exclude>**/*Tests.java</exclude> -->
</excludes>
```

#### Phase 3: Optimize for JUnit 5
Add JUnit 5-specific configuration:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <configuration>
    <trimStackTrace>false</trimStackTrace>
    <systemPropertyVariables>
      <test.basedir>target/test-classes/test-data</test.basedir>
      <test.basedir.res>test-data</test.basedir.res>
      <derby.stream.error.file>target/derby.log</derby.stream.error.file>
    </systemPropertyVariables>
    <excludes>
      <exclude>**/RunTest.java</exclude>
      <exclude>**/*$*</exclude>
    </excludes>
    <!-- JUnit 5 specific configuration -->
    <properties>
      <configurationParameters>
        junit.jupiter.execution.parallel.enabled = false
        junit.jupiter.testinstance.lifecycle.default = per_method
      </configurationParameters>
    </properties>
  </configuration>
</plugin>
```

### 4.4 Profile Updates

Update all profiles that configure Surefire (java17, no-test-hdfs, etc.) to remove `*Tests.java` exclusions after migration.

---

## 5. Migration Approach

### 5.1 Recommended Strategy: **Incremental Migration with Vintage Engine**

**Rationale:**
- Lower risk - tests continue to run during migration
- Can be done module by module or even file by file
- Easier to identify and fix issues
- Can be spread across multiple commits/PRs

### 5.2 Migration Workflow

#### Step 1: Prepare (1-2 days)
- [ ] Create feature branch: `feature/junit5-migration`
- [ ] Document current test execution baseline
- [ ] Run full test suite and record results
- [ ] Set up automated testing in CI

#### Step 2: Migrate Standalone Tests (1-2 weeks)
- [ ] Identify standalone test files (not part of custom suites)
- [ ] Create migration script/tool for common patterns
- [ ] Migrate files in batches of 10-20
- [ ] Run tests after each batch
- [ ] Commit working batches

**Priority order:**
1. Filter tests (commons-vfs2/src/test/java/org/apache/commons/vfs2/filter/)
2. Utility tests (commons-vfs2/src/test/java/org/apache/commons/vfs2/util/)
3. Provider-specific tests (already using @Test)
4. Other standalone tests

#### Step 3: Refactor Test Suite Infrastructure (2-3 weeks)
- [ ] Design new JUnit 5 test suite architecture
- [ ] Create prototype with one provider test suite
- [ ] Migrate AbstractProviderTestCase to JUnit 5
- [ ] Migrate AbstractTestSuite to @Suite
- [ ] Update all *Tests.java files to use new infrastructure
- [ ] Remove Surefire exclusions
- [ ] Verify all tests still run

#### Step 4: Clean Up (1 week)
- [ ] Remove junit-vintage-engine dependency
- [ ] Remove any remaining JUnit 4 imports
- [ ] Update documentation
- [ ] Update BUILDING.txt if needed
- [ ] Final full test suite run

#### Step 5: Review and Merge (1 week)
- [ ] Code review
- [ ] CI validation on multiple JDK versions
- [ ] Merge to main branch

**Total estimated time:** 5-8 weeks

### 5.3 Alternative: Big Bang Migration

**Not recommended** due to:
- High risk of breaking tests
- Difficult to debug issues
- Large PR difficult to review
- Custom test suite infrastructure complexity

---

## 6. Risk Assessment

### 6.1 High Risk Areas

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Custom test suite infrastructure breaks** | HIGH - Many tests won't run | Thorough testing of new infrastructure; Keep vintage engine during migration |
| **Capability checking mechanism fails** | MEDIUM - Tests run when they shouldn't | Careful migration of AbstractProviderTestCase; Add integration tests |
| **Provider-specific tests fail** | MEDIUM - Coverage gaps | Test each provider separately; Maintain test coverage metrics |
| **Reflection-based test discovery breaks** | HIGH - Tests silently skipped | Use JUnit 5 test discovery; Verify test count before/after |

### 6.2 Medium Risk Areas

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Assertion behavior differences** | LOW - Most assertions identical | Review JUnit 5 assertion docs; Test edge cases |
| **Test lifecycle differences** | MEDIUM - Setup/teardown issues | Careful review of @BeforeEach/@AfterEach usage |
| **Timeout handling changes** | LOW - Few timeout tests | Review timeout tests individually |
| **Exception testing changes** | LOW - Better in JUnit 5 | Use assertThrows consistently |

### 6.3 Low Risk Areas

- Annotation changes (straightforward mapping)
- Static imports (IDE can help)
- Most assertion migrations (already done)
- Dependency updates (already configured)

### 6.4 Compatibility Concerns

✅ **No breaking changes expected for:**
- Java 8 compatibility (JUnit 5 supports Java 8+)
- Maven compatibility (Surefire 3.x supports JUnit 5)
- CI/CD pipelines (transparent to build systems)
- IDE support (all major IDEs support JUnit 5)

---

## 7. Testing Strategy

### 7.1 Pre-Migration Baseline

**Before starting migration:**
```bash
# Run full test suite
mvn clean test

# Record metrics:
# - Total tests run
# - Tests passed
# - Tests failed
# - Tests skipped
# - Execution time
```

**Expected baseline (approximate):**
- Total tests: ~2000 tests
- All tests should pass
- Execution time: Record for comparison

### 7.2 During Migration Validation

**After each migration batch:**
```bash
# Run tests for migrated module
mvn clean test -pl commons-vfs2

# Verify:
# - Same number of tests run
# - All tests pass
# - No new failures
# - No tests silently skipped
```

**Test count verification:**
```bash
# Before migration
mvn test | grep "Tests run:"

# After migration  
mvn test | grep "Tests run:"

# Compare counts - should be identical
```

### 7.3 Post-Migration Validation

**After complete migration:**

1. **Full test suite execution:**
   ```bash
   mvn clean install
   ```

2. **Multi-JDK testing:**
   ```bash
   # Test with Java 8
   mvn clean test -Djava.version=1.8
   
   # Test with Java 11
   mvn clean test -Djava.version=11
   
   # Test with Java 17
   mvn clean test -Djava.version=17
   
   # Test with Java 21
   mvn clean test -Djava.version=21
   ```

3. **Profile testing:**
   ```bash
   # Test with different profiles
   mvn test -Pwebdav
   mvn test -Pftp
   mvn test -Psftp
   mvn test -Phttp
   ```

4. **Coverage analysis:**
   ```bash
   mvn clean test jacoco:report
   # Compare coverage before/after migration
   ```

5. **Performance comparison:**
   - Compare test execution times
   - Should be similar or faster (JUnit 5 is generally faster)

### 7.4 Success Criteria

✅ **Migration is successful when:**

1. **All tests pass** - No new test failures
2. **Test count matches** - Same number of tests run before/after
3. **Coverage maintained** - Code coverage ≥ baseline
4. **No vintage engine** - All tests run on JUnit 5 platform
5. **Documentation updated** - BUILDING.txt, README.md reflect JUnit 5
6. **CI passes** - All CI builds green on all supported JDK versions
7. **Performance acceptable** - Test execution time within 10% of baseline

### 7.5 Rollback Plan

**If migration fails:**

1. **Immediate rollback:**
   ```bash
   git revert <migration-commits>
   ```

2. **Partial rollback:**
   - Keep vintage engine
   - Revert problematic modules
   - Continue with working modules

3. **Investigation:**
   - Analyze test failures
   - Review migration approach
   - Adjust strategy

---

## 8. Implementation Checklist

### Phase 1: Preparation
- [ ] Create feature branch
- [ ] Document baseline test metrics
- [ ] Set up CI for migration branch
- [ ] Review this migration plan with team

### Phase 2: Standalone Test Migration
- [ ] Migrate filter tests (~15 files)
- [ ] Migrate utility tests (~10 files)
- [ ] Migrate provider tests with @Test (~40 files)
- [ ] Migrate other standalone tests (~15 files)
- [ ] Verify test counts and coverage

### Phase 3: Infrastructure Migration
- [ ] Design new test suite architecture
- [ ] Migrate AbstractProviderTestCase
- [ ] Migrate AbstractTestSuite
- [ ] Migrate ProviderTestSuite
- [ ] Migrate all *Tests.java files (~40 files)
- [ ] Remove Surefire exclusions
- [ ] Verify all provider tests run

### Phase 4: Cleanup
- [ ] Remove vintage engine from all POMs
- [ ] Remove JUnit 4 imports
- [ ] Update documentation
- [ ] Final test suite validation

### Phase 5: Review and Merge
- [ ] Code review
- [ ] CI validation
- [ ] Merge to master

---

## 9. Resources and References

### JUnit 5 Documentation
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 Migration Guide](https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4)
- [JUnit 5 Assertions](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/Assertions.html)

### Maven Surefire
- [Surefire JUnit 5 Support](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html)

### Migration Tools
- IntelliJ IDEA: Built-in JUnit 4 to 5 migration
- Eclipse: JUnit 5 migration wizard
- OpenRewrite: Automated migration recipes

---

## 10. Timeline and Effort Estimate

| Phase | Duration | Effort (person-days) |
|-------|----------|---------------------|
| Preparation | 1-2 days | 1-2 |
| Standalone Tests | 1-2 weeks | 5-10 |
| Infrastructure | 2-3 weeks | 10-15 |
| Cleanup | 1 week | 3-5 |
| Review & Merge | 1 week | 2-3 |
| **Total** | **5-8 weeks** | **21-35 days** |

**Assumptions:**
- 1 developer working part-time (50% allocation)
- Includes testing and validation time
- Includes code review time

---

## Conclusion

The Apache Commons VFS project is well-positioned for JUnit 5 migration:
- ✅ Dependencies already configured
- ✅ Most assertions already migrated
- ✅ Many tests already using JUnit 5
- ⚠️ Custom test suite infrastructure requires careful migration

**Recommended approach:** Incremental migration with vintage engine, focusing on infrastructure migration as the critical path.

**Expected outcome:** Fully migrated to JUnit 5 with improved test maintainability and modern testing practices.


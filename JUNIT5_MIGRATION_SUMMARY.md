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
# JUnit 5 Migration - Executive Summary

## Quick Facts

- **Current State:** Hybrid JUnit 3/4/5 with Vintage Engine
- **JUnit Version:** 5.13.4 (via commons-parent 88)
- **Total Test Files:** 204
- **Already JUnit 5:** 83 files (41%)
- **Still JUnit 4:** 43 files (21%)
- **Legacy JUnit 3:** ~50 files (24%) - Custom test suite infrastructure
- **Estimated Effort:** 21-35 person-days over 5-8 weeks

## Current Status

### ✅ What's Already Done

1. **Dependencies Configured** - All modules have JUnit 5 dependencies
2. **Vintage Engine Active** - Supports running JUnit 3/4/5 tests together
3. **Most Assertions Migrated** - 199 files use JUnit 5 assertions
4. **Many Tests Migrated** - 83 files already use JUnit 5 `@Test`

### ⚠️ What Needs Migration

1. **43 files** still using JUnit 4 `@Test` annotation
2. **Custom test suite infrastructure** using JUnit 3 patterns:
   - `AbstractProviderTestCase` extends `junit.framework.TestCase`
   - `AbstractTestSuite` extends `junit.extensions.TestSetup`
   - Reflection-based test discovery
   - ~40 `*Tests.java` files excluded from Surefire

## Key Challenges

### 1. Custom Test Suite Infrastructure (HIGH COMPLEXITY)

The project uses a sophisticated JUnit 3-style test suite mechanism:

```
AbstractTestSuite
  └── ProviderTestSuite
        └── Dynamically discovers testXxx() methods
        └── Creates test instances via reflection
        └── Capability-based test filtering
```

**Impact:** This affects ~40 test classes and is the critical path for migration.

**Solution:** Migrate to JUnit 5 `@Suite` + `@ParameterizedTest` for provider tests.

### 2. Files Excluded from Surefire

Maven Surefire excludes `**/*Tests.java` with comment:
> "Need to port fully to JUnit 4 or 5."

These are JUnit 3 suite classes that need complete refactoring.

### 3. Capability-Based Test Filtering

`AbstractProviderTestCase.runTest()` checks provider capabilities before running tests. This needs to be migrated to JUnit 5 `Assumptions`.

## Recommended Approach

### Strategy: **Incremental Migration with Vintage Engine**

**Why?**
- Lower risk - tests continue running during migration
- Can be done module by module
- Easier to identify and fix issues
- Can spread work across multiple PRs

### Migration Phases

#### Phase 1: Standalone Tests (1-2 weeks)
Migrate ~80 standalone test files that don't use custom suite infrastructure.

**Files to migrate:**
- Filter tests (commons-vfs2/src/test/java/org/apache/commons/vfs2/filter/)
- Utility tests (commons-vfs2/src/test/java/org/apache/commons/vfs2/util/)
- Provider-specific tests already using `@Test`

**Risk:** LOW - These are straightforward migrations

#### Phase 2: Test Suite Infrastructure (2-3 weeks)
Refactor the custom test suite infrastructure to use JUnit 5.

**Key changes:**
- Migrate `AbstractProviderTestCase` to use `@BeforeEach` + `Assumptions`
- Replace `AbstractTestSuite` with `@Suite`
- Convert `*Tests.java` files to proper JUnit 5 test classes
- Remove Surefire exclusions

**Risk:** MEDIUM-HIGH - This is the critical path

#### Phase 3: Cleanup (1 week)
- Remove `junit-vintage-engine` dependency
- Remove remaining JUnit 4 imports
- Update documentation

**Risk:** LOW - Final validation

## Migration Patterns

### Pattern 1: Simple @Test Migration

**Before (JUnit 4):**
```java
import org.junit.Test;

public class MyTest {
    @Test
    public void testSomething() {
        // test code
    }
}
```

**After (JUnit 5):**
```java
import org.junit.jupiter.api.Test;

public class MyTest {
    @Test
    public void testSomething() {
        // test code
    }
}
```

### Pattern 2: Exception Testing

**Before (JUnit 4):**
```java
@Test(expected = FileSystemException.class)
public void testException() throws Exception {
    // code that throws
}
```

**After (JUnit 5):**
```java
@Test
public void testException() {
    assertThrows(FileSystemException.class, () -> {
        // code that throws
    });
}
```

### Pattern 3: Lifecycle Methods

**Before (JUnit 4):**
```java
@Before
public void setUp() { }

@After
public void tearDown() { }

@BeforeClass
public static void setUpClass() { }

@AfterClass
public static void tearDownClass() { }
```

**After (JUnit 5):**
```java
@BeforeEach
public void setUp() { }

@AfterEach
public void tearDown() { }

@BeforeAll
public static void setUpClass() { }

@AfterAll
public static void tearDownClass() { }
```

### Pattern 4: AbstractProviderTestCase Migration

**Before (JUnit 3):**
```java
public abstract class AbstractProviderTestCase extends TestCase {
    @Override
    protected void runTest() throws Throwable {
        // Check capabilities
        if (!hasCapability(cap)) {
            return; // Skip test
        }
        super.runTest();
    }
}
```

**After (JUnit 5):**
```java
public abstract class AbstractProviderTestCase {
    @BeforeEach
    void checkCapabilities() {
        Capability[] caps = getRequiredCapabilities();
        if (caps != null) {
            for (Capability cap : caps) {
                Assumptions.assumeTrue(
                    getFileSystem().hasCapability(cap),
                    "Skipping test - missing capability: " + cap
                );
            }
        }
    }
}
```

## Dependencies

### Current (Keep During Migration)

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

### After Migration (Remove Vintage)

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
<!-- Remove vintage engine -->
```

## Build Configuration

### Maven Surefire

**Current exclusions (to be removed after migration):**
```xml
<excludes>
  <exclude>**/RunTest.java</exclude>
  <exclude>**/*$*</exclude>
  <exclude>**/*Tests.java</exclude>  <!-- REMOVE AFTER MIGRATION -->
</excludes>
```

**After migration:**
```xml
<excludes>
  <exclude>**/RunTest.java</exclude>
  <exclude>**/*$*</exclude>
</excludes>
```

## Testing Strategy

### Validation Checklist

Before migration:
- [ ] Run full test suite: `mvn clean test`
- [ ] Record test count and execution time
- [ ] Record code coverage metrics

During migration (after each batch):
- [ ] Run tests: `mvn clean test -pl <module>`
- [ ] Verify test count matches baseline
- [ ] Verify all tests pass
- [ ] Check for silently skipped tests

After migration:
- [ ] Full test suite: `mvn clean install`
- [ ] Multi-JDK testing (Java 8, 11, 17, 21)
- [ ] Profile testing (webdav, ftp, sftp, http)
- [ ] Coverage analysis: `mvn jacoco:report`
- [ ] Performance comparison

### Success Criteria

✅ Migration is successful when:
1. All tests pass
2. Test count matches baseline
3. Code coverage ≥ baseline
4. No vintage engine dependency
5. Documentation updated
6. CI passes on all JDK versions

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Test suite infrastructure breaks | Keep vintage engine during migration; thorough testing |
| Tests silently skipped | Verify test counts before/after; use JUnit 5 test discovery |
| Capability checking fails | Careful migration of AbstractProviderTestCase; integration tests |
| Provider tests fail | Test each provider separately; maintain coverage metrics |

## Timeline

| Phase | Duration | Deliverable |
|-------|----------|-------------|
| Preparation | 1-2 days | Migration plan, baseline metrics |
| Standalone Tests | 1-2 weeks | ~80 files migrated, tests passing |
| Infrastructure | 2-3 weeks | Custom suite migrated, all tests running |
| Cleanup | 1 week | Vintage removed, docs updated |
| Review & Merge | 1 week | PR merged |
| **Total** | **5-8 weeks** | **Full JUnit 5 migration** |

## Next Steps

1. **Review this plan** with the team
2. **Create feature branch**: `feature/junit5-migration`
3. **Document baseline**: Run tests and record metrics
4. **Start Phase 1**: Migrate standalone tests in small batches
5. **Iterate**: Commit working batches, run tests frequently

## Resources

- **Full Migration Plan:** See `JUNIT5_MIGRATION_PLAN.md`
- **JUnit 5 User Guide:** https://junit.org/junit5/docs/current/user-guide/
- **JUnit 5 Migration Guide:** https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4
- **Maven Surefire JUnit 5:** https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html

## Conclusion

The Apache Commons VFS project is **well-positioned** for JUnit 5 migration:
- Dependencies already configured ✅
- Most assertions already migrated ✅
- Many tests already using JUnit 5 ✅
- Clear migration path identified ✅

**Main challenge:** Custom test suite infrastructure requires careful refactoring.

**Recommended approach:** Incremental migration over 5-8 weeks with vintage engine support during transition.

**Expected outcome:** Modern, maintainable test suite using JUnit 5 best practices.


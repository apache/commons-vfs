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

# JUnit 5 Migration - Phase 3b Plan: Full Test Suite Infrastructure Migration

## Current State (After Phase 3a)

### What Works
- ✅ All 3122 tests run successfully via JUnit 3 Vintage engine
- ✅ `AbstractProviderTestCase` has JUnit 5 lifecycle methods (`@BeforeEach`, `@AfterEach`)
- ✅ Individual test methods can be run directly via JUnit 5 (with limitations)
- ✅ Zero test regressions

### Current Architecture

The test infrastructure uses JUnit 3's `TestSuite` pattern:

```
LocalProviderTestCase (config)
    └── public static Test suite()
            └── new ProviderTestSuite(config)
                    └── extends AbstractTestSuite
                            └── extends junit.extensions.TestSetup
                                    ├── setUp() - creates FileSystemManager, folders
                                    ├── addTests(Class) - reflection to find testXxx() methods
                                    ├── tearDown() - cleanup
                                    └── TestSuite containing:
                                            ├── ContentTests instance 1 (testFile1)
                                            ├── ContentTests instance 2 (testFile2)
                                            ├── ProviderReadTests instance 1 (testRead)
                                            └── ... (3122 total test instances)
```

**Key Dependencies on JUnit 3:**
1. `AbstractProviderTestCase extends TestCase` - provides `setName()`, `getName()`, implements `Test` interface
2. `AbstractTestSuite extends TestSetup` - provides suite-level `setUp()`/`tearDown()`
3. `TestSuite.addTest(Test)` - requires tests to implement `junit.framework.Test`
4. Reflection-based test discovery via `testXxx()` method naming convention
5. `setMethod(Method)` to tell each test instance which method to execute

## Why Full Migration is Complex

### Challenge 1: Suite-Level Setup
- Current: `AbstractTestSuite.setUp()` creates ONE `FileSystemManager` for ALL tests
- Current: All test instances are configured via `setConfig()` before any test runs
- JUnit 5: `@BeforeAll` runs once, but test instances are created per-method by default
- **Impact**: Need to redesign how test instances share the file system manager

### Challenge 2: Test Instance Creation
- Current: `AbstractTestSuite.addTests()` creates one instance per test method
- Current: Each instance has `setMethod()` called to know which method to run
- Current: `runTest()` uses reflection to invoke the method
- JUnit 5: Test instances are created automatically, one per `@Test` method
- **Impact**: The `setMethod()` pattern doesn't work in JUnit 5

### Challenge 3: Dynamic Test Discovery
- Current: Reflection finds all `public void testXxx()` methods
- Current: Works for classes that don't have `@Test` annotations
- JUnit 5: Requires `@Test` annotation or `@TestFactory` for dynamic tests
- **Impact**: All 3122 test methods need `@Test` annotations OR we need `@TestFactory`

### Challenge 4: Provider Configuration
- Current: Each provider has a `*ProviderTestCase` with `suite()` method
- Current: Configuration is passed to `ProviderTestSuite` constructor
- JUnit 5: No direct equivalent to `suite()` method pattern
- **Impact**: Need new way to configure provider-specific settings

## Migration Path Options

### Option A: Minimal Change (Recommended for Phase 3b)

**Keep JUnit 3 infrastructure, improve documentation and tooling**

**Pros:**
- Zero risk of breaking tests
- All 3122 tests continue to work
- Can be done incrementally
- Maintains backward compatibility

**Cons:**
- Still depends on Vintage engine
- Can't remove `extends TestCase`
- Not "pure" JUnit 5

**Implementation:**
1. ✅ Add JUnit 5 lifecycle methods (Phase 3a - DONE)
2. Document the hybrid approach
3. Add `@Test` annotations to all test methods (for future migration)
4. Create migration guide for future phases

### Option B: Gradual Migration with @TestFactory

**Convert one provider at a time to use `@TestFactory`**

**Example for LocalProviderTestCase:**

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalProviderTests {
    
    private DefaultFileSystemManager manager;
    private FileObject readFolder;
    private FileObject writeFolder;
    
    @BeforeAll
    void setUpFileSystem() throws Exception {
        // Create manager, folders (from AbstractTestSuite.setUp())
        manager = new DefaultFileSystemManager();
        // ... setup code ...
    }
    
    @TestFactory
    Stream<DynamicTest> contentTests() {
        return Stream.of(
            dynamicTest("testFile1", () -> {
                ContentTests test = new ContentTests();
                test.setConfig(manager, config, baseFolder, readFolder, writeFolder);
                test.testFile1();
            }),
            dynamicTest("testFile2", () -> {
                ContentTests test = new ContentTests();
                test.setConfig(manager, config, baseFolder, readFolder, writeFolder);
                test.testFile2();
            })
            // ... more tests ...
        );
    }
    
    @AfterAll
    void tearDownFileSystem() throws Exception {
        // Cleanup (from AbstractTestSuite.tearDown())
    }
}
```

**Pros:**
- Pure JUnit 5 for migrated providers
- Can be done incrementally
- Removes Vintage dependency for migrated providers

**Cons:**
- Requires migrating 45 provider test cases
- Need to maintain both patterns during transition
- Complex refactoring for each provider

### Option C: Full Rewrite with @Nested Tests

**Completely redesign test architecture**

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalProviderTests {
    
    private TestContext context;
    
    @BeforeAll
    void setUp() {
        context = new TestContext(new LocalProviderTestConfig());
    }
    
    @Nested
    class ContentTests {
        @Test
        void testFile1() {
            // Test implementation with context
        }
        
        @Test
        void testFile2() {
            // Test implementation with context
        }
    }
    
    @Nested
    class ProviderReadTests {
        @Test
        void testRead() {
            // Test implementation
        }
    }
}
```

**Pros:**
- Clean JUnit 5 architecture
- No Vintage dependency
- Modern test organization

**Cons:**
- Requires rewriting all test classes
- High risk of breaking tests
- Months of work
- Need to verify all 3122 tests still work

## Recommended Approach for Phase 3b

**Hybrid Approach: Document and Prepare**

1. **Add `@Test` annotations to all test methods** (preparation for future migration)
   - This allows tests to be discovered by both JUnit 3 (via reflection) and JUnit 5 (via annotation)
   - No functional change, just adds annotations

2. **Create detailed migration guide** (this document)
   - Document current architecture
   - Document migration options
   - Provide examples for future migration

3. **Add TODO comments** in code pointing to migration path
   - Mark `extends TestCase` with TODO
   - Mark `extends TestSetup` with TODO
   - Reference this document

4. **Create prototype** for one provider using `@TestFactory`
   - Prove the concept works
   - Document lessons learned
   - Don't migrate all providers yet

5. **Update progress documentation**
   - Mark Phase 3b as "Documented and Prepared"
   - Set realistic timeline for full migration (Phase 4)

## Estimated Effort for Full Migration

### Option A (Recommended): 2-3 person-days
- Add `@Test` annotations: 1 day
- Documentation: 1 day
- Prototype: 1 day

### Option B (Future Phase 4): 15-20 person-days
- Migrate 45 provider test cases: 10-15 days
- Testing and validation: 3-5 days
- Documentation: 2 days

### Option C (Not Recommended): 40-60 person-days
- Rewrite all test classes: 30-40 days
- Testing and validation: 5-10 days
- Bug fixes: 5-10 days

## Success Criteria for Phase 3b

- [x] Phase 3a complete (JUnit 5 lifecycle methods added)
- [ ] All test methods have `@Test` annotations
- [ ] Comprehensive migration documentation created
- [ ] Prototype `@TestFactory` implementation for one provider
- [ ] All 3122 tests still pass
- [ ] Zero regressions

## Next Steps After Phase 3b

**Phase 4: Gradual Provider Migration (Future)**
- Migrate one provider at a time to `@TestFactory`
- Validate each migration thoroughly
- Update documentation with lessons learned
- Timeline: 3-6 months

**Phase 5: Remove Vintage Engine (Future)**
- After all providers migrated
- Remove `junit-vintage-engine` dependency
- Remove `extends TestCase` from `AbstractProviderTestCase`
- Timeline: After Phase 4 complete

## Conclusion

Full migration of the test suite infrastructure from JUnit 3 to JUnit 5 is a major undertaking that requires careful planning and execution. The recommended approach for Phase 3b is to **document the migration path and prepare the codebase** rather than attempting a full migration now.

This allows us to:
1. Maintain stability (all 3122 tests continue to work)
2. Reduce risk (no breaking changes)
3. Enable future migration (clear path documented)
4. Deliver value incrementally (Phase 3a already delivered JUnit 5 lifecycle methods)

The full migration can be tackled in future phases when there's more time and resources available.


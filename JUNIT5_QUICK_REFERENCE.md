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
# JUnit 5 Migration - Quick Reference Guide

## Import Changes

### Annotations
```java
// JUnit 4
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Ignore;

// JUnit 5
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
```

### Assertions
```java
// JUnit 4
import static org.junit.Assert.*;

// JUnit 5
import static org.junit.jupiter.api.Assertions.*;
```

### Assumptions
```java
// JUnit 4
import static org.junit.Assume.*;

// JUnit 5
import static org.junit.jupiter.api.Assumptions.*;
```

## Annotation Changes

| JUnit 4 | JUnit 5 | Notes |
|---------|---------|-------|
| `@Test` | `@Test` | Different package |
| `@Before` | `@BeforeEach` | Renamed |
| `@After` | `@AfterEach` | Renamed |
| `@BeforeClass` | `@BeforeAll` | Renamed, must be static |
| `@AfterClass` | `@AfterAll` | Renamed, must be static |
| `@Ignore` | `@Disabled` | Renamed |
| `@Ignore("reason")` | `@Disabled("reason")` | Same functionality |
| `@Category(Fast.class)` | `@Tag("fast")` | Different approach |
| `@RunWith(Suite.class)` | `@Suite` | Simplified |
| `@RunWith(Parameterized.class)` | `@ParameterizedTest` | More powerful |

## Common Patterns

### Basic Test

**JUnit 4:**
```java
import org.junit.Test;
import static org.junit.Assert.*;

public class MyTest {
    @Test
    public void testSomething() {
        assertEquals(expected, actual);
    }
}
```

**JUnit 5:**
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MyTest {
    @Test
    public void testSomething() {
        assertEquals(expected, actual);
    }
}
```

### Lifecycle Methods

**JUnit 4:**
```java
@Before
public void setUp() {
    // runs before each test
}

@After
public void tearDown() {
    // runs after each test
}

@BeforeClass
public static void setUpClass() {
    // runs once before all tests
}

@AfterClass
public static void tearDownClass() {
    // runs once after all tests
}
```

**JUnit 5:**
```java
@BeforeEach
public void setUp() {
    // runs before each test
}

@AfterEach
public void tearDown() {
    // runs after each test
}

@BeforeAll
public static void setUpClass() {
    // runs once before all tests
}

@AfterAll
public static void tearDownClass() {
    // runs once after all tests
}
```

### Exception Testing

**JUnit 4:**
```java
@Test(expected = IllegalArgumentException.class)
public void testException() {
    throw new IllegalArgumentException("error");
}
```

**JUnit 5:**
```java
@Test
public void testException() {
    assertThrows(IllegalArgumentException.class, () -> {
        throw new IllegalArgumentException("error");
    });
}

// With message verification
@Test
public void testExceptionWithMessage() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        throw new IllegalArgumentException("error");
    });
    assertEquals("error", exception.getMessage());
}
```

### Timeout Testing

**JUnit 4:**
```java
@Test(timeout = 1000)
public void testTimeout() {
    // must complete within 1 second
}
```

**JUnit 5:**
```java
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;

@Test
@Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
public void testTimeout() {
    // must complete within 1 second
}

// Or using assertTimeout
@Test
public void testTimeout() {
    assertTimeout(Duration.ofSeconds(1), () -> {
        // code that should complete within 1 second
    });
}
```

### Ignoring Tests

**JUnit 4:**
```java
@Ignore
@Test
public void testIgnored() {
    // not executed
}

@Ignore("Not implemented yet")
@Test
public void testIgnoredWithReason() {
    // not executed
}
```

**JUnit 5:**
```java
@Disabled
@Test
public void testDisabled() {
    // not executed
}

@Disabled("Not implemented yet")
@Test
public void testDisabledWithReason() {
    // not executed
}
```

### Assumptions

**JUnit 4:**
```java
import static org.junit.Assume.*;

@Test
public void testOnlyOnLinux() {
    assumeTrue(System.getProperty("os.name").contains("Linux"));
    // test code
}
```

**JUnit 5:**
```java
import static org.junit.jupiter.api.Assumptions.*;

@Test
public void testOnlyOnLinux() {
    assumeTrue(System.getProperty("os.name").contains("Linux"));
    // test code
}

// With message
@Test
public void testOnlyOnLinux() {
    assumeTrue(System.getProperty("os.name").contains("Linux"),
               "Test only runs on Linux");
    // test code
}
```

## Assertion Changes

Most assertions have the same signature, just different package:

```java
// Both JUnit 4 and 5 (same signature)
assertEquals(expected, actual);
assertEquals(expected, actual, "message");
assertNotEquals(unexpected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);
assertSame(expected, actual);
assertNotSame(unexpected, actual);
assertArrayEquals(expectedArray, actualArray);
```

### New in JUnit 5

```java
// assertAll - group assertions
assertAll("person",
    () -> assertEquals("John", person.getFirstName()),
    () -> assertEquals("Doe", person.getLastName())
);

// assertThrows - exception testing
assertThrows(IllegalArgumentException.class, () -> {
    throw new IllegalArgumentException();
});

// assertTimeout - timeout testing
assertTimeout(Duration.ofSeconds(1), () -> {
    // code
});

// assertIterableEquals - compare iterables
assertIterableEquals(expectedList, actualList);
```

## Test Suites

**JUnit 4:**
```java
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    Test1.class,
    Test2.class
})
public class TestSuite {
}
```

**JUnit 5:**
```java
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SelectClasses;

@Suite
@SelectClasses({
    Test1.class,
    Test2.class
})
public class TestSuite {
}
```

## Parameterized Tests

**JUnit 4:**
```java
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ParameterizedTest {
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { 1, 2, 3 },
            { 2, 3, 5 }
        });
    }
    
    private int a, b, expected;
    
    public ParameterizedTest(int a, int b, int expected) {
        this.a = a;
        this.b = b;
        this.expected = expected;
    }
    
    @Test
    public void testAdd() {
        assertEquals(expected, a + b);
    }
}
```

**JUnit 5:**
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ParameterizedTest {
    
    @ParameterizedTest
    @CsvSource({
        "1, 2, 3",
        "2, 3, 5"
    })
    public void testAdd(int a, int b, int expected) {
        assertEquals(expected, a + b);
    }
}
```

## Custom Extensions

**JUnit 4 (Rules):**
```java
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public class MyTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void testUsingTempFolder() {
        File file = folder.newFile("test.txt");
        // test code
    }
}
```

**JUnit 5 (Extensions):**
```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

public class MyTest {
    
    @Test
    public void testUsingTempDir(@TempDir Path tempDir) {
        Path file = tempDir.resolve("test.txt");
        // test code
    }
}
```

## Nested Tests

**JUnit 5 only:**
```java
import org.junit.jupiter.api.Nested;

public class OuterTest {
    
    @Test
    public void outerTest() {
        // test code
    }
    
    @Nested
    class InnerTests {
        @Test
        public void innerTest() {
            // test code
        }
    }
}
```

## Display Names

**JUnit 5 only:**
```java
import org.junit.jupiter.api.DisplayName;

@DisplayName("A special test case")
public class MyTest {
    
    @Test
    @DisplayName("Custom test name with spaces and special characters!")
    public void testWithDisplayName() {
        // test code
    }
}
```

## Conditional Test Execution

**JUnit 5 only:**
```java
import org.junit.jupiter.api.condition.*;

@Test
@EnabledOnOs(OS.LINUX)
public void testOnLinux() {
    // only runs on Linux
}

@Test
@EnabledOnJre(JRE.JAVA_8)
public void testOnJava8() {
    // only runs on Java 8
}

@Test
@EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
public void testOn64Bit() {
    // only runs on 64-bit systems
}

@Test
@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
public void testOnCI() {
    // only runs when ENV=ci
}
```

## Migration Checklist

- [ ] Replace `import org.junit.Test` with `import org.junit.jupiter.api.Test`
- [ ] Replace `@Before` with `@BeforeEach`
- [ ] Replace `@After` with `@AfterEach`
- [ ] Replace `@BeforeClass` with `@BeforeAll`
- [ ] Replace `@AfterClass` with `@AfterAll`
- [ ] Replace `@Ignore` with `@Disabled`
- [ ] Replace `import static org.junit.Assert.*` with `import static org.junit.jupiter.api.Assertions.*`
- [ ] Replace `@Test(expected=...)` with `assertThrows(...)`
- [ ] Replace `@Test(timeout=...)` with `@Timeout(...)` or `assertTimeout(...)`
- [ ] Replace `@RunWith` with `@ExtendWith` or appropriate annotation
- [ ] Replace `@Rule` with `@ExtendWith` or built-in extensions
- [ ] Update test suites to use `@Suite`
- [ ] Update parameterized tests to use `@ParameterizedTest`

## Common Mistakes

### 1. Forgetting to make @BeforeAll/@AfterAll static

**Wrong:**
```java
@BeforeAll
public void setUp() { } // ERROR: must be static
```

**Correct:**
```java
@BeforeAll
public static void setUp() { }
```

### 2. Wrong assertion argument order

```java
// Correct (same in JUnit 4 and 5)
assertEquals(expected, actual);
assertEquals(expected, actual, "message");

// Wrong
assertEquals(actual, expected); // arguments swapped
```

### 3. Forgetting to update imports

```java
// Wrong - mixing JUnit 4 and 5
import org.junit.Test; // JUnit 4
import static org.junit.jupiter.api.Assertions.*; // JUnit 5

// Correct
import org.junit.jupiter.api.Test; // JUnit 5
import static org.junit.jupiter.api.Assertions.*; // JUnit 5
```

## Useful Commands

```bash
# Find JUnit 4 tests
grep -r "import org.junit.Test;" --include="*.java"

# Find JUnit 5 tests
grep -r "import org.junit.jupiter.api.Test;" --include="*.java"

# Find tests using expected attribute
grep -r "@Test(expected" --include="*.java"

# Find tests using timeout attribute
grep -r "@Test(timeout" --include="*.java"

# Run specific test
mvn test -Dtest=MyTest

# Run tests with verbose output
mvn test -X

# Run tests and generate report
mvn test surefire-report:report
```

## Resources

- **JUnit 5 User Guide:** https://junit.org/junit5/docs/current/user-guide/
- **JUnit 5 API Docs:** https://junit.org/junit5/docs/current/api/
- **Migration Guide:** https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4
- **JUnit 5 Samples:** https://github.com/junit-team/junit5-samples


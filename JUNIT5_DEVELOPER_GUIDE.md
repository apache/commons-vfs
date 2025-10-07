<!--
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

# JUnit 5 Developer Guide for Apache Commons VFS

This guide provides best practices and patterns for writing tests in Apache Commons VFS using JUnit 5.

## Table of Contents

1. [Basic Test Structure](#basic-test-structure)
2. [Assertions](#assertions)
3. [Lifecycle Methods](#lifecycle-methods)
4. [Provider Test Suites](#provider-test-suites)
5. [Modern JUnit 5 Features](#modern-junit-5-features)
6. [Test Organization](#test-organization)
7. [Running Tests](#running-tests)

## Basic Test Structure

### Simple Test Class

```java
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("My Feature Tests")
public class MyFeatureTest {

    @Test
    @DisplayName("Should do something correctly")
    public void testSomething() {
        // Arrange
        String input = "test";
        
        // Act
        String result = processInput(input);
        
        // Assert
        assertEquals("expected", result, "Processing should return expected value");
    }
}
```

## Assertions

### Parameter Order

**IMPORTANT**: JUnit 5 has a different parameter order than JUnit 3/4!

```java
// ✅ CORRECT (JUnit 5)
assertEquals(expected, actual, "message");
assertSame(expected, actual, "message");
assertTrue(condition, "message");

// ❌ WRONG (JUnit 3/4 style - DO NOT USE)
assertEquals("message", expected, actual);  // WRONG!
assertSame(actual, "message", expected);    // WRONG!
```

### Common Assertions

```java
// Equality
assertEquals(expected, actual);
assertEquals(expected, actual, "Custom message");

// Identity
assertSame(expectedObject, actualObject);
assertNotSame(object1, object2);

// Null checks
assertNull(object);
assertNotNull(object);

// Boolean
assertTrue(condition);
assertFalse(condition);

// Exceptions
assertThrows(FileSystemException.class, () -> {
    // Code that should throw exception
});

// Timeouts
assertTimeout(Duration.ofSeconds(5), () -> {
    // Code that should complete within 5 seconds
});

// Lazy messages (only evaluated on failure)
assertEquals(expected, actual, () -> "Expensive message: " + computeDetails());
```

## Lifecycle Methods

### Method-level Lifecycle

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

public class MyTest {

    private FileObject testFile;

    @BeforeEach
    public void setUp() throws Exception {
        // Runs before each test method
        testFile = createTestFile();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Runs after each test method
        if (testFile != null) {
            testFile.delete();
        }
    }
}
```

### Class-level Lifecycle

```java
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MyTest {

    private static FileSystemManager manager;

    @BeforeAll
    public static void setUpClass() throws Exception {
        // Runs once before all tests
        manager = VFS.getManager();
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        // Runs once after all tests
        if (manager != null) {
            manager.close();
        }
    }
}
```

## Provider Test Suites

### Creating a Provider Test

Provider tests use a special infrastructure for testing file system providers:

```java
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

@DisplayName("My Provider Tests")
@Tag("provider")
@Tag("myfs")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MyProviderTest extends ProviderTestSuiteJunit5 {

    public MyProviderTest() throws Exception {
        super(new MyProviderTestConfig(), "", false);
    }

    private static class MyProviderTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            // Return the base folder for tests
            return manager.resolveFile("myfs://test-folder");
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            // Register your provider
            manager.addProvider("myfs", new MyFileProvider());
        }
    }
}
```

## Modern JUnit 5 Features

### @ParameterizedTest

Use parameterized tests to reduce code duplication:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedTest(name = "FileType.{0} should find {1} items")
@CsvSource({
    "FILE, 5",
    "FOLDER, 8",
    "FILE_OR_FOLDER, 0"
})
public void testFileTypeSelector(FileType fileType, int expectedCount) {
    FileSelector selector = new FileTypeSelector(fileType);
    FileObject[] results = baseFolder.findFiles(selector);
    assertEquals(expectedCount, results.length);
}

@ParameterizedTest
@ValueSource(strings = {"test1.txt", "test2.txt", "test3.txt"})
public void testFileExists(String fileName) {
    FileObject file = baseFolder.resolveFile(fileName);
    assertTrue(file.exists());
}
```

### @Nested

Organize related tests into nested classes:

```java
import org.junit.jupiter.api.Nested;

@DisplayName("File Operations Tests")
public class FileOperationsTest {

    @Nested
    @DisplayName("Read operations")
    class ReadOperations {
        
        @Test
        @DisplayName("Should read file content")
        public void testReadContent() {
            // Test reading
        }
        
        @Test
        @DisplayName("Should handle missing files")
        public void testReadMissingFile() {
            // Test error handling
        }
    }

    @Nested
    @DisplayName("Write operations")
    class WriteOperations {
        
        @Test
        @DisplayName("Should write file content")
        public void testWriteContent() {
            // Test writing
        }
        
        @Test
        @DisplayName("Should create parent directories")
        public void testCreateParents() {
            // Test directory creation
        }
    }
}
```

### @Tag

Tag tests for selective execution:

```java
import org.junit.jupiter.api.Tag;

@Tag("provider")
@Tag("network")
@Tag("slow")
public class NetworkProviderTest {
    // Tests that require network access
}

@Tag("unit")
@Tag("fast")
public class UtilityTest {
    // Fast unit tests
}
```

Run specific tags:
```bash
# Run only provider tests
mvn test -Dgroups=provider

# Run only fast tests
mvn test -Dgroups=fast

# Exclude slow tests
mvn test -DexcludedGroups=slow
```

### @DisplayName

Add human-readable test names:

```java
@DisplayName("File System Manager Tests")
public class FileSystemManagerTest {

    @Test
    @DisplayName("Should resolve absolute file paths correctly")
    public void testResolveAbsolutePath() {
        // Test implementation
    }

    @Test
    @DisplayName("Should throw exception for invalid URIs")
    public void testInvalidUri() {
        // Test implementation
    }
}
```

## Test Organization

### File Naming Conventions

- **`*Test.java`**: Test suite files (run by Maven Surefire)
- **`*Tests.java`**: Test implementation files (run via suite infrastructure)
- **`*TestCase.java`**: Old JUnit 3 pattern (deprecated, do not use)

### Package Structure

```
src/test/java/
└── org/apache/commons/vfs2/
    ├── provider/
    │   ├── http/
    │   │   ├── HttpProviderTest.java        (Test suite)
    │   │   └── HttpProviderTestConfig.java  (Configuration)
    │   └── local/
    │       ├── LocalProviderTest.java
    │       └── FileNameTests.java           (Test implementation)
    └── filter/
        ├── NameFileFilterTest.java
        └── PrefixFileFilterTest.java
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=HttpProviderTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=HttpProviderTest#testReadFile
```

### Run Tests by Tag

```bash
# Run provider tests only
mvn test -Dgroups=provider

# Run multiple tags
mvn test -Dgroups="provider,network"

# Exclude tags
mvn test -DexcludedGroups=slow
```

### Skip Tests

```bash
mvn install -DskipTests
```

## Best Practices

1. **Use descriptive test names**: Use `@DisplayName` for human-readable names
2. **Organize with @Nested**: Group related tests together
3. **Reduce duplication**: Use `@ParameterizedTest` for similar tests
4. **Tag appropriately**: Use `@Tag` for test categorization
5. **Follow AAA pattern**: Arrange, Act, Assert
6. **Use lazy messages**: Use lambda expressions for expensive assertion messages
7. **Clean up resources**: Always clean up in `@AfterEach` or `@AfterAll`
8. **Test one thing**: Each test should verify one specific behavior
9. **Make tests independent**: Tests should not depend on execution order
10. **Use meaningful assertions**: Include descriptive failure messages

## Migration from JUnit 4

If you're migrating old tests:

1. Replace `@org.junit.Test` with `@org.junit.jupiter.api.Test`
2. Replace `@Before` with `@BeforeEach`
3. Replace `@After` with `@AfterEach`
4. Replace `@BeforeClass` with `@BeforeAll` (and make method static or use `@TestInstance`)
5. Replace `@AfterClass` with `@AfterAll`
6. Fix assertion parameter order (expected comes first in JUnit 5)
7. Replace `@Ignore` with `@Disabled`
8. Replace `@Category` with `@Tag`

## Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [JUnit 5 API Documentation](https://junit.org/junit5/docs/current/api/)
- [Apache Commons VFS Documentation](https://commons.apache.org/proper/commons-vfs/)


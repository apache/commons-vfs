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

# Apache Commons VFS Test Suite

This directory contains the test suite for Apache Commons VFS, fully migrated to JUnit 5.

## Test Structure

### File Naming Conventions

- **`*Test.java`**: Test suite files that are directly executed by Maven Surefire
- **`*Tests.java`**: Test implementation classes that are executed via the provider test infrastructure
- **`*TestConfig.java`**: Configuration classes for provider tests

### Directory Organization

```
src/test/java/org/apache/commons/vfs2/
├── provider/                    # Provider-specific tests
│   ├── http/                   # HTTP provider tests
│   ├── ftp/                    # FTP provider tests
│   ├── local/                  # Local file system tests
│   ├── ram/                    # RAM file system tests
│   ├── zip/                    # ZIP file system tests
│   ├── tar/                    # TAR file system tests
│   └── ...                     # Other providers
├── filter/                      # File filter tests
├── cache/                       # File cache tests
├── util/                        # Utility tests
└── *.java                       # Core VFS tests
```

## Test Infrastructure

### Provider Test Suites

Provider tests use a special infrastructure that allows the same test suite to be run against different file system providers:

1. **`AbstractProviderTestSuite`**: Base class for all provider test suites
2. **`ProviderTestSuiteJunit5`**: Standard test suite with common tests
3. **`AbstractProviderTestConfig`**: Configuration interface for provider-specific setup

Example provider test structure:

```
HttpProviderTest.java           # Test suite (extends ProviderTestSuiteJunit5)
└── HttpProviderTestConfig      # Configuration (implements AbstractProviderTestConfig)
    ├── getBaseTestFolder()     # Returns the base folder for tests
    └── prepare()               # Registers the provider
```

### Test Execution Flow

1. Maven Surefire discovers `*Test.java` files
2. Test suite extends `ProviderTestSuiteJunit5`
3. Suite uses `@TestFactory` to dynamically generate tests
4. Each test class (e.g., `ProviderReadTests.java`) is instantiated
5. Tests are executed using the provider-specific configuration

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Provider Tests

```bash
# HTTP provider
mvn test -Dtest=HttpProviderTest

# Local file system
mvn test -Dtest=LocalProviderTest

# ZIP provider
mvn test -Dtest=ZipProviderTest
```

### Run Tests by Category

Tests are tagged for selective execution:

```bash
# Run all provider tests
mvn test -Dgroups=provider

# Run network-dependent tests
mvn test -Dgroups=network

# Run only fast unit tests
mvn test -Dgroups=unit

# Exclude slow tests
mvn test -DexcludedGroups=slow
```

### Run Specific Test Method

```bash
mvn test -Dtest=FileTypeSelectorTest#testFileTypeSelector
```

## Test Categories (Tags)

Tests are organized using JUnit 5 tags:

- **`@Tag("provider")`**: File system provider tests
- **`@Tag("network")`**: Tests requiring network access
- **`@Tag("unit")`**: Fast unit tests
- **`@Tag("integration")`**: Integration tests
- **`@Tag("slow")`**: Slow-running tests
- **`@Tag("local")`**: Local file system tests
- **`@Tag("archive")`**: Archive file system tests (ZIP, TAR, JAR)

## Test Data

Test data files are located in:

```
src/test/resources/test-data/
├── read-tests/          # Read-only test files
├── write-tests/         # Files for write tests
├── nested.zip           # Nested archive test files
├── nested.tar
├── nested.jar
└── ...
```

## Common Test Classes

### Core Tests (Applied to All Providers)

- **`ProviderReadTests`**: Tests for reading files
- **`ProviderWriteTests`**: Tests for writing files
- **`ProviderDeleteTests`**: Tests for deleting files
- **`ProviderRenameTests`**: Tests for renaming files
- **`NamingTests`**: Tests for file naming and path resolution
- **`ContentTests`**: Tests for file content operations
- **`UriTests`**: Tests for URI handling

### Provider-Specific Tests

Each provider may have additional tests specific to its functionality:

- **HTTP**: Connection pooling, authentication
- **FTP**: Active/passive mode, SSL/TLS
- **Local**: Permissions, symbolic links
- **ZIP/TAR**: Nested archives, compression

## Writing New Tests

### Simple Unit Test

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("My Feature Tests")
public class MyFeatureTest {

    @Test
    @DisplayName("Should do something correctly")
    public void testSomething() {
        assertEquals(expected, actual, "Descriptive message");
    }
}
```

### Provider Test

```java
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
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
        public FileObject getBaseTestFolder(FileSystemManager manager) throws Exception {
            return manager.resolveFile("myfs://test-folder");
        }

        @Override
        public void prepare(DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("myfs", new MyFileProvider());
        }
    }
}
```

## Test Requirements

### Environment-Specific Tests

Some tests require specific infrastructure:

- **FTP/FTPS tests**: Require FTP server running on localhost
- **HDFS tests**: Require Hadoop cluster
- **WebDAV tests**: Require WebDAV server
- **SFTP tests**: Require SSH server

These tests are typically skipped if the required infrastructure is not available.

### Conditional Test Execution

Tests can be conditionally executed based on environment:

```java
import org.junit.jupiter.api.condition.*;

@EnabledOnOs(OS.WINDOWS)
@Test
public void testWindowsSpecific() {
    // Windows-only test
}

@DisabledOnOs(OS.WINDOWS)
@Test
public void testUnixSpecific() {
    // Unix-only test
}

@EnabledIfSystemProperty(named = "test.ftp.uri", matches = ".*")
@Test
public void testFtp() {
    // Only runs if system property is set
}
```

## Debugging Tests

### Enable Debug Logging

Add to `src/test/resources/log4j2-test.xml`:

```xml
<Logger name="org.apache.commons.vfs2" level="DEBUG"/>
```

### Run Single Test with Debug

```bash
mvn test -Dtest=MyTest -X
```

### IDE Integration

All tests can be run directly from your IDE (IntelliJ IDEA, Eclipse, VS Code) using the built-in JUnit 5 test runner.

## Contributing

When contributing new tests:

1. Follow the existing naming conventions
2. Use `@DisplayName` for readable test names
3. Add appropriate `@Tag` annotations
4. Include descriptive assertion messages
5. Clean up resources in `@AfterEach` or `@AfterAll`
6. Ensure tests are independent and can run in any order
7. See [JUNIT5_DEVELOPER_GUIDE.md](../../../../../../../JUNIT5_DEVELOPER_GUIDE.md) for detailed guidelines

## Additional Documentation

- [JUnit 5 Developer Guide](../../../../../../../JUNIT5_DEVELOPER_GUIDE.md)
- [JUnit 5 Migration Report](../../../../../../../JUNIT5_MIGRATION_FINAL_REPORT.md)
- [Apache Commons VFS Documentation](https://commons.apache.org/proper/commons-vfs/)


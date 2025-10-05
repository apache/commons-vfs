#!/usr/bin/env python3
"""
Script to migrate provider test cases from JUnit 3 to JUnit 5.
Converts *ProviderTestCase.java to *ProviderTest.java using the new JUnit 5 infrastructure.
"""

import os
import re
import sys

def migrate_provider_test(input_file):
    """Migrate a single provider test case file."""
    
    # Read the file
    with open(input_file, 'r') as f:
        content = f.read()
    
    # Extract package name
    package_match = re.search(r'package\s+([\w.]+);', content)
    if not package_match:
        print(f"ERROR: Could not find package in {input_file}")
        return None
    package = package_match.group(1)
    
    # Extract class name
    class_match = re.search(r'public\s+class\s+(\w+)\s+extends\s+AbstractProviderTestConfig', content)
    if not class_match:
        print(f"SKIP: {input_file} does not extend AbstractProviderTestConfig")
        return None
    old_class_name = class_match.group(1)
    
    # New class name: replace TestCase with Test
    new_class_name = old_class_name.replace('TestCase', 'Test')
    
    # Extract the suite() method content
    suite_match = re.search(
        r'public\s+static\s+(?:junit\.framework\.)?Test\s+suite\(\)\s+throws\s+Exception\s*\{(.*?)\n\s*return\s+testSuite;\s*\}',
        content,
        re.DOTALL
    )
    
    if not suite_match:
        print(f"ERROR: Could not find suite() method in {input_file}")
        return None
    
    suite_content = suite_match.group(1)
    
    # Extract test additions from suite content
    test_additions = re.findall(r'testSuite\.addTests\((\w+)\.class\);', suite_content)
    
    # Extract getBaseTestFolder method
    base_folder_match = re.search(
        r'(@Override\s+)?public\s+FileObject\s+getBaseTestFolder\([^)]*\)\s+throws\s+Exception\s*\{(.*?)\}',
        content,
        re.DOTALL
    )
    
    if not base_folder_match:
        print(f"ERROR: Could not find getBaseTestFolder() in {input_file}")
        return None
    
    base_folder_body = base_folder_match.group(2).strip()
    
    # Build the new file content
    new_content = f'''/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package {package};

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;

/**
 * JUnit 5 tests for the provider.
 * <p>
 * This class replaces {{@link {old_class_name}}} with a pure JUnit 5 implementation.
 * </p>
 */
public class {new_class_name} extends ProviderTestSuiteJunit5 {{

    public {new_class_name}() throws Exception {{
        super(new {new_class_name}Config(), "", false);
    }}

    @Override
    protected void addBaseTests() throws Exception {{
        // Add standard provider tests
        super.addBaseTests();
        
        // Add provider-specific tests
'''
    
    # Add test additions
    for test_class in test_additions:
        new_content += f'        addTests({test_class}.class);\n'
    
    new_content += f'''    }}

    /**
     * Configuration for provider tests.
     */
    private static class {new_class_name}Config extends AbstractProviderTestConfig {{
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {{
            {base_folder_body}
        }}
    }}
}}
'''
    
    # Determine output file path
    output_file = input_file.replace('TestCase.java', 'Test.java')
    
    return {
        'input_file': input_file,
        'output_file': output_file,
        'content': new_content,
        'old_class_name': old_class_name,
        'new_class_name': new_class_name
    }

def main():
    # Find all provider test case files
    test_cases = []
    for root, dirs, files in os.walk('commons-vfs2/src/test/java'):
        for file in files:
            if file.endswith('ProviderTestCase.java') and file != 'AbstractProviderTestCase.java':
                test_cases.append(os.path.join(root, file))
    
    print(f"Found {len(test_cases)} provider test cases to migrate")
    
    migrated = []
    for test_case in sorted(test_cases):
        print(f"\nMigrating: {test_case}")
        result = migrate_provider_test(test_case)
        if result:
            migrated.append(result)
            print(f"  -> {result['output_file']}")
            print(f"  -> {result['old_class_name']} -> {result['new_class_name']}")
    
    # Write the migrated files
    if migrated:
        print(f"\n\nWriting {len(migrated)} migrated files...")
        for result in migrated:
            with open(result['output_file'], 'w') as f:
                f.write(result['content'])
            print(f"  Written: {result['output_file']}")
    
    print(f"\n\nMigration complete! Migrated {len(migrated)} files.")
    print("\nNext steps:")
    print("1. Review the generated files")
    print("2. Run: mvn test-compile -pl commons-vfs2 -am")
    print("3. Run: mvn test -pl commons-vfs2 -am")
    print("4. If tests pass, remove the old *ProviderTestCase.java files")

if __name__ == '__main__':
    main()


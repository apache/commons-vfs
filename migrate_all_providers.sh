#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Script to test all migrated JUnit 5 provider tests

echo "Testing all migrated JUnit 5 provider tests..."
echo "=============================================="

# List of migrated providers
PROVIDERS=(
    "HttpProviderTest"
    "RamProviderTest"
    "JarProviderTest"
    "ZipProviderTest"
    "TarProviderTest"
)

TOTAL=0
PASSED=0
FAILED=0

for provider in "${PROVIDERS[@]}"; do
    echo ""
    echo "Testing $provider..."
    result=$(mvn test -pl commons-vfs2 -am -Dtest=$provider 2>&1 | grep "Tests run:" | tail -1)
    echo "  $result"
    
    if echo "$result" | grep -q "Failures: 0, Errors: 0"; then
        ((PASSED++))
    else
        ((FAILED++))
    fi
    ((TOTAL++))
done

echo ""
echo "=============================================="
echo "Summary: $PASSED/$TOTAL providers passed"
if [ $FAILED -gt 0 ]; then
    echo "WARNING: $FAILED providers failed"
    exit 1
fi
echo "All providers passed!"


/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.jar.test;

import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests JAR attributes.
 *
 * @version $Id$
 */
public class JarAttributesTestCase {

    private void printAttributes(final Map<String, Object> attributes) {
        for (final Map.Entry<String, Object> e : attributes.entrySet()) {
            System.out.println("Key: " + e.getKey() + ", Value: " + e.getValue());
        }
    }

    @Test
    public void testAttributes() throws Exception {
        final FileObject file = JarProviderTestCase.getTestJar(VFS.getManager(), "test.jar");

        final Map<String, Object> attributes = file.getContent().getAttributes();
        Assert.assertEquals("1.0", attributes.get("Manifest-Version"));
        // Debugging:
        // this.printAttributes(attributes);
    }

    @Test
    public void testNestedAttributes() throws Exception {
        final FileObject nested = JarProviderTestCase.getTestJar(VFS.getManager(), "nested.jar");
        final FileObject file = nested.resolveFile("test.jar");

        final Map<String, Object> attributes = file.getContent().getAttributes();
        Assert.assertEquals("1.0", attributes.get("Manifest-Version"));
        // Debugging:
        // this.printAttributes(attributes);
    }
}

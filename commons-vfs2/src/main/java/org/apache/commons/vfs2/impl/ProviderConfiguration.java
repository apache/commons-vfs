/*
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
package org.apache.commons.vfs2.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes the configuration for a provider.
 * <p>
 * Used by digester in StandardFileSystemManager
 * </p>
 */
public class ProviderConfiguration {

    private String className;
    private final List<String> schemes = new ArrayList<>(10);
    private final List<String> dependencies = new ArrayList<>(10);

    /**
     * Constructs a new instance.
     */
    public ProviderConfiguration() {
    }

    /**
     * Gets the class name.
     *
     * @return the class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the dependency.
     *
     * @return the dependency.
     */
    public List<String> getDependencies() {
        return dependencies;
    }

    /**
     * Gets the schema.
     *
     * @return the scheme.
     */
    public List<String> getSchemes() {
        return schemes;
    }

    /**
     * Always returns false.
     *
     * @return false.
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * sets the class name.
     *
     * @param className the class name.
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /**
     * Sets the dependency.
     *
     * @param dependency the dependency.
     */
    public void setDependency(final String dependency) {
        dependencies.add(dependency);
    }

    /**
     * Sets the scheme.
     *
     * @param scheme the scheme.
     */
    public void setScheme(final String scheme) {
        schemes.add(scheme);
    }
}

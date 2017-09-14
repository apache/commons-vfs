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
package org.apache.commons.vfs2;

/**
 * An enumerated type to deal with the various cache strategies.
 */
public enum CacheStrategy {
    /**
     * Deal with cached data manually. Call {@link FileObject#refresh()} to refresh the object data.
     */
    MANUAL("manual"),

    /**
     * Refresh the data every time you request a file from {@link FileSystemManager#resolveFile}.
     */
    ON_RESOLVE("onresolve"),

    /**
     * Refresh the data every time you call a method on the fileObject. You'll use this only if you really need the
     * latest info as this setting is a major performance loss.
     */
    ON_CALL("oncall");

    /**
     * Cache strategy name
     */
    private final String realName;

    private CacheStrategy(final String name) {
        this.realName = name;
    }

    /**
     * Returns the name of the scope.
     *
     * @return the name of the scope.
     */
    @Override
    public String toString() {
        return realName;
    }

    /**
     * Returns the name of the scope.
     *
     * @return the name of the scope.
     */
    public String getName() {
        return realName;
    }
}

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
package org.apache.commons.vfs2.util;

/**
 * Factory to create an instance of a Cryptor.
 *
 * @since 2.0
 */
public final class CryptorFactory {
    /**
     * The System property name to identify the Cryptor class to be used.
     */
    public static final String CRYPTOR_CLASS = "org.apache.commons.vfs2.cryptor";

    private static Cryptor instance;

    /**
     * Prevent instantiation of the class.
     */
    private CryptorFactory() {

    }

    /**
     * Allows the Cryptor class to be set programmatically.
     *
     * @param cryptor The Cryptor.
     */
    public static synchronized void setCryptor(final Cryptor cryptor) {
        instance = cryptor;
    }

    /**
     * Return the Cryptor. If one has not been previously set, create it. The Cryptor class can be set by setting the
     * "org.apache.commons.vfs2.cryptor" System property to the name of the Cryptor class.
     *
     * @return The Cryptor.
     */
    public static synchronized Cryptor getCryptor() {
        if (instance != null) {
            return instance;
        }

        final String cryptorClass = System.getProperty(CRYPTOR_CLASS);
        if (cryptorClass != null) {
            try {
                final Class<?> clazz = Class.forName(cryptorClass);
                instance = (Cryptor) clazz.newInstance();
                return instance;
            } catch (final Exception ex) {
                throw new RuntimeException("Unable to create Cryptor " + cryptorClass, ex);
            }
        }
        instance = new DefaultCryptor();
        return instance;
    }
}

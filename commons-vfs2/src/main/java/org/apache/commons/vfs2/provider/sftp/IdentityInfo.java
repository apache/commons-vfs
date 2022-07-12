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
package org.apache.commons.vfs2.provider.sftp;

import java.io.File;
import java.util.Arrays;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

/**
 * Structure for an identity based on Files.
 *
 * @since 2.1
 */
public class IdentityInfo implements IdentityProvider {

    private final byte[] passPhrase;
    private final File privateKey;
    private final File publicKey;

    /**
     * Constructs an identity info with private key.
     * <p>
     * The key is not passphrase protected.
     * </p>
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param privateKey The file with the private key
     * @since 2.1
     */
    public IdentityInfo(final File privateKey) {
        this(privateKey, null, null);
    }

    /**
     * Constructs an identity info with private key and its passphrase.
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param privateKey The file with the private key
     * @param passPhrase The passphrase to decrypt the private key (can be {@code null} if no passphrase is used)
     * @since 2.1
     */
    public IdentityInfo(final File privateKey, final byte[] passPhrase) {
        this(privateKey, null, passPhrase);
    }

    /**
     * Constructs an identity info with private and public key and passphrase for the private key.
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param privateKey The file with the private key
     * @param publicKey  The public key part used for connections with exchange of certificates (can be {@code null})
     * @param passPhrase The passphrase to decrypt the private key (can be {@code null} if no passphrase is used)
     * @since 2.1
     */
    public IdentityInfo(final File privateKey, final File publicKey, final byte[] passPhrase) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passPhrase = passPhrase;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IdentityInfo) {
            final IdentityInfo other = (IdentityInfo) obj;
            if (!Arrays.equals(passPhrase, other.passPhrase)) {
                return false;
            }
            if (!isSameFile(privateKey, other.privateKey)) {
                return false;
            }
            return isSameFile(publicKey, other.publicKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + (passPhrase == null ? 0 : Arrays.hashCode(passPhrase));
        result = prime * result + (privateKey == null ? 0 : privateKey.getAbsolutePath().hashCode());
        return prime * result + (publicKey == null ? 0 : publicKey.getAbsolutePath().hashCode());
    }

    /**
     * @since 2.4
     */
    @Override
    public void addIdentity(final JSch jsch) throws JSchException {
        jsch.addIdentity(getAbsolutePath(privateKey), getAbsolutePath(publicKey), passPhrase);
    }

    private String getAbsolutePath(final File file) {
        return file != null ? file.getAbsolutePath() : null;
    }

    /**
     * Get the passphrase of the private key.
     *
     * @return the passphrase
     * @since 2.1
     */
    public byte[] getPassPhrase() {
        return passPhrase;
    }

    /**
     * Get the file with the private key.
     *
     * @return the file
     * @since 2.1
     */
    public File getPrivateKey() {
        return privateKey;
    }

    /**
     * Get the file with the public key.
     *
     * @return the file
     * @since 2.1
     */
    public File getPublicKey() {
        return publicKey;
    }

    private static boolean isSameFile(File a, File b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.getAbsolutePath().equals(b.getAbsolutePath());
    }
}

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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

/**
 * Structure for an identity based on byte arrays.
 *
 * @since 2.4
 */
public class BytesIdentityInfo implements IdentityProvider {

    private final byte[] passPhrase;

    private final byte[] privateKey;

    private final byte[] publicKey;

    /**
     * Constructs an identity info with private and passphrase for the private key.
     *
     * @param privateKey Private key bytes
     * @param passPhrase The passphrase to decrypt the private key (can be {@code null} if no passphrase is used)
     */
    public BytesIdentityInfo(final byte[] privateKey, final byte[] passPhrase) {
        super();
        this.privateKey = privateKey;
        this.publicKey = null;
        this.passPhrase = passPhrase;
    }

    /**
     * Constructs an identity info with private and public key and passphrase for the private key.
     *
     * @param privateKey Private key bytes
     * @param publicKey  The public key part used for connections with exchange of certificates (can be {@code null})
     * @param passPhrase The passphrase to decrypt the private key (can be {@code null} if no passphrase is used)
     */
    public BytesIdentityInfo(final byte[] privateKey, final byte[] publicKey, final byte[] passPhrase) {
        super();
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.passPhrase = passPhrase;
    }

    @Override
    public void addIdentity(final JSch jsch) throws JSchException {
        jsch.addIdentity("PrivateKey", privateKey, publicKey, passPhrase);
    }

    public byte[] getPassPhrase() {
        return passPhrase;
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey;
    }

    public byte[] getPublicKeyBytes() {
        return publicKey;
    }
}

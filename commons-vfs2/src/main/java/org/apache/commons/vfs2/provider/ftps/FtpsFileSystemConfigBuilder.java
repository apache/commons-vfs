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
package org.apache.commons.vfs2.provider.ftps;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

/**
 * The configuration builder for various FTPS configuration options.
 *
 * @since 2.0
 */
public final class FtpsFileSystemConfigBuilder extends FtpFileSystemConfigBuilder {
    private static final String _PREFIX = FtpsFileSystemConfigBuilder.class.getName();

    private static final FtpsFileSystemConfigBuilder BUILDER = new FtpsFileSystemConfigBuilder();

    private static final String FTPS_MODE = _PREFIX + ".FTPS_MODE";
    private static final String PROT = _PREFIX + ".PROT";
    private static final String KEY_MANAGER = _PREFIX + ".KEY_MANAGER";
    private static final String TRUST_MANAGER = _PREFIX + ".TRUST_MANAGER";

    private FtpsFileSystemConfigBuilder() {
        super("ftps.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static FtpsFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * Set FTPS mode, either "implicit" or "explicit".
     *
     * <p>
     * Note, that implicit mode is not standardized and considered as deprecated. Some unit tests for VFS fail with
     * implicit mode and it is not yet clear if its a problem with Commons VFS/Commons Net or our test server Apache
     * FTP/SSHD.
     * </p>
     *
     * @param opts The FileSystemOptions.
     * @param ftpsMode The mode to establish a FTPS connection.
     * @see <a href="http://en.wikipedia.org/wiki/FTPS#Implicit">Wikipedia: FTPS/Implicit</a>
     * @since 2.1
     */
    public void setFtpsMode(final FileSystemOptions opts, final FtpsMode ftpsMode) {
        setParam(opts, FTPS_MODE, ftpsMode);
    }

    /**
     * Return the FTPS mode. Defaults to "explicit" if not defined.
     *
     * @param opts The FileSystemOptions.
     * @return The file type.
     * @see #setFtpsType
     */
    public FtpsMode getFtpsMode(final FileSystemOptions opts) {
        return getEnum(FtpsMode.class, opts, FTPS_MODE, FtpsMode.EXPLICIT);
    }

    /**
     * Set FTPS type, either "implicit" or "explicit".
     * <p>
     * Note, that implicit mode is not standardized and considered as deprecated. Some unit tests for VFS fail with
     * implicit mode and it is not yet clear if its a problem with Commons VFS/Commons Net or our test server Apache
     * FTP/SSHD.
     * </p>
     *
     * @param opts The FileSystemOptions.
     * @param ftpsType The file type.
     * @see <a href="http://en.wikipedia.org/wiki/FTPS#Implicit">Wikipedia: FTPS/Implicit</a>
     * @deprecated As of 2.1, use {@link #setFtpsMode(FileSystemOptions, FtpsMode)}
     */
    @Deprecated
    public void setFtpsType(final FileSystemOptions opts, final String ftpsType) {
        final FtpsMode mode;
        if (ftpsType != null) {
            mode = FtpsMode.valueOf(ftpsType.toUpperCase());
            if (mode == null) {
                throw new IllegalArgumentException("Not a proper FTPS mode: " + ftpsType);
            }
        } else {
            mode = null;
        }
        setFtpsMode(opts, mode);
    }

    /**
     * Return the FTPS type. Defaults to "explicit" if not defined.
     *
     * @param opts The FileSystemOptions.
     * @return The file type.
     * @see #setFtpsType
     * @deprecated As of 2.1, use {@link #getFtpsMode(FileSystemOptions)}
     */
    @Deprecated
    public String getFtpsType(final FileSystemOptions opts) {
        return getFtpsMode(opts).name().toLowerCase();
    }

    /**
     * Gets the data channel protection level (PROT).
     *
     * @param opts The FileSystemOptions.
     * @return The PROT value.
     * @see org.apache.commons.net.ftp.FTPSClient#execPROT(String)
     * @since 2.1
     */
    public FtpsDataChannelProtectionLevel getDataChannelProtectionLevel(final FileSystemOptions opts) {
        return getEnum(FtpsDataChannelProtectionLevel.class, opts, PROT);
    }

    /**
     * Sets the data channel protection level (PROT).
     *
     * @param opts The FileSystemOptions.
     * @param prot The PROT value, {@code null} has no effect.
     * @see org.apache.commons.net.ftp.FTPSClient#execPROT(String)
     * @since 2.1
     */
    public void setDataChannelProtectionLevel(final FileSystemOptions opts, final FtpsDataChannelProtectionLevel prot) {
        setParam(opts, PROT, prot);
    }

    /**
     * Gets the KeyManager used to provide a client-side certificate if the FTPS server requests it.
     *
     * @param opts The FileSystemOptions.
     * @return the key manager instance or {@code null}
     * @see org.apache.commons.net.ftp.FTPSClient#setKeyManager(KeyManager)
     * @since 2.1
     */
    public KeyManager getKeyManager(final FileSystemOptions opts) {
        return (KeyManager) getParam(opts, KEY_MANAGER);
    }

    /**
     * Sets the KeyManager used to provide a client-side certificate if the FTPS server requests it.
     *
     * @param opts The FileSystemOptions.
     * @param keyManager The key manager instance.
     * @see org.apache.commons.net.ftp.FTPSClient#setKeyManager(KeyManager)
     * @since 2.1
     */
    public void setKeyManager(final FileSystemOptions opts, final KeyManager keyManager) {
        setParam(opts, KEY_MANAGER, keyManager);
    }

    /**
     * Gets the TrustManager that validates the FTPS server's certificate.
     * <p>
     * If the params do not contain the key for the trust manager, it will return a trust manger that simply checks this
     * certificate for validity.
     * </p>
     *
     * @param opts The FileSystemOptions.
     * @return the trust manager instance or {@code null}
     * @see org.apache.commons.net.ftp.FTPSClient#setTrustManager(TrustManager)
     * @since 2.1
     */
    public TrustManager getTrustManager(final FileSystemOptions opts) {
        final TrustManager trustManager;
        if (hasParam(opts, TRUST_MANAGER)) {
            trustManager = (TrustManager) getParam(opts, TRUST_MANAGER);
        } else {
            trustManager = TrustManagerUtils.getValidateServerCertificateTrustManager();
        }
        return trustManager;
    }

    /**
     * Sets the TrustManager that validates the FTPS server's certificate.
     *
     * @param opts The FileSystemOptions.
     * @param trustManager The trust manager instance.
     * @see org.apache.commons.net.ftp.FTPSClient#setTrustManager(TrustManager)
     * @since 2.1
     */
    public void setTrustManager(final FileSystemOptions opts, final TrustManager trustManager) {
        setParam(opts, TRUST_MANAGER, trustManager);
    }
}

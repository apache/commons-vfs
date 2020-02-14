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
package org.apache.commons.vfs2.provider.ftp.test;

import java.io.IOException;

import org.apache.commons.vfs2.test.LastModifiedTests;
import org.apache.ftpserver.command.Command;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.impl.FtpIoSession;
import org.apache.ftpserver.impl.FtpReplyTranslator;
import org.apache.ftpserver.impl.FtpServerContext;
import org.apache.ftpserver.impl.LocalizedFtpReply;

import junit.framework.Test;

public class FtpProviderNoMdtmTestCase extends FtpProviderTestCase {

    public static Test suite() throws Exception {
        return suite(new FtpProviderNoMdtmTestCase(), LastModifiedTests.class);
    }
    
    /**
     * Explicitly remove MDTM feature from underlying apache MINA ftp server so
     * we can fallback to LIST timestamp (existing default behaviour).
     */
    @Override
    protected CommandFactory getFtpCommandFactory() {
        final CommandFactoryFactory factory = new CommandFactoryFactory();
        factory.addCommand("FEAT", new Command() {
            
            @Override
            public void execute(FtpIoSession session, FtpServerContext context, FtpRequest request)
                    throws IOException, FtpException {
                session.resetState();
                
                final String replyMsg = FtpReplyTranslator.translateMessage(session, request, context, 
                        FtpReply.REPLY_211_SYSTEM_STATUS_REPLY, "FEAT", null);
                final LocalizedFtpReply reply = new LocalizedFtpReply(FtpReply.REPLY_211_SYSTEM_STATUS_REPLY, replyMsg.replaceFirst(" MDTM\\n", ""));
                
                session.write(reply);
            }
        });
        return factory.createCommandFactory();
    }
}

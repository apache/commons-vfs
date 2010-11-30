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
package org.apache.commons.vfs2.provider;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;

/**
 * Holds the data which needs to be local to the current thread
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
class FileContentThreadData
{
    // private int state = DefaultFileContent.STATE_CLOSED;

    private final ArrayList<InputStream> instrs = new ArrayList<InputStream>();
    private final ArrayList<RandomAccessContent> rastrs = new ArrayList<RandomAccessContent>();
    private DefaultFileContent.FileContentOutputStream outstr;

    FileContentThreadData()
    {
    }

    /*
    int getState()
    {
        return state;
    }

    void setState(int state)
    {
        this.state = state;
    }
    */

    void addInstr(InputStream is)
    {
        this.instrs.add(is);
    }

    void setOutstr(DefaultFileContent.FileContentOutputStream os)
    {
        this.outstr = os;
    }

    DefaultFileContent.FileContentOutputStream getOutstr()
    {
        return this.outstr;
    }

    void addRastr(RandomAccessContent ras)
    {
        this.rastrs.add(ras);
    }

    int getInstrsSize()
    {
        return this.instrs.size();
    }

    public Object removeInstr(int pos)
    {
        return this.instrs.remove(pos);
    }

    public void removeInstr(InputStream instr)
    {
        this.instrs.remove(instr);
    }

    public Object removeRastr(int pos)
    {
        return this.rastrs.remove(pos);
    }

    public void removeRastr(RandomAccessContent ras)
    {
        this.rastrs.remove(ras);
    }

    public boolean hasStreams()
    {
        return instrs.size() > 0 || outstr != null || rastrs.size() > 0;
    }

    public void closeOutstr() throws FileSystemException
    {
        outstr.close();
        outstr = null;
    }

    int getRastrsSize()
    {
        return rastrs.size();
    }
}

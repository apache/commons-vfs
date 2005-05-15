/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.RandomAccessContent;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Holds the data which needs to be local to the current thread
 */
class FileContentThreadData
{
    // private int state = DefaultFileContent.STATE_CLOSED;

    private final ArrayList instrs = new ArrayList();
    private DefaultFileContent.FileContentOutputStream outstr;
    private RandomAccessContent rastr;

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

    void setRastr(RandomAccessContent ras)
    {
        this.rastr = ras;
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

    public RandomAccessContent getRastr()
    {
        return this.rastr;
    }

    public boolean hasStreams()
    {
        return instrs.size() > 0 || outstr != null || rastr != null;
    }

    public void closeOutstr() throws FileSystemException
    {
        outstr.close();
        outstr = null;
    }

    public void closeRastr() throws IOException
    {
        rastr.close();
        rastr = null;
    }
}

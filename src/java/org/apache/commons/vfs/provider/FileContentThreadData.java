package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.RandomAccessContent;

import java.io.InputStream;
import java.util.ArrayList;

class FileContentThreadData
{
    // private int state = DefaultFileContent.STATE_CLOSED;

    private final ArrayList instrs = new ArrayList();
    private DefaultFileContent.FileContentOutputStream outstr;
    private RandomAccessContent rastr;

    private final DefaultFileContent defaultFileContent;

    FileContentThreadData(DefaultFileContent defaultFileContent)
    {
        this.defaultFileContent = defaultFileContent;
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
}

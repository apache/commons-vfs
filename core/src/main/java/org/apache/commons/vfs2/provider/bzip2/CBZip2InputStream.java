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
package org.apache.commons.vfs2.provider.bzip2;

import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.BASE_BLOCK_SIZE;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.G_SIZE;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.MAX_ALPHA_SIZE;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.MAX_CODE_LEN;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.MAX_SELECTORS;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.N_GROUPS;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.RAND_NUMS;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.RUNA;
import static org.apache.commons.vfs2.provider.bzip2.BZip2Constants.RUNB;

import java.io.IOException;
import java.io.InputStream;

/*
 * This package is based on the work done by Keiron Liddle, Aftex Software
 * <keiron@aftexsw.com> to whom the Ant project is very grateful for his
 * great code.
 */

/**
 * An input stream that decompresses from the BZip2 format (without the file
 * header chars) to be read as any other stream.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
class CBZip2InputStream extends InputStream
{
    private static final int START_BLOCK_STATE = 1;
    private static final int RAND_PART_A_STATE = 2;
    private static final int RAND_PART_B_STATE = 3;
    private static final int RAND_PART_C_STATE = 4;
    private static final int NO_RAND_PART_A_STATE = 5;
    private static final int NO_RAND_PART_B_STATE = 6;
    private static final int NO_RAND_PART_C_STATE = 7;

    private CRC crc = new CRC();
    private boolean[] inUse = new boolean[256];
    private char[] seqToUnseq = new char[256];
    private char[] unseqToSeq = new char[256];
    private char[] selector = new char[MAX_SELECTORS];
    private char[] selectorMtf = new char[MAX_SELECTORS];

    /*
     * freq table collected to save a pass over the data
     * during decompression.
     */
    private int[] unzftab = new int[256];

    private int[][] limit = new int[N_GROUPS][MAX_ALPHA_SIZE];
    private int[][] base = new int[N_GROUPS][MAX_ALPHA_SIZE];
    private int[][] perm = new int[N_GROUPS][MAX_ALPHA_SIZE];
    private int[] minLens = new int[N_GROUPS];

    private boolean streamEnd;
    private int currentChar = -1;

    private int currentState = START_BLOCK_STATE;
    private int rNToGo;
    private int rTPos;
    private int tPos;

    private int i2;
    private int count;
    private int chPrev;
    private int ch2;
    private int j2;
    private char z;

    private boolean blockRandomised;

    /*
     * always: in the range 0 .. 9.
     * The current block size is 100000 * this number.
     */
    private int blockSize100k;
    private int bsBuff;
    private int bsLive;

    private InputStream inputStream;

    private int computedBlockCRC;
    private int computedCombinedCRC;

    /*
     * index of the last char in the block, so
     * the block size == last + 1.
     */
    private int last;
    private char[] mll8;
    private int nInUse;

    /*
     * index in zptr[] of original string after sorting.
     */
    private int origPtr;

    private int storedBlockCRC;
    private int storedCombinedCRC;
    private int[] tt;

    CBZip2InputStream(final InputStream input)
    {
        bsSetStream(input);
        initialize();
        initBlock();
        setupBlock();
    }

    private static void badBlockHeader()
    {
        cadvise();
    }

    private static void blockOverrun()
    {
        cadvise();
    }

    private static void cadvise()
    {
        System.out.println("CRC Error");
        //throw new CCoruptionError();
    }

    private static void compressedStreamEOF()
    {
        cadvise();
    }

    private static void crcError()
    {
        cadvise();
    }

    /**
     * a fake <code>available</code> which always returns 1 as long as the stream is not at end.
     * This is required to make this stream work if wrapped in an BufferedInputStream.
     *
     */
    @Override
    public int available() throws IOException
    {
        if (!streamEnd)
        {
            return 1;
        }
        return 0;
    }

    @Override
    public int read()
    {
        if (streamEnd)
        {
            return -1;
        }
        else
        {
            int retChar = currentChar;
            switch (currentState)
            {
                case START_BLOCK_STATE:
                    break;
                case RAND_PART_A_STATE:
                    break;
                case RAND_PART_B_STATE:
                    setupRandPartB();
                    break;
                case RAND_PART_C_STATE:
                    setupRandPartC();
                    break;
                case NO_RAND_PART_A_STATE:
                    break;
                case NO_RAND_PART_B_STATE:
                    setupNoRandPartB();
                    break;
                case NO_RAND_PART_C_STATE:
                    setupNoRandPartC();
                    break;
                default:
                    break;
            }
            return retChar;
        }
    }

    private void setDecompressStructureSizes(int newSize100k)
    {
        if (!(0 <= newSize100k && newSize100k <= 9 && 0 <= blockSize100k
                && blockSize100k <= 9))
        {
            // throw new IOException("Invalid block size");
        }

        blockSize100k = newSize100k;

        if (newSize100k == 0)
        {
            return;
        }

        int n = BASE_BLOCK_SIZE * newSize100k;
        mll8 = new char[n];
        tt = new int[n];
    }

    private void setupBlock()
    {
        int[] cftab = new int[257];
        char ch;

        cftab[0] = 0;
        for (int i = 1; i <= 256; i++)
        {
            cftab[i] = unzftab[i - 1];
        }
        for (int i = 1; i <= 256; i++)
        {
            cftab[i] += cftab[i - 1];
        }

        for (int i = 0; i <= last; i++)
        {
            ch = mll8[i];
            tt[cftab[ch]] = i;
            cftab[ch]++;
        }
        cftab = null;

        tPos = tt[origPtr];

        count = 0;
        i2 = 0;
        ch2 = 256;
        /*
         * not a char and not EOF
         */
        if (blockRandomised)
        {
            rNToGo = 0;
            rTPos = 0;
            setupRandPartA();
        }
        else
        {
            setupNoRandPartA();
        }
    }

    private void setupNoRandPartA()
    {
        if (i2 <= last)
        {
            chPrev = ch2;
            ch2 = mll8[tPos];
            tPos = tt[tPos];
            i2++;

            currentChar = ch2;
            currentState = NO_RAND_PART_B_STATE;
            crc.updateCRC(ch2);
        }
        else
        {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    private void setupNoRandPartB()
    {
        if (ch2 != chPrev)
        {
            currentState = NO_RAND_PART_A_STATE;
            count = 1;
            setupNoRandPartA();
        }
        else
        {
            count++;
            if (count >= 4)
            {
                z = mll8[tPos];
                tPos = tt[tPos];
                currentState = NO_RAND_PART_C_STATE;
                j2 = 0;
                setupNoRandPartC();
            }
            else
            {
                currentState = NO_RAND_PART_A_STATE;
                setupNoRandPartA();
            }
        }
    }

    private void setupNoRandPartC()
    {
        if (j2 < z)
        {
            currentChar = ch2;
            crc.updateCRC(ch2);
            j2++;
        }
        else
        {
            currentState = NO_RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupNoRandPartA();
        }
    }

    private void setupRandPartA()
    {
        if (i2 <= last)
        {
            chPrev = ch2;
            ch2 = mll8[tPos];
            tPos = tt[tPos];
            if (rNToGo == 0)
            {
                rNToGo = RAND_NUMS[rTPos];
                rTPos++;
                if (rTPos == 512)
                {
                    rTPos = 0;
                }
            }
            rNToGo--;
            ch2 ^= ((rNToGo == 1) ? 1 : 0);
            i2++;

            currentChar = ch2;
            currentState = RAND_PART_B_STATE;
            crc.updateCRC(ch2);
        }
        else
        {
            endBlock();
            initBlock();
            setupBlock();
        }
    }

    private void setupRandPartB()
    {
        if (ch2 != chPrev)
        {
            currentState = RAND_PART_A_STATE;
            count = 1;
            setupRandPartA();
        }
        else
        {
            count++;
            if (count >= 4)
            {
                z = mll8[tPos];
                tPos = tt[tPos];
                if (rNToGo == 0)
                {
                    rNToGo = RAND_NUMS[rTPos];
                    rTPos++;
                    if (rTPos == 512)
                    {
                        rTPos = 0;
                    }
                }
                rNToGo--;
                z ^= ((rNToGo == 1) ? 1 : 0);
                j2 = 0;
                currentState = RAND_PART_C_STATE;
                setupRandPartC();
            }
            else
            {
                currentState = RAND_PART_A_STATE;
                setupRandPartA();
            }
        }
    }

    private void setupRandPartC()
    {
        if (j2 < z)
        {
            currentChar = ch2;
            crc.updateCRC(ch2);
            j2++;
        }
        else
        {
            currentState = RAND_PART_A_STATE;
            i2++;
            count = 0;
            setupRandPartA();
        }
    }

    private void getAndMoveToFrontDecode()
    {
        int nextSym;

        int limitLast = BASE_BLOCK_SIZE * blockSize100k;
        origPtr = readVariableSizedInt(24);

        recvDecodingTables();
        int eob = nInUse + 1;
        int groupNo = -1;
        int groupPos = 0;

        /*
         * Setting up the unzftab entries here is not strictly
         * necessary, but it does save having to do it later
         * in a separate pass, and so saves a block's worth of
         * cache misses.
         */
        for (int i = 0; i <= 255; i++)
        {
            unzftab[i] = 0;
        }

        final char[] yy = new char[256];
        for (int i = 0; i <= 255; i++)
        {
            yy[i] = (char) i;
        }

        last = -1;
        int zt;
        int zn;
        int zvec;
        int zj;
        groupNo++;
        groupPos = G_SIZE - 1;

        zt = selector[groupNo];
        zn = minLens[zt];
        zvec = bsR(zn);
        while (zvec > limit[zt][zn])
        {
            zn++;

            while (bsLive < 1)
            {
                int zzi = 0;
                try
                {
                    zzi = inputStream.read();
                }
                catch (IOException e)
                {
                    compressedStreamEOF();
                }
                if (zzi == -1)
                {
                    compressedStreamEOF();
                }
                bsBuff = (bsBuff << 8) | (zzi & 0xff);
                bsLive += 8;
            }

            zj = (bsBuff >> (bsLive - 1)) & 1;
            bsLive--;

            zvec = (zvec << 1) | zj;
        }
        nextSym = perm[zt][zvec - base[zt][zn]];

        while (true)
        {
            if (nextSym == eob)
            {
                break;
            }

            if (nextSym == RUNA || nextSym == RUNB)
            {
                char ch;
                int s = -1;
                int n = 1;
                do
                {
                    if (nextSym == RUNA)
                    {
                        s = s + (0 + 1) * n;
                    }
                    else // if( nextSym == RUNB )
                    {
                        s = s + (1 + 1) * n;
                    }
                    n = n * 2;

                    if (groupPos == 0)
                    {
                        groupNo++;
                        groupPos = G_SIZE;
                    }
                    groupPos--;
                    zt = selector[groupNo];
                    zn = minLens[zt];
                    zvec = bsR(zn);
                    while (zvec > limit[zt][zn])
                    {
                        zn++;

                        while (bsLive < 1)
                        {
                            int zzi = 0;
                            try
                            {
                                zzi = inputStream.read();
                            }
                            catch (IOException e)
                            {
                                compressedStreamEOF();
                            }
                            if (zzi == -1)
                            {
                                compressedStreamEOF();
                            }
                            bsBuff = (bsBuff << 8) | (zzi & 0xff);
                            bsLive += 8;
                        }

                        zj = (bsBuff >> (bsLive - 1)) & 1;
                        bsLive--;
                        zvec = (zvec << 1) | zj;
                    }

                    nextSym = perm[zt][zvec - base[zt][zn]];

                }
                while (nextSym == RUNA || nextSym == RUNB);

                s++;
                ch = seqToUnseq[yy[0]];
                unzftab[ch] += s;

                while (s > 0)
                {
                    last++;
                    mll8[last] = ch;
                    s--;
                }

                if (last >= limitLast)
                {
                    blockOverrun();
                }
                continue;
            }
            else
            {
                char tmp;
                last++;
                if (last >= limitLast)
                {
                    blockOverrun();
                }

                tmp = yy[nextSym - 1];
                unzftab[seqToUnseq[tmp]]++;
                mll8[last] = seqToUnseq[tmp];

                /*
                 * This loop is hammered during decompression,
                 * hence the unrolling.
                 * for (j = nextSym-1; j > 0; j--) yy[j] = yy[j-1];
                 */
                int j = nextSym - 1;
                for (; j > 3; j -= 4)
                {
                    yy[j] = yy[j - 1];
                    yy[j - 1] = yy[j - 2];
                    yy[j - 2] = yy[j - 3];
                    yy[j - 3] = yy[j - 4];
                }
                for (; j > 0; j--)
                {
                    yy[j] = yy[j - 1];
                }

                yy[0] = tmp;

                if (groupPos == 0)
                {
                    groupNo++;
                    groupPos = G_SIZE;
                }
                groupPos--;
                zt = selector[groupNo];
                zn = minLens[zt];
                zvec = bsR(zn);
                while (zvec > limit[zt][zn])
                {
                    zn++;

                    while (bsLive < 1)
                    {
                        char ch = 0;
                        try
                        {
                            ch = (char) inputStream.read();
                        }
                        catch (IOException e)
                        {
                            compressedStreamEOF();
                        }

                        bsBuff = (bsBuff << 8) | (ch & 0xff);
                        bsLive += 8;
                    }

                    zj = (bsBuff >> (bsLive - 1)) & 1;
                    bsLive--;

                    zvec = (zvec << 1) | zj;
                }
                nextSym = perm[zt][zvec - base[zt][zn]];

                continue;
            }
        }
    }

    private void bsFinishedWithStream()
    {
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                // Ignore the exception.
            }
        }
        inputStream = null;
    }

    private int readVariableSizedInt(final int numBits)
    {
        return bsR(numBits);
    }

    private char readUnsignedChar()
    {
        return (char) bsR(8);
    }

    private int readInt()
    {
        int u = 0;
        u = (u << 8) | bsR(8);
        u = (u << 8) | bsR(8);
        u = (u << 8) | bsR(8);
        u = (u << 8) | bsR(8);
        return u;
    }

    private int bsR(final int n)
    {
        while (bsLive < n)
        {
            int ch = 0;
            try
            {
                ch = inputStream.read();
            }
            catch (final IOException ioe)
            {
                compressedStreamEOF();
            }

            if (ch == -1)
            {
                compressedStreamEOF();
            }

            bsBuff = (bsBuff << 8) | (ch & 0xff);
            bsLive += 8;
        }

        final int result = (bsBuff >> (bsLive - n)) & ((1 << n) - 1);
        bsLive -= n;
        return result;
    }

    private void bsSetStream(final InputStream input)
    {
        inputStream = input;
        bsLive = 0;
        bsBuff = 0;
    }

    private void complete()
    {
        storedCombinedCRC = readInt();
        if (storedCombinedCRC != computedCombinedCRC)
        {
            crcError();
        }

        bsFinishedWithStream();
        streamEnd = true;
    }

    private void endBlock()
    {
        computedBlockCRC = crc.getFinalCRC();
        /*
         * A bad CRC is considered a fatal error.
         */
        if (storedBlockCRC != computedBlockCRC)
        {
            crcError();
        }

        computedCombinedCRC = (computedCombinedCRC << 1)
                | (computedCombinedCRC >>> 31);
        computedCombinedCRC ^= computedBlockCRC;
    }

    private void hbCreateDecodeTables(final int[] limit,
                                      final int[] base,
                                      final int[] perm,
                                      final char[] length,
                                      final int minLen,
                                      final int maxLen,
                                      final int alphaSize)
    {
        int pp = 0;
        for (int i = minLen; i <= maxLen; i++)
        {
            for (int j = 0; j < alphaSize; j++)
            {
                if (length[j] == i)
                {
                    perm[pp] = j;
                    pp++;
                }
            }
        }

        for (int i = 0; i < MAX_CODE_LEN; i++)
        {
            base[i] = 0;
        }

        for (int i = 0; i < alphaSize; i++)
        {
            base[length[i] + 1]++;
        }

        for (int i = 1; i < MAX_CODE_LEN; i++)
        {
            base[i] += base[i - 1];
        }

        for (int i = 0; i < MAX_CODE_LEN; i++)
        {
            limit[i] = 0;
        }

        int vec = 0;
        for (int i = minLen; i <= maxLen; i++)
        {
            vec += (base[i + 1] - base[i]);
            limit[i] = vec - 1;
            vec <<= 1;
        }

        for (int i = minLen + 1; i <= maxLen; i++)
        {
            base[i] = ((limit[i - 1] + 1) << 1) - base[i];
        }
    }

    private void initBlock()
    {
        final char magic1 = readUnsignedChar();
        final char magic2 = readUnsignedChar();
        final char magic3 = readUnsignedChar();
        final char magic4 = readUnsignedChar();
        final char magic5 = readUnsignedChar();
        final char magic6 = readUnsignedChar();
        if (magic1 == 0x17 && magic2 == 0x72 && magic3 == 0x45 &&
                magic4 == 0x38 && magic5 == 0x50 && magic6 == 0x90)
        {
            complete();
            return;
        }

        if (magic1 != 0x31 || magic2 != 0x41 || magic3 != 0x59 ||
                magic4 != 0x26 || magic5 != 0x53 || magic6 != 0x59)
        {
            badBlockHeader();
            streamEnd = true;
            return;
        }

        storedBlockCRC = readInt();

        if (bsR(1) == 1)
        {
            blockRandomised = true;
        }
        else
        {
            blockRandomised = false;
        }

        //        currBlockNo++;
        getAndMoveToFrontDecode();

        crc.initialiseCRC();
        currentState = START_BLOCK_STATE;
    }

    private void initialize()
    {
        final char magic3 = readUnsignedChar();
        final char magic4 = readUnsignedChar();
        if (magic3 != 'h' || magic4 < '1' || magic4 > '9')
        {
            bsFinishedWithStream();
            streamEnd = true;
            return;
        }

        setDecompressStructureSizes(magic4 - '0');
        computedCombinedCRC = 0;
    }

    private void makeMaps()
    {
        nInUse = 0;
        for (int i = 0; i < 256; i++)
        {
            if (inUse[i])
            {
                seqToUnseq[nInUse] = (char) i;
                unseqToSeq[i] = (char) nInUse;
                nInUse++;
            }
        }
    }

    private void recvDecodingTables()
    {
        buildInUseTable();
        makeMaps();
        final int alphaSize = nInUse + 2;

        /*
         * Now the selectors
         */
        final int groupCount = bsR(3);
        final int selectorCount = bsR(15);
        for (int i = 0; i < selectorCount; i++)
        {
            int run = 0;
            while (bsR(1) == 1)
            {
                run++;
            }
            selectorMtf[i] = (char) run;
        }

        /*
         * Undo the MTF values for the selectors.
         */
        final char[] pos = new char[N_GROUPS];
        for (char v = 0; v < groupCount; v++)
        {
            pos[v] = v;
        }

        for (int i = 0; i < selectorCount; i++)
        {
            int v = selectorMtf[i];
            final char tmp = pos[v];
            while (v > 0)
            {
                pos[v] = pos[v - 1];
                v--;
            }
            pos[0] = tmp;
            selector[i] = tmp;
        }

        final char[][] len = new char[N_GROUPS][MAX_ALPHA_SIZE];
        /*
         * Now the coding tables
         */
        for (int i = 0; i < groupCount; i++)
        {
            int curr = bsR(5);
            for (int j = 0; j < alphaSize; j++)
            {
                while (bsR(1) == 1)
                {
                    if (bsR(1) == 0)
                    {
                        curr++;
                    }
                    else
                    {
                        curr--;
                    }
                }
                len[i][j] = (char) curr;
            }
        }

        /*
         * Create the Huffman decoding tables
         */
        for (int k = 0; k < groupCount; k++)
        {
            int minLen = 32;
            int maxLen = 0;
            for (int i = 0; i < alphaSize; i++)
            {
                if (len[k][i] > maxLen)
                {
                    maxLen = len[k][i];
                }
                if (len[k][i] < minLen)
                {
                    minLen = len[k][i];
                }
            }
            hbCreateDecodeTables(limit[k], base[k], perm[k], len[k], minLen,
                    maxLen, alphaSize);
            minLens[k] = minLen;
        }
    }

    private void buildInUseTable()
    {
        final boolean[] inUse16 = new boolean[16];

        /*
         * Receive the mapping table
         */
        for (int i = 0; i < 16; i++)
        {
            if (bsR(1) == 1)
            {
                inUse16[i] = true;
            }
            else
            {
                inUse16[i] = false;
            }
        }

        for (int i = 0; i < 256; i++)
        {
            inUse[i] = false;
        }

        for (int i = 0; i < 16; i++)
        {
            if (inUse16[i])
            {
                for (int j = 0; j < 16; j++)
                {
                    if (bsR(1) == 1)
                    {
                        inUse[i * 16 + j] = true;
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException
    {
        bsFinishedWithStream();
    }
}

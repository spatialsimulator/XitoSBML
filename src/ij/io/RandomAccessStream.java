/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.io;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Vector;


// TODO: Auto-generated Javadoc
/** This class uses a memory cache to allow seeking within
	an InputStream. Based on the JAI MemoryCacheSeekableStream class.
	Can also be constructed from a RandomAccessFile, which uses less
	memory since the memory cache is not required.
*/ 
public final class RandomAccessStream extends InputStream {

    /** The Constant BLOCK_SIZE. */
    private static final int BLOCK_SIZE = 1024;
    
    /** The Constant BLOCK_MASK. */
    private static final int BLOCK_MASK = 1023;
    
    /** The Constant BLOCK_SHIFT. */
    private static final int BLOCK_SHIFT = 10;

    /** The src. */
    private InputStream src;
    
    /** The ras. */
    private RandomAccessFile ras;
    
    /** The pointer. */
    private long pointer;
    
    /** The data. */
    private Vector data;
    
    /** The length. */
    private long length;
    
    /** The found EOS. */
    private boolean foundEOS;
    
    /**
     *  Constructs a RandomAccessStream from an InputStream. Seeking
     * 		backwards is supported using a memory cache.
     *
     * @param inputstream the inputstream
     */
	public RandomAccessStream(InputStream inputstream) {
        pointer = 0L;
        data = new Vector();
        length = 0L;
        foundEOS = false;
        src = inputstream;
    }

    /**
     *  Constructs a RandomAccessStream from an RandomAccessFile.
     *
     * @param ras the ras
     */
	public RandomAccessStream(RandomAccessFile ras) {
		this.ras = ras;
    }

    /**
     * Gets the file pointer.
     *
     * @return the file pointer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public int getFilePointer() throws IOException {
    	if (ras!=null)
    		return (int)ras.getFilePointer();
    	else
        	return (int)pointer;
    }

    /**
     * Gets the long file pointer.
     *
     * @return the long file pointer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public long getLongFilePointer() throws IOException {
    	if (ras!=null)
    		return ras.getFilePointer();
    	else
        	return pointer;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
    	if (ras!=null)
    		return ras.read();
        long l = pointer + 1L;
        long l1 = readUntil(l);
        if (l1>=l) {
            byte abyte0[] = (byte[])data.elementAt((int)(pointer>>BLOCK_SHIFT));
            return abyte0[(int)(pointer++ & BLOCK_MASK)] & 0xff;
        } else
            return -1;
    }

    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] bytes, int off, int len) throws IOException {
        if(bytes == null)
            throw new NullPointerException();
     	if (ras!=null)
    		return ras.read(bytes, off, len);
        if (off<0 || len<0 || off+len>bytes.length)
            throw new IndexOutOfBoundsException();
        if (len == 0)
            return 0;
        long l = readUntil(pointer+len);
        if (l<=pointer)
            return -1;
        else {
            byte abyte1[] = (byte[])data.elementAt((int)(pointer >> BLOCK_SHIFT));
            int k = Math.min(len, BLOCK_SIZE - (int)(pointer & BLOCK_MASK));
            System.arraycopy(abyte1, (int)(pointer & BLOCK_MASK), bytes, off, k);
            pointer += k;
            return k;
        }
    }

    /**
     * Read fully.
     *
     * @param bytes the bytes
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final void readFully(byte[] bytes) throws IOException {
        readFully(bytes, bytes.length);
    }

    /**
     * Read fully.
     *
     * @param bytes the bytes
     * @param len the len
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final void readFully(byte[] bytes, int len) throws IOException {
   		int read = 0;
        do {
            int l = read(bytes, read, len - read);
            if(l < 0) break;
            read += l;
        } while (read<len);
    }

    /**
     * Read until.
     *
     * @param l the l
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private long readUntil(long l) throws IOException {
        if (l<length)
            return l;
        if (foundEOS)
            return length;
        int i = (int)(l>>BLOCK_SHIFT);
        int j = (int)(length>>BLOCK_SHIFT);
        for (int k=j; k<=i; k++) {
            byte abyte0[] = new byte[BLOCK_SIZE];
            data.addElement(abyte0);
            int i1 = BLOCK_SIZE;
            int j1 = 0;
            while (i1>0) {
                int k1 = src.read(abyte0, j1, i1);
                if (k1==-1) {
                    foundEOS = true;
                    return length;
                }
                j1 += k1;
                i1 -= k1;
                length += k1;
            }
        }
        return length;
    }

    /**
     * Seek.
     *
     * @param loc the loc
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void seek(long loc) throws IOException {
		//IJ.log("seek (long): "+loc+"  "+(ras!=null));
    	if (ras!=null)
    		{ras.seek(loc); return;}
        if (loc<0L)
			pointer = 0L;
        else
            pointer = loc;
    }

	/**
	 * Seek.
	 *
	 * @param loc the loc
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void seek(int loc) throws IOException {
		long lloc = ((long)loc)&0xffffffffL;
		//IJ.log("seek (int): "+lloc+"  "+(ras!=null));
		if (ras!=null) {
			ras.seek(lloc);
			return;
		}
		if (lloc<0L)
			pointer = 0L;
		else
			pointer = lloc;
	}

    /**
     * Read int.
     *
     * @return the int
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final int readInt() throws IOException {
        int i = read();
        int j = read();
        int k = read();
        int l = read();
        if ((i | j | k | l) < 0)
            throw new EOFException();
        else
            return (i << 24) + (j << 16) + (k << 8) + l;
    }

    /**
     * Read long.
     *
     * @return the long
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final long readLong() throws IOException {
        return ((long)readInt()<<32) + ((long)readInt()&0xffffffffL);
    }

    /**
     * Read double.
     *
     * @return the double
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read short.
     *
     * @return the short
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final short readShort() throws IOException {
        int i = read();
        int j = read();
        if ((i | j) < 0)
            throw new EOFException();
        else
            return (short)((i<<8) + j);
    }

    /**
     * Read float.
     *
     * @return the float
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
		//ij.IJ.log("close: "+(data!=null?""+data.size():""));
 		if (ras!=null)
 			ras.close();
 		else {
 			data.removeAllElements();
    		src.close();
    	}
    }
    
 
}

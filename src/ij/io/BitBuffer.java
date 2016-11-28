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

// TODO: Auto-generated Javadoc
/**
 * A class for reading arbitrary numbers of bits from a byte array.
 * @author Eric Kjellman egkjellman at wisc.edu
 */
public class BitBuffer {

	/** The current byte. */
	private int currentByte;
	
	/** The current bit. */
	private int currentBit;
	
	/** The byte buffer. */
	private byte[] byteBuffer;
	
	/** The eof byte. */
	private int eofByte;
	
	/** The back mask. */
	private int[] backMask;
	
	/** The front mask. */
	private int[] frontMask;
	
	/** The eof flag. */
	private boolean eofFlag;

	/**
	 * Instantiates a new bit buffer.
	 *
	 * @param byteBuffer the byte buffer
	 */
	public BitBuffer(byte[] byteBuffer) {
		this.byteBuffer = byteBuffer;
		currentByte = 0;
		currentBit = 0;
		eofByte = byteBuffer.length;
		backMask = new int[] {0x0000, 0x0001, 0x0003, 0x0007,
			0x000F, 0x001F, 0x003F, 0x007F};
		frontMask = new int[] {0x0000, 0x0080, 0x00C0, 0x00E0,
			0x00F0, 0x00F8, 0x00FC, 0x00FE};
	}

	/**
	 * Gets the bits.
	 *
	 * @param bitsToRead the bits to read
	 * @return the bits
	 */
	public int getBits(int bitsToRead) {
		if (bitsToRead == 0)
			return 0;
		if (eofFlag)
			return -1; // Already at end of file
		int toStore = 0;
		while(bitsToRead != 0  && !eofFlag) {
			if (bitsToRead >= 8 - currentBit) {
				if (currentBit == 0) { // special
					toStore = toStore << 8;
					int cb = ((int) byteBuffer[currentByte]);
					toStore += (cb<0 ? (int) 256 + cb : (int) cb);
					bitsToRead -= 8;
					currentByte++;
				} else {
					toStore = toStore << (8 - currentBit);
					toStore += ((int) byteBuffer[currentByte]) & backMask[8 - currentBit];
					bitsToRead -= (8 - currentBit);
					currentBit = 0;
					currentByte++;
				}
			} else {
				toStore = toStore << bitsToRead;
				int cb = ((int) byteBuffer[currentByte]);
				cb = (cb<0 ? (int) 256 + cb : (int) cb);
				toStore += ((cb) & (0x00FF - frontMask[currentBit])) >> (8 - (currentBit + bitsToRead));
				currentBit += bitsToRead;
				bitsToRead = 0;
			}
			if (currentByte == eofByte) {
				eofFlag = true;
				return toStore;
			}
		}
		return toStore;
	}

}

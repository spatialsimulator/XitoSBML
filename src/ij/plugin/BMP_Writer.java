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
package ij.plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.LookUpTable;
import ij.WindowManager;
import ij.io.SaveDialog;

import java.awt.Image;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

// TODO: Auto-generated Javadoc
/** Implements the File/Save As/BMP command. Based on BMPFile class from
   http://www.javaworld.com/javaworld/javatips/jw-javatip60-p2.html */

public class BMP_Writer implements PlugIn {
 
 /** The Constant BITMAPFILEHEADER_SIZE. */
 //--- Private constants
 private final static int BITMAPFILEHEADER_SIZE = 14;
 
 /** The Constant BITMAPINFOHEADER_SIZE. */
 private final static int BITMAPINFOHEADER_SIZE = 40;
 //--- Private variable declaration
 /** The bitmap file header. */
 //--- Bitmap file header
 private byte bitmapFileHeader [] = new byte [14];
 
 /** The bf type. */
 private byte bfType [] =  {(byte)'B', (byte)'M'};
 
 /** The bf size. */
 private int bfSize = 0;
 
 /** The bf reserved 1. */
 private int bfReserved1 = 0;
 
 /** The bf reserved 2. */
 private int bfReserved2 = 0;
 
 /** The bf off bits. */
 private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;
 
 /** The bitmap info header. */
 //--- Bitmap info header
 private byte bitmapInfoHeader [] = new byte [40];
 
 /** The bi size. */
 private int biSize = BITMAPINFOHEADER_SIZE;
 
 /** The bi width. */
 private int biWidth = 0;
 
 /** The pad width. */
 private int padWidth = 0;
 
 /** The bi height. */
 private int biHeight = 0;
 
 /** The bi planes. */
 private int biPlanes = 1;
 
 /** The bi bit count. */
 private int biBitCount = 24;
 
 /** The bi compression. */
 private int biCompression = 0;
 
 /** The bi size image. */
 private int biSizeImage = 0;
 
 /** The bi X pels per meter. */
 private int biXPelsPerMeter = 0x0;
 
 /** The bi Y pels per meter. */
 private int biYPelsPerMeter = 0x0;
 
 /** The bi clr used. */
 private int biClrUsed = 0;
 
 /** The bi clr important. */
 private int biClrImportant = 0;
 
 /** The int bitmap. */
 //--- Bitmap raw data
 private int intBitmap [];
 
 /** The byte bitmap. */
 private byte byteBitmap [];
 
 /** The fo. */
 //--- File section
 private FileOutputStream fo;
 
 /** The bfo. */
 private BufferedOutputStream bfo;
 
 /** The imp. */
 ImagePlus imp;

 /* (non-Javadoc)
  * @see ij.plugin.PlugIn#run(java.lang.String)
  */
 public void run(String path) {
   IJ.showProgress(0);
   imp = WindowManager.getCurrentImage();
   if (imp==null)
     {IJ.noImage(); return;}
   try {
     writeImage(imp, path);
   } catch (Exception e) {
     String msg = e.getMessage();
     if (msg==null || msg.equals(""))
   msg = ""+e;
     IJ.error("BMP Writer", "An error occured writing the file.\n \n" + msg);
   }
   IJ.showProgress(1);
   IJ.showStatus("");
 }

 /**
  * Write image.
  *
  * @param imp the imp
  * @param path the path
  * @throws Exception the exception
  */
 void writeImage(ImagePlus imp, String path) throws Exception {
   if(imp.getBitDepth()==24)
     biBitCount = 24;
   else {
     biBitCount = 8;
     LookUpTable lut = imp.createLut();
     biClrUsed=lut.getMapSize(); // 8 bit color image may use less
     bfOffBits+=biClrUsed*4;
   }
   if (path==null || path.equals("")) {
     String prompt = "Save as " + biBitCount + " bit BMP";
     SaveDialog sd = new SaveDialog(prompt, imp.getTitle(), ".bmp");
     if(sd.getFileName()==null)
   return;
     path = sd.getDirectory()+sd.getFileName();
   }
   imp.startTiming();
   saveBitmap (path, imp.getImage(), imp.getWidth(), imp.getHeight() );
 }


 /**
  * Save bitmap.
  *
  * @param parFilename the par filename
  * @param parImage the par image
  * @param parWidth the par width
  * @param parHeight the par height
  * @throws Exception the exception
  */
 public void saveBitmap (String parFilename, Image parImage, int parWidth, int parHeight) throws Exception {
   fo = new FileOutputStream (parFilename);
   bfo = new BufferedOutputStream(fo);
   save (parImage, parWidth, parHeight);
   bfo.close();
   fo.close ();
 }

 /**
  * Save.
  *
  * @param parImage the par image
  * @param parWidth the par width
  * @param parHeight the par height
  * @throws Exception the exception
  */
 /*
  *   The saveMethod is the main method of the process. This method
  *   will call the convertImage method to convert the memory image to
  *   a byte array; method writeBitmapFileHeader creates and writes
  *   the bitmap file header; writeBitmapInfoHeader creates the
  *   information header; and writeBitmap writes the image.
  *
  */
 private void save (Image parImage, int parWidth, int parHeight) throws Exception {
   convertImage (parImage, parWidth, parHeight);
   writeBitmapFileHeader ();
   writeBitmapInfoHeader ();
   if(biBitCount == 8)
     writeBitmapPalette ();
   writeBitmap ();
 }

 /**
  * Write bitmap palette.
  *
  * @throws Exception the exception
  */
 private void writeBitmapPalette() throws Exception {
   LookUpTable lut = imp.createLut();
   byte[] g = lut.getGreens();
   byte[] r = lut.getReds();
   byte[] b = lut.getBlues();
   for(int i = 0;i<lut.getMapSize();i++) {
     bfo.write(b[i]);
     bfo.write(g[i]);
     bfo.write(r[i]);
     bfo.write(0x00);
   }
 }

 /**
  * Convert image.
  *
  * @param parImage the par image
  * @param parWidth the par width
  * @param parHeight the par height
  * @return true, if successful
  */
 /*
  * convertImage converts the memory image to the bitmap format (BRG).
  * It also computes some information for the bitmap info header.
  *
  */
 private boolean convertImage (Image parImage, int parWidth, int parHeight) {
   int pad;
   if(biBitCount == 24)
     intBitmap = (int[]) imp.getProcessor().getPixels();
   else
     byteBitmap = (byte[]) imp.getProcessor().convertToByte(true).getPixels();
   biWidth = parWidth;
   biHeight = parHeight;
   if(biBitCount==24)
     pad = 4 - ((biWidth * 3) % 4);
   else
     pad = 4 - ((biWidth) % 4);
   if (pad == 4)       // <==== Bug correction
     pad = 0;            // <==== Bug correction
   padWidth = biWidth*(biBitCount==24?3:1)+pad;
   return (true);
 }

 /**
  * Write bitmap.
  *
  * @throws Exception the exception
  */
 /*
  * writeBitmap converts the image returned from the pixel grabber to
  * the format required. Remember: scan lines are inverted in
  * a bitmap file!
  *
  * Each scan line must be padded to an even 4-byte boundary.
  */
 private void writeBitmap () throws Exception {
   int value;
   int i;
   int pad;
   byte rgb [] = new byte [3];
   if(biBitCount==24)
     pad = 4 - ((biWidth * 3) % 4);
   else
     pad = 4 - ((biWidth) % 4);
   if (pad == 4)       // <==== Bug correction
     pad = 0;            // <==== Bug correction

   int counter=0;
   for(int row = biHeight; row>0; row--) {
     if (row%20==0)
   IJ.showProgress((double)(biHeight-row)/biHeight);
     for(int col = 0; col<biWidth; col++) {
   if(biBitCount==24) {
     value = intBitmap [(row-1)*biWidth + col ];
     rgb [0] = (byte) (value & 0xFF);
     rgb [1] = (byte) ((value >> 8) & 0xFF);
     rgb [2] = (byte) ((value >> 16) & 0xFF);
     bfo.write(rgb);
   } else
     bfo.write(byteBitmap [(row-1)*biWidth + col ]);
   ++counter;
     }
     for (i = 1; i <= pad; i++)
   bfo.write (0x00);
     counter += pad;
   }
   // IJ.write("counter of bytes written = " + counter);
 }


 /**
  * Write bitmap file header.
  *
  * @throws Exception the exception
  */
 /*
  * writeBitmapFileHeader writes the bitmap file header to the file.
  *
  */
 private void writeBitmapFileHeader() throws Exception {
   fo.write (bfType);
   // calculate bfSize
   bfSize = bfOffBits+padWidth*biHeight;
   fo.write (intToDWord (bfSize));
   fo.write (intToWord (bfReserved1));
   fo.write (intToWord (bfReserved2));
   fo.write (intToDWord (bfOffBits));
   // IJ.write("biClrUsed = " + biClrUsed + " bfSize = " + bfSize + " bfOffBits=" + bfOffBits);
 }

 /**
  * Write bitmap info header.
  *
  * @throws Exception the exception
  */
 /*
  *
  * writeBitmapInfoHeader writes the bitmap information header
  * to the file.
  *
  */
 private void writeBitmapInfoHeader () throws Exception {
   fo.write (intToDWord (biSize));
   fo.write (intToDWord (biWidth));
   fo.write (intToDWord (biHeight));
   fo.write (intToWord (biPlanes));
   fo.write (intToWord (biBitCount));
   fo.write (intToDWord (biCompression));
   fo.write (intToDWord (biSizeImage));
   fo.write (intToDWord (biXPelsPerMeter));
   fo.write (intToDWord (biYPelsPerMeter));
   fo.write (intToDWord (biClrUsed));
   fo.write (intToDWord (biClrImportant));
 }

 /**
  * Int to word.
  *
  * @param parValue the par value
  * @return the byte[]
  */
 /*
  *
  * intToWord converts an int to a word, where the return
  * value is stored in a 2-byte array.
  *
  */
 private byte [] intToWord (int parValue) {
   byte retValue [] = new byte [2];
   retValue [0] = (byte) (parValue & 0x00FF);
   retValue [1] = (byte) ((parValue >> 8) & 0x00FF);
   return (retValue);
 }

 /**
  * Int to D word.
  *
  * @param parValue the par value
  * @return the byte[]
  */
 /*
  *
  * intToDWord converts an int to a double word, where the return
  * value is stored in a 4-byte array.
  *
  */
 private byte [] intToDWord (int parValue) {
   byte retValue [] = new byte [4];
   retValue [0] = (byte) (parValue & 0x00FF);
   retValue [1] = (byte) ((parValue >> 8) & 0x000000FF);
   retValue [2] = (byte) ((parValue >> 16) & 0x000000FF);
   retValue [3] = (byte) ((parValue >> 24) & 0x000000FF);
   return (retValue);
 }
}

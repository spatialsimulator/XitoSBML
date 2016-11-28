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
import ij.IJ;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

// TODO: Auto-generated Javadoc
/**
 * This class provides the functionality to divert output sent to the System.out
 * and System.err streams to ImageJ's log console. The purpose is to allow 
 * use of existing Java classes or writing new generic Java classes that only 
 * output to System.out and are thus less dependent on ImageJ.
 * See the ImageJ plugin Redirect_System_Streams at
 *    http://staff.fh-hagenberg.at/burger/imagej/
 * for usage examples.
 *
 * @author Wilhelm Burger (wilbur at ieee.org)
 * See Also: Redirect_System_Streams (http://staff.fh-hagenberg.at/burger/imagej/)
 */
public class LogStream extends PrintStream {
	
	/** The out prefix. */
	private static String outPrefix = "out> ";	// prefix string for System.out
	
	/** The err prefix. */
	private static String errPrefix = "err >";	// prefix string for System.err
	
	/** The original system out. */
	private static PrintStream originalSystemOut = null;
	
	/** The original system err. */
	private static PrintStream originalSystemErr = null;
	
	/** The temporary system out. */
	private static PrintStream temporarySystemOut = null;
	
	/** The temporary system err. */
	private static PrintStream temporarySystemErr = null;
	
	/**
	 * Redirects all output sent to <code>System.out</code> and <code>System.err</code> to ImageJ's log console
	 * using the default prefixes.
	 *
	 * @param redirect the redirect
	 */
	public static void redirectSystem(boolean redirect) {
		if (redirect)
			redirectSystem();
		else
			revertSystem();
	}
	
	/**
	 * Redirects all output sent to <code>System.out</code> and <code>System.err</code> to ImageJ's log console
	 * using the default prefixes.
	 * Alternatively use 
	 * {@link #redirectSystemOut(String)} and {@link #redirectSystemErr(String)}
	 * to redirect the streams separately and to specify individual prefixes.
	 */
	public static void redirectSystem() {
		redirectSystemOut(outPrefix);
		redirectSystemErr(errPrefix);
	}

	/**
	 * Redirects all output sent to <code>System.out</code> to ImageJ's log console.
	 * @param prefix The prefix string inserted at the start of each output line. 
	 * Pass <code>null</code>  to use the default prefix or an empty string to 
	 * remove the prefix.
	 */
	public static void redirectSystemOut(String prefix) {
		if (originalSystemOut == null) {		// has no effect if System.out is already replaced
			originalSystemOut = System.out;		// remember the original System.out stream
			temporarySystemOut = new LogStream(prefix);
			System.setOut(temporarySystemOut);
		}
	}
	
	/**
	 * Redirects all output sent to <code>System.err</code> to ImageJ's log console.
	 * @param prefix The prefix string inserted at the start of each output line. 
	 * Pass <code>null</code>  to use the default prefix or an empty string to 
	 * remove the prefix.
	 */
	public static void redirectSystemErr(String prefix) {
		if (originalSystemErr == null) {		// has no effect if System.out is already replaced
			originalSystemErr = System.err;		// remember the original System.out stream
			temporarySystemErr = new LogStream(prefix);
			System.setErr(temporarySystemErr);
		}
	}
	
	/**
	 * Returns the redirection stream for {@code System.out} if it exists.
	 * Note that a reference to the current output stream can also be obtained directly from 
	 * the {@code System.out} field.
	 * @return A reference to the {@code PrintStream} object currently substituting {@code System.out}
	 * or {@code null} of if {@code System.out} is currently not redirected.
	 */
	public static PrintStream getCurrentOutStream() {
		return temporarySystemOut;
	}
	
	/**
	 * Returns the redirection stream for {@code System.err} if it exists.
	 * Note that a reference to the current output stream can also be obtained directly from 
	 * the {@code System.err} field.
	 * @return A reference to the {@code PrintStream} object currently substituting {@code System.err}
	 * or {@code null} of if {@code System.err} is currently not redirected.
	 */
	public static PrintStream getCurrentErrStream() {
		return temporarySystemErr;
	}
	
	/**
	 * Use this method to revert both <code>System.out</code> and <code>System.err</code> 
	 * to their original output streams.
	 */
	public static void revertSystem() {
		revertSystemOut();
		revertSystemErr();
	}

	/**
	 * Use this method to revert<code>System.out</code>
	 * to the original output stream.
	 */
	public static void revertSystemOut() {
		if (originalSystemOut != null && temporarySystemOut != null) {
			temporarySystemOut.flush();
			temporarySystemOut.close();
			System.setOut(originalSystemOut);
			originalSystemOut = null;
			temporarySystemOut = null;
		}
	}
	
	/**
	 * Use this method to revert<code>System.err</code>
	 * to the original output stream.
	 */
	public static void revertSystemErr() {
		if (originalSystemErr != null && temporarySystemErr != null) {
			temporarySystemErr.flush();
			temporarySystemErr.close();
			System.setErr(originalSystemErr);
			originalSystemErr = null;
			temporarySystemErr = null;
		}
	}
	
	// ----------------------------------------------------------------
	
	/** The end of line system. */
	private final String endOfLineSystem = System.getProperty("line.separator"); 
	
	/** The end of line short. */
	private final String endOfLineShort = String.format("\n"); 	
	
	/** The byte stream. */
	private final ByteArrayOutputStream byteStream;
	
	/** The prefix. */
	private final String prefix;
	
	/**
	 * Instantiates a new log stream.
	 */
	public LogStream() {
		super(new ByteArrayOutputStream());
		this.byteStream = (ByteArrayOutputStream) this.out;
		this.prefix = "";
	}

	/**
	 * Instantiates a new log stream.
	 *
	 * @param prefix the prefix
	 */
	private LogStream(String prefix) {
		super(new ByteArrayOutputStream());
		this.byteStream = (ByteArrayOutputStream) this.out;
		this.prefix = (prefix == null) ? "" : prefix;
	}
	
	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	@Override
	// ever called?
	public void write(byte[] b) {
		this.write(b, 0, b.length);
	}
	
	/* (non-Javadoc)
	 * @see java.io.PrintStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) {
		String msg = new String(b, off, len);
		if (msg.equals(endOfLineSystem) || msg.equals(endOfLineShort)) { // this is a newline sequence only
			ejectBuffer();
		} else {
			byteStream.write(b, off, len);	// append message to buffer
			if (msg.endsWith(endOfLineSystem) || msg.endsWith(endOfLineShort)) { // line terminated by Newline
				// note that this does not seem to happen ever (even with format)!?
				ejectBuffer();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.io.PrintStream#write(int)
	 */
	@Override
	// ever called?
	public void write(int b) {
		byteStream.write(b);
	}

	/* (non-Javadoc)
	 * @see java.io.PrintStream#flush()
	 */
	@Override
	public void flush() {
		if (byteStream.size() > 0) {
			String msg = byteStream.toString();
			if (msg.endsWith(endOfLineSystem) || msg.endsWith(endOfLineShort))
				ejectBuffer();
		}
		super.flush();
	}
	
	/* (non-Javadoc)
	 * @see java.io.PrintStream#close()
	 */
	@Override
	public void close() {
		super.close();
	}
	
	/**
	 * Eject buffer.
	 */
	private void ejectBuffer() {
		IJ.log(prefix + byteStream.toString());
		byteStream.reset();
	}
	
}

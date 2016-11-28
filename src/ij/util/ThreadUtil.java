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
package ij.util;

// TODO: Auto-generated Javadoc
/**
 * The Class ThreadUtil.
 */
public class ThreadUtil {
	
	/**
	 *  Start all given threads and wait on each of them until all are done.
	 * From Stephan Preibisch's Multithreading.java class. See:
	 * http://repo.or.cz/w/trakem2.git?a=blob;f=mpi/fruitfly/general/MultiThreading.java;hb=HEAD
	 *
	 * @param threads the threads
	 */
	public static void startAndJoin(Thread[] threads) {
		for (int ithread = 0; ithread < threads.length; ++ithread) {
			threads[ithread].setPriority(Thread.NORM_PRIORITY);
			threads[ithread].start();
		}

		try {
			for (int ithread = 0; ithread < threads.length; ++ithread) {
				threads[ithread].join();
			}
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

	/**
	 * Creates the thread array.
	 *
	 * @param nb the nb
	 * @return the thread[]
	 */
	public static Thread[] createThreadArray(int nb) {
		if (nb == 0) {
			nb = getNbCpus();
		}
		Thread[] threads = new Thread[nb];

		return threads;
	}

	/**
	 * Creates the thread array.
	 *
	 * @return the thread[]
	 */
	public static Thread[] createThreadArray() {
		return createThreadArray(0);
	}

	/**
	 * Gets the nb cpus.
	 *
	 * @return the nb cpus
	 */
	public static int getNbCpus() {
		return Runtime.getRuntime().availableProcessors();
	}

}

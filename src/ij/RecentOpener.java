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
package ij;
import ij.io.Opener;

import java.awt.Menu;
import java.awt.MenuItem;

// TODO: Auto-generated Javadoc
/** Opens, in a separate thread, files selected from the File/Open Recent submenu.*/
public class RecentOpener implements Runnable {
	
	/** The path. */
	private String path;

	/**
	 * Instantiates a new recent opener.
	 *
	 * @param path the path
	 */
	RecentOpener(String path) {
		this.path = path;
		Thread thread = new Thread(this, "RecentOpener");
		thread.start();
	}

	/** Open the file and move the path to top of the submenu. */
	public void run() {
		Opener o = new Opener();
		o.open(path);
		Menu menu = Menus.getOpenRecentMenu();
		int n = menu.getItemCount();
		int index = 0;
		for (int i=0; i<n; i++) {
			if (menu.getItem(i).getLabel().equals(path)) {
				index = i;
				break;
			}
		}
		if (index>0) {
			MenuItem item = menu.getItem(index);
			menu.remove(index);
			menu.insert(item, 0);
		}
	}

}


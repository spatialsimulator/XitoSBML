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
package ij.gui;
import java.awt.Button;
import java.awt.Dimension;

// TODO: Auto-generated Javadoc
/** This is an extended Button class used to reduce the width of the HUGE buttons on Mac OS X. */
public class TrimmedButton extends Button {
    
    /** The trim. */
    private int trim = 0;
    
    /**
     * Instantiates a new trimmed button.
     *
     * @param title the title
     * @param trim the trim
     */
    public TrimmedButton(String title, int trim) {
        super(title);
        this.trim = trim;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getMinimumSize()
     */
    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width-trim, super.getMinimumSize().height);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

}

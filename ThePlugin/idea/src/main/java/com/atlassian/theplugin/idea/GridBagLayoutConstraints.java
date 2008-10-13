/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea;

import java.awt.*;

/**
 This class simplifies the use of the GridBagConstraints
 class.
 */
public class GridBagLayoutConstraints extends GridBagConstraints {

	static final long serialVersionUID = 3992743098660874985L;

	/**
     Constructs a GridBagLayoutConstraints with a given gridx and gridy position and
     all other grid bag constraint values set to the default.
     @param gridx the gridx position
     @param gridy the gridy position
     */
    public GridBagLayoutConstraints(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    /**
     Sets the cell spans.
     @param gridwidth the cell span in x-direction
     @param gridheight the cell span in y-direction
     @return this object for further modification
     */
    public GridBagLayoutConstraints setSpan(int gridwidth, int gridheight) {
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
        return this;
    }

    /**
     Sets the anchor.
     @param anchor the anchor value
     @return this object for further modification
     */
    public GridBagLayoutConstraints setAnchor(int anchor) {
        this.anchor = anchor;
        return this;
    }

    /**
     Sets the fill direction.
     @param fill the fill direction
     @return this object for further modification
     */
    public GridBagLayoutConstraints setFill(int fill) {
        this.fill = fill;
        return this;
    }

    /**
     Sets the cell weights.
     @param weightx the cell weight in x-direction
     @param weighty the cell weight in y-direction
     @return this object for further modification
     */
    public GridBagLayoutConstraints setWeight(double weightx, double weighty) {
        this.weightx = weightx;
        this.weighty = weighty;
        return this;
    }

    /**
     Sets the insets of this cell.
     @param distance the spacing to use in all directions
     @return this object for further modification
     */
    public GridBagLayoutConstraints setInsets(int distance) {
        this.insets = new java.awt.Insets(
                distance, distance, distance, distance);
        return this;
    }

    /**
     Sets the insets of this cell.
     @param top the spacing to use on top
     @param left the spacing to use to the left
     @param bottom the spacing to use on the bottom
     @param right the spacing to use to the right
     @return this object for further modification
     */
    public GridBagLayoutConstraints setInsets(int top, int left, int bottom, int right) {
        this.insets = new java.awt.Insets(
                top, left, bottom, right);
        return this;
    }

    /**
     Sets the internal padding
     @param ipadx the internal padding in x-direction
     @param ipady the internal padding in y-direction
     @return this object for further modification
     */
    public GridBagLayoutConstraints setIpad(int ipadx, int ipady) {
        this.ipadx = ipadx;
        this.ipady = ipady;
        return this;
    }
}

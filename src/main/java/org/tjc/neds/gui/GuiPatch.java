/*
 * Copyright (c) 2005, Thomas J. Clancy
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in 
 * the Software without restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * 	1. 	The above copyright notice and this permission notice shall be included in 
 * 		all	copies or substantial portions of the Software. 
 * 
 * 	2.	Neither the name of the organization nor the names of its contributors may 
 * 		be used to endorse or promote products derived from this software without 
 * 		specific prior written permission.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *  
 */
package org.tjc.neds.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tjc.neds.simulation.Coordinate;
import org.tjc.neds.simulation.Dimensions;
import org.tjc.neds.simulation.Neds;
import org.tjc.neds.simulation.Patch;
import org.tjc.neds.simulation.Rng;

/**
 *
 * @author Thomas
 */
public class GuiPatch {

    private static final long serialVersionUID = 5697856423431538249L;
    private static final Logger log = LoggerFactory.getLogger(GuiPatch.class);
    private Patch patch = null;
    private Coordinate upperLeft;
    private Coordinate patchCoord;
    private Dimensions size;
    private Dimensions divisionDimensions = new Dimensions(1, 1);
    private Color grass = Color.green;
    private List<Rectangle> divisions;
    private Neds neds;

    public GuiPatch(Neds neds, Patch patch, Coordinate ul, Coordinate patchCoord, Dimensions size) {
        this.upperLeft = ul;
        this.patchCoord = patchCoord;
        this.size = size;
        this.patch = patch;
        this.neds = neds;
        this.initializeDivisions();
    }

    public GuiPatch(Neds neds, Patch patch, Coordinate ul, Coordinate patchCoord, Dimensions size,
        Dimensions divisionDimensions) {
        this(neds, patch, ul, patchCoord, size);
        this.divisionDimensions = divisionDimensions;
    }

    public void paint(Graphics2D g) {
        paintPatch(g);
        //paintStats(g);
    }

    public Coordinate getUpperLeft() {
        return upperLeft;
    }

    public Dimensions getSize() {
        return size;
    }

    private void paintPatch(Graphics2D g) {
        double d = patch.getPercentRemainingFood();
        int alpha = (int) ((d / 100.0) * 200.0);

        if (alpha < 1) {
            alpha = 1;
        }

        Collections.shuffle(divisions);

        for (Rectangle r : divisions) {
            int da = 200;
            if (d < 99.0) {
                int s = alpha - 10;
                if (s < 1) {
                    s = 1;
                }
                da = Rng.getInstance().nextInt(alpha) + s;
                if (da > 200) {
                    da = 200;
                }
            }
            grass = new Color(0, 255, 0, da);
            g.setColor(grass);
            g.fill(r);
        }
    }

    private void paintStats(Graphics2D g) {
        g.setColor(Color.white);
        Font f = g.getFont();
        Font d = f.deriveFont(11.0f);
        int males = neds.getMaleCount(patchCoord);
        int females = neds.getFemaleCount(patchCoord);
        String str = "food(" + patch.getFood() + ")  m(" + males + ") f(" + females + ")";
        Rectangle2D r = d.getStringBounds(str, g.getFontRenderContext());
        g.setFont(d);
        g.drawString(str, upperLeft.getX() + 2, (int) (upperLeft.getY() + r.getHeight() - 1));
    }

    private void initializeDivisions() {
        if (divisions == null) {
            divisions = new ArrayList<>();

            int hw = size.getWidth() / this.divisionDimensions.getWidth();
            int hh = size.getHeight() / this.divisionDimensions.getHeight();
            int ux = upperLeft.getX();
            int uy = upperLeft.getY();

            for (int rows = 0; rows < this.divisionDimensions.getHeight(); rows++) {
                for (int cols = 0; cols < this.divisionDimensions.getWidth(); cols++) {
                    Rectangle r = new Rectangle(ux, uy, hw * (cols + 1), hh * (rows + 1));
                    divisions.add(r);
                }
            }
        }
    }
}

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tjc.neds.simulation.Coordinate;
import org.tjc.neds.simulation.Dimensions;
import org.tjc.neds.simulation.Ned;
import org.tjc.neds.simulation.Neds;
import org.tjc.neds.simulation.NedsEvent;
import org.tjc.neds.simulation.NedsEventListener;
import org.tjc.neds.simulation.Patch;

public class NedField extends JPanel implements NedsEventListener, ComponentListener {

    private static final Logger log = LoggerFactory.getLogger(NedField.class);
    private static final long serialVersionUID = -1931472112999786156L;
    private static final ReentrantLock lock = new ReentrantLock();
    private GuiPatch[][] guiPatches = null;
    private Patch[][] patches = null;
    private int rows;
    private int cols;
    private boolean initialized = false;
    private final Dimensions dimensions;
    private Neds neds;

    public NedField(Neds neds) {
        super(true);
        this.neds = neds;
        this.dimensions = neds.getDimension();
        this.neds = neds;
        this.patches = neds.getFields();
        this.setLayout(new GridLayout(dimensions.getHeight(), dimensions.getWidth()));
        this.neds.addNedsEventListener(this);
        addComponentListener(this);
    }

    public Dimensions getDimension() {
        return dimensions;
    }

    @Override
    public void paintComponent(Graphics g) {
        lock.lock();
        try {
            if (!initialized) {
                initialize();
            }
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.black);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            paintPatches(g2d);
            drawGrid(g2d);
            paintNedsRandomly(g2d);
        }
        finally {
            lock.unlock();
        }
    }

    private void paintPatches(Graphics2D g) {
        for (int y = 0; y < cols; y++) {
            for (int x = 0; x < rows; x++) {
                GuiPatch p = guiPatches[x][y];
                p.paint(g);
            }
        }
    }

    private void drawGrid(Graphics2D g) {
        int w = this.getWidth() / dimensions.getHeight();
        int h = this.getHeight() / dimensions.getHeight();

        g.setColor(Color.GRAY);
        for (int x = 0; x < w; x++) {
            g.drawLine(0, x * h, this.getWidth(), x * h);
        }

        for (int y = 0; y < h; y++) {
            g.drawLine(y * w, 0, y * w, this.getHeight());
        }
    }

    private void paintNedsRandomly(final Graphics2D g) {
        Set<Ned> ns = neds.getAllNeds();
        ns.stream().
            forEach((ned) -> {
                GuiPatch gp;
                Coordinate c;
                if (ned.getPoint() == null) {
                    gp = getGuiPatch(ned.getCoord());
                    c = Coordinate.newRandomCoordinate(gp.getUpperLeft(), gp.getSize());
                    ned.setPoint(new Point(c.getX(), c.getY()));
                }
                else {
                    c = new Coordinate(ned.getPoint().x, ned.getPoint().y);
                }
                if (ned.getSex() == 'm') {
                    g.setColor(Color.blue);
                }
                else {
                    g.setColor(Color.pink);
                }
                Dimensions nedSize;
                if (ned.getAge() < 12) {
                    nedSize = new Dimensions(5, 5);
                }
                else {
                    nedSize = new Dimensions(10, 10);
                }
                int x = c.getX();
                int y = c.getY();
                int nw = nedSize.getWidth();
                int nh = nedSize.getHeight();
                g.fillOval(x - nw / 2, y - nh / 2, nw, nh);
                g.setColor(Color.black);
                g.drawOval(x - nw / 2, y - nh / 2, nw, nh);
            });
    }

    private GuiPatch getGuiPatch(Coordinate c) {
        return guiPatches[c.getX()][c.getY()];
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        log.debug("initialize: {}", dimensions);
        this.patches = neds.getFields();

        int w = this.getWidth();
        int h = this.getHeight();

        rows = dimensions.getHeight();
        cols = dimensions.getWidth();

        int patchWidth = w / cols;
        int patchHeight = h / rows;

        Dimensions patchDimensions = new Dimensions(patchWidth, patchHeight);

        guiPatches = new GuiPatch[cols][rows];

        neds.resetNedPoint();

        lock.lock();
        try {
            int top = 0;
            for (int y = 0; y < rows; y++) {
                int left = 0;
                for (int x = 0; x < cols; x++) {
                    Coordinate c = new Coordinate(left, top);
                    Coordinate pc = new Coordinate(x, y);
                    GuiPatch gp = new GuiPatch(neds, patches[x][y], c, pc, patchDimensions);
                    guiPatches[x][y] = gp;
                    left += patchWidth;
                }
                top += patchHeight;
            }
            setVisible(true);
            initialized = true;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void handle(NedsEvent event) {
        if (event.getEvent().equals("step")) {
            repaint(0);
        }

    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentResized(ComponentEvent e) {
        this.initialize();
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

}

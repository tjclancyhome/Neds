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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tjc.neds.simulation.Dimensions;
import org.tjc.neds.simulation.Ned;
import org.tjc.neds.simulation.Neds;
import org.tjc.neds.simulation.NedsEvent;
import org.tjc.neds.simulation.NedsEventListener;
import org.tjc.neds.simulation.Range;
import org.tjc.neds.simulation.Rng;
import org.tjc.neds.simulation.util.Resources;

/**
 * The primary Swing-based application. This was originally generated with Eclipse and its Visual
 * Editor plugin. It's a simple JFrame with a menu bar and some menu items. Nothing fancy yet. The
 * JFrame application acts as a listener for both SimulatorEvents and ModelEvents. SimulatorEvents
 * are of the kind Start, Stop, End, Reset. When you call one of the methods of the simulator... say
 * stop(), for example, when the simulator stops the simulation it sends an event to all listeners
 * that the simulation has indeed stopped (it might take a moment for the thread to complete its
 * current tick, for example).
 *
 * A ModelEvent is of the kind PreStep, Step, PostStep, EndStep, etc. This is useful for a Gui so
 * that it can know, for example, when to repaint. When the application receives the EndStep it
 * knows that the simulator has finished calling all step methods on the model and hence has
 * finished executing all actions produced by the model for the current step iteration and so the
 * Gui knows it can safely update to show the present state of the simulation.
 *
 * @author tjclancy
 *
 */
public class NedWorld extends JFrame implements NedsEventListener, ActionListener, ChangeListener {

    private static final Logger log = LoggerFactory.getLogger(NedWorld.class);
    private static final long serialVersionUID = 1476561849859539192L;
    private static final int DEFAULT_DELAY = 550; // milliseconds
    private static final int DEFAULT_POPULATION = 20;
    private static final Dimensions DEFAULT_DIMENSIONS = new Dimensions(8, 8);
    private static final Range<Integer> DEFAULT_FOOD_RANGE = new Range<>(0, 50);

    private NedField nedField = null;
    private JPanel jContentPane = null;
    private JMenuBar jJMenuBar = null;
    private JMenu jMenu = null;
    private JMenuItem jMenuItem1 = null;
    private JMenuItem jMenuItem3 = null;
    private JMenuItem jMenuItem4 = null;
    private JMenuItem jMenuItem5 = null;
    private JMenu jMenu2 = null;
    private JLabel newBorns = null;
    private JLabel oldestNed = null;
    private JLabel deaths = null;
    private JLabel population = null;
    private JLabel day = null;
    private JLabel seed = null;
    private JSlider speedSlider = null;
    private Neds neds;
    private Thread nedsThread;
    private final int delay = DEFAULT_DELAY;
    private final Dimensions dimensions;
    private int currStep;
    private JPanel statusBar;
    private JToolBar animToolBar;
    private final Map<String, JButton> buttons = new HashMap<>();

    private ImageIcon playing;
    private ImageIcon play;

    public NedWorld(Dimensions dimensions) throws HeadlessException {
        super();
        log.debug("Constructing NedWorld");
        this.dimensions = dimensions;
        initialize();
    }

    public NedWorld(GraphicsConfiguration gc, Dimensions dimensions) {
        super(gc);
        log.debug("Constructing NedWorld");
        this.dimensions = dimensions;
        initialize();
    }

    public NedWorld(String title, Dimensions dimensions) throws HeadlessException {
        super(title);
        log.debug("Constructing NedWorld");
        this.dimensions = dimensions;
        initialize();
    }

    public NedWorld(String title, GraphicsConfiguration gc, Dimensions dimensions) {
        super(title, gc);
        log.debug("Constructing NedWorld");
        this.dimensions = dimensions;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        neds = newNeds();
        this.setTitle("NedWorld 0.1");
        this.setBounds(0, 0, 500, 400);
        this.setMinimumSize(new java.awt.Dimension(500, 400));
        this.setContentPane(getJContentPane());
        this.setJMenuBar(getJJMenuBar());
        this.play = new ImageIcon(Resources.getResource("images/PlayButton.png"));
        this.playing = new ImageIcon(Resources.getResource("images/PlayingButton.png"));
        add(getAnimationToolbar(), BorderLayout.PAGE_START);
        updateStats();
    }

    private JToolBar getAnimationToolbar() {
        if (animToolBar == null) {
            animToolBar = new JToolBar();
            animToolBar.setFloatable(false);
            addButton(animToolBar, "images/New.png", "images/NewPressed.png", "New",
                "Start a new ned session");
            addButton(animToolBar, "images/Save.png", "images/SavePressed.png", "Save",
                "Save ned session");
            animToolBar.addSeparator();
            addButton(animToolBar, "images/Reset.png", "images/ResetPressed.png", "Reset",
                "Reset current ned session");
            addButton(animToolBar, "images/PauseButton.png", "images/PausePressedButton.png",
                "Pause", "Pause running ned session");
            addButton(animToolBar, "images/PlayButton.png", "images/PlayPressedButton.png", "Play",
                "Play ned session");
            animToolBar.addSeparator();
            addSpeedSlider(animToolBar);
            animToolBar.addSeparator();
            initializeButtons();
        }
        return animToolBar;
    }

    private void addSpeedSlider(JToolBar tb) {
        if (speedSlider == null) {
            speedSlider = new JSlider(0, 2000, 1000);
            speedSlider.setPaintTicks(true);
            speedSlider.setMajorTickSpacing(100);
            speedSlider.setMinorTickSpacing(50);
            speedSlider.setMaximumSize(new Dimension(300, 25));
            speedSlider.setInverted(true);
            speedSlider.addChangeListener(this);
            neds.setDelay(speedSlider.getValue());
            tb.add(speedSlider);
        }
    }

    private void initializeButtons() {
        buttons.get("Pause").setEnabled(false);
        buttons.get("Play").setEnabled(true);
    }

    private void addButton(JToolBar tb, String icon, String pressedIcon, String actionCommand,
        String toolTip) {
        URL iconUrl = Resources.getResource(icon);
        if (iconUrl != null) {
            JButton button = new JButton();
            button.setActionCommand(actionCommand);
            button.addActionListener(this);
            button.setToolTipText(toolTip);
            button.setBorderPainted(false);

            ImageIcon iconImg = new ImageIcon(iconUrl, actionCommand);
            button.setIcon(iconImg);

            URL pressedUrl = Resources.getResource(pressedIcon);
            if (pressedUrl != null) {
                ImageIcon pressedImg = new ImageIcon(pressedUrl, actionCommand);
                button.setPressedIcon(pressedImg);
            }
            buttons.put(actionCommand, button);
            tb.add(button);
        }
        else {
            JButton button = new JButton(actionCommand);
            button.setToolTipText(toolTip);
            tb.add(button);
        }
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setDoubleBuffered(true);
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getNedField(), BorderLayout.CENTER);
            jContentPane.add(getStatusBar(), java.awt.BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    private Component getStatusBar() {
        if (statusBar == null) {
            statusBar = new JPanel();
            GridLayout gl = new GridLayout(2, 5);
            statusBar.setLayout(gl);
            oldestNed = new JLabel();
            newBorns = new JLabel();
            deaths = new JLabel();
            population = new JLabel();
            day = new JLabel();
            seed = new JLabel();

            Font font = statusBar.getFont().deriveFont(12.0f);

            statusBar.add(seed);
            statusBar.add(oldestNed);
            statusBar.add(newBorns);
            statusBar.add(deaths);
            statusBar.add(population);
            statusBar.add(day);
            statusBar.setVisible(true);

            newBorns.setFont(font);
            oldestNed.setFont(font);
            deaths.setFont(font);
            population.setFont(font);
            day.setFont(font);
            seed.setFont(font);
        }
        return statusBar;
    }

    /**
     * This method initializes jJMenuBar
     *
     * @return javax.swing.JMenuBar
     */
    private JMenuBar getJJMenuBar() {
        if (jJMenuBar == null) {
            jJMenuBar = new JMenuBar();
            jJMenuBar.add(getJMenu());
            jJMenuBar.add(getJMenu2());
        }
        return jJMenuBar;
    }

    /**
     * This method initializes jMenu
     *
     * @return javax.swing.JMenu
     */
    private JMenu getJMenu() {
        if (jMenu == null) {
            jMenu = new JMenu();
            jMenu.setText("File");
            jMenu.add(getJMenuItem1());
        }
        return jMenu;
    }

    /**
     * This method initializes jMenuItem1
     *
     * @return javax.swing.JMenuItem
     */
    private JMenuItem getJMenuItem1() {
        if (jMenuItem1 == null) {
            jMenuItem1 = new JMenuItem();
            jMenuItem1.setText("Close");
            jMenuItem1.addActionListener((java.awt.event.ActionEvent e) -> {
                log.debug("actionPerformed()"); // TODO
                // Auto-generated
                // Event
                // stub actionPerformed()
                stopSimulation();
                System.exit(0);
            });
        }
        return jMenuItem1;
    }

    private JMenu getJMenu2() {
        if (jMenu2 == null) {
            jMenu2 = new JMenu();
            jMenu2.setText("Simulation");
            jMenu2.add(getJMenuItem3());
            jMenu2.add(getJMenuItem4());
            jMenu2.add(getJMenuItem5());
        }
        return jMenu2;
    }

    private JMenuItem getJMenuItem3() {
        if (jMenuItem3 == null) {
            jMenuItem3 = new JMenuItem();
            jMenuItem3.setText("Start Simulation");
            jMenuItem3.addActionListener((java.awt.event.ActionEvent e) -> {
                runSimulation();
            });
        }
        return jMenuItem3;
    }

    private JMenuItem getJMenuItem4() {
        if (jMenuItem4 == null) {
            jMenuItem4 = new JMenuItem();
            jMenuItem4.setText("Stop Simulation");
            jMenuItem4.addActionListener((java.awt.event.ActionEvent e) -> {
                stopSimulation();
            });
        }
        return jMenuItem4;
    }

    private JMenuItem getJMenuItem5() {
        if (jMenuItem5 == null) {
            jMenuItem5 = new JMenuItem();
            jMenuItem5.setText("Reset Simulation");
            jMenuItem5.addActionListener((java.awt.event.ActionEvent e) -> {
                resetSimulation();
            });
        }
        return jMenuItem5;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NedWorld nedWorld = new NedWorld("NedWorld 1.0", new Dimensions(20, 20));
            nedWorld.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            nedWorld.setVisible(true);
        });
    }

    private JPanel getNedField() {
        return nedField;
    }

    /**
     *
     */
    public void runSimulation() {
        log.debug("NedWorld: runSimulation().");
        nedsThread = new Thread(neds);
        nedsThread.start();
    }

    /**
     *
     */
    public void stopSimulation() {
        if (nedsThread != null) {
            nedsThread.interrupt();
        }
    }

    /**
     *
     */
    public void resetSimulation() {
        stopSimulation();
        Rng.getInstance().resetCurrentSeed();
        neds = newNeds();
    }

    /**
     *
     */
    public void newSimulation() {
        stopSimulation();
        Rng.getInstance().setNewRandomSeed();
        neds = newNeds();
    }

    /**
     *
     * @return
     */
    private Neds newNeds() {
        currStep = 0;
        neds = new Neds(DEFAULT_DIMENSIONS, DEFAULT_POPULATION, DEFAULT_FOOD_RANGE, DEFAULT_DELAY);
        if (jContentPane != null && nedField != null) {
            jContentPane.remove(nedField);
        }
        nedField = new NedField(neds);
        if (jContentPane != null) {
            jContentPane.add(nedField, BorderLayout.CENTER);
        }
        neds.addNedsEventListener(this);
        int sliderDelay = 1000;
        if (speedSlider != null) {
            sliderDelay = speedSlider.getValue();
        }
        neds.setDelay(sliderDelay);
        return neds;
    }

    public boolean isListening() {
        return true;
    }

    private void updateStats() {
        if (neds != null) {
            this.newBorns.setText("Births: " + neds.getBirths());
            Ned ned = neds.getOldestNed();
            if (ned != null) {
                this.oldestNed.setText("Oldest Ned: age: " + ned.getAge() + ", id: " + ned.getDna());
            }
            this.deaths.setText("Deaths: " + neds.getDeaths());
            this.population.setText("Population: " + neds.getPopulation());
            this.day.setText("Day: " + currStep);
            this.seed.setText("Seed: " + Rng.getInstance().getSeed());
            currStep++;
        }
    }

    @Override
    public void handle(NedsEvent event) {
        updateStats();
        if (event.getEvent().equals("stopped")) {
            log.debug("Postmortem:");
            Set<Ned> s = neds.getAllNeds();
            log.debug("number of neds: " + s.size());
            int alive = 0;
            int dead = 0;
            for (Ned ned : s) {
                if (ned.isAlive()) {
                    alive++;
                }
                else {
                    dead++;
                }
            }
            log.debug("alive: " + alive + ", dead: " + dead);
            JButton b = buttons.get("Pause");
            b.setEnabled(false);
            b = buttons.get("Play");
            b.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        switch (e.getActionCommand()) {
            case "Play": {
                JButton button = (JButton) e.getSource();
                button.setDisabledIcon(playing);
                button.setEnabled(false);
                JButton b = buttons.get("Pause");
                b.setEnabled(true);
                runSimulation();
                break;
            }
            case "Pause": {
                JButton button = (JButton) e.getSource();
                button.setEnabled(false);
                JButton b = buttons.get("Play");
                b.setIcon(play);
                b.setEnabled(true);
                stopSimulation();
                break;
            }
            case "Reset":
                resetSimulation();
                break;
            case "New":
                newSimulation();
                break;
        }
        updateStats();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider js = (JSlider) e.getSource();
        if (!js.getValueIsAdjusting()) {
            int sliderDelay = (int) js.getValue();
            neds.setDelay(sliderDelay);
        }
    }
}

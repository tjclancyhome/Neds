/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tjc.neds.simulation.util;

import java.net.URL;

/**
 *
 * @author Thomas
 */
public class Resources {

    /**
     *
     * @param resName
     * @return
     */
    public static URL getResource(String resName) {
        return ClassLoader.getSystemResource(resName);
    }

    private Resources() {
    }
}

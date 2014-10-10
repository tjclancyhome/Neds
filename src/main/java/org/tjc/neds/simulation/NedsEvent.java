package org.tjc.neds.simulation;

import java.util.EventObject;

public class NedsEvent extends EventObject {
    private static final long serialVersionUID = -5312961037595588543L;
    public NedsEvent(String event) {
        super(event);
    }

    public String getEvent() {
        return (String)getSource();
    }
}

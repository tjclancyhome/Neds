package org.tjc.neds.simulation;

public enum Direction {
    North(0), 
    South(1), 
    East(2), 
    West(3),
    NorthEast(4),
    NorthWest(5),
    SouthEast(6),
    SouthWest(7);

    int dir;

    public int value() {
        return dir;
    }

    Direction(int dir) {
        this.dir = dir;
    }
}

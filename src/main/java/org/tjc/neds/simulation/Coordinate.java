package org.tjc.neds.simulation;

public class Coordinate {

    int x;
    int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object other) {
        if (other != null) {
            if (other instanceof Coordinate) {
                Coordinate o = (Coordinate) other;
                return x == o.getX() && y == o.getY();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public int hashCode() {
        return (x * 100) + y;
    }

    public static Coordinate newRandomCoordinate(Dimensions constraint) {
        int x = Rng.getInstance().nextInt(constraint.getWidth());
        int y = Rng.getInstance().nextInt(constraint.getHeight());
        return new Coordinate(x, y);
    }

    public static Coordinate newRandomCoordinate(Coordinate upperLeft, Dimensions constraint) {
        int x = Rng.getInstance().nextInt(constraint.getWidth());
        int y = Rng.getInstance().nextInt(constraint.getHeight());
        x += upperLeft.getX();
        y += upperLeft.getY();
        return new Coordinate(x, y);
    }

}

package org.tjc.neds.simulation;

public class Dimensions {
    private int width;
    private int height;

    public Dimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void updateCoordinates(Coordinate coord, boolean wrap) {
        int x = coord.getX();
        int y = coord.getY();

        if (wrap) {
            if (x < 0) {
                x = width - 1;
            } else if (x > width - 1) {
                x = 0;
            }

            if (y < 0) {
                y = height - 1;
            } else if (y > height - 1) {
                y = 0;
            }

        } else {
            if (x < 0) {
                x = 0;
            } else if (x > width - 1) {
                x = width - 1;
            }

            if (y < 0) {
                y = 0;
            } else if (y > height - 1) {
                y = height - 1;
            }
        }

        coord.setX(x);
        coord.setY(y);

    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    @Override
    public String toString() {
        return "Dimensions[" + width + ", " + height + "]";
    }
}

package org.tjc.neds.simulation;

import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tjc.neds.Asserts;

/**
 *
 * @author Thomas
 */
public class Patch {

    private static final Logger log = LoggerFactory.getLogger(Patch.class.getName());
    private final ReentrantLock lock = new ReentrantLock();
    private int food;
    private final Coordinate location;
    private final Range<Integer> foodRange;
    private int foodGrowthRate;

    public Patch(Range<Integer> foodRange, Neds neds, Coordinate c) {
        Asserts.assertNotNull(neds, "neds is null");
        this.location = c;
        this.foodRange = foodRange;
        init();
    }

    public void replant(int step) {
        food += foodGrowthRate;
        if (food > getMaxFood()) {
            food = getMaxFood();
        }
    }

    public Coordinate getLocation() {
        return location;
    }

    public int getFood() {
        return food;
    }

    public double getPercentRemainingFood() {
        double rf = (food / (double) getMaxFood()) * 100.00;
        log.debug(this.toString());
        return rf;
    }

    private void init() {
        int min = foodRange.getLow();
        int max = foodRange.getHigh();
        food = Rng.getInstance().nextInt(max - min) + min;
        foodGrowthRate = (int) (getMaxFood() * ((Rng.getInstance().nextInt(10) + 1) / 100.00));
    }

    public int pick(int n) {
        int picked = 0;
        if (food > 0) {
            if (food >= n) {
                food -= n;
                picked = n;
            }
            else {
                food = 0;
                picked = n;
            }
        }
        return picked;
    }

    public int getMaxFood() {
        return foodRange.getHigh();
    }

    public int getMinFood() {
        return foodRange.getLow();
    }

    @Override
    public String toString() {
        return "patch[" +
            "location: " + location +
            ", food: " + food +
            ", growth rate: " + foodGrowthRate +
            ", food range: " + foodRange + "]";
    }
}

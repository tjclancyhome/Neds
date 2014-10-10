package org.tjc.neds.simulation;

import java.util.Random;

/**
 *
 * @author Thomas
 */
public class Rng {

    private static final Rng instance = new Rng();
    private long seed;
    private Random rng;

    public static Rng getInstance() {
        return instance;
    }

    public void resetCurrentSeed() {
        rng = new Random(seed);
    }

    public void setNewRandomSeed() {
        Random r = new Random();
        seed = r.nextLong();
        rng = new Random(seed);
    }

    public Long getSeed() {
        return seed;
    }

    public Long nextLong() {
        return rng.nextLong();
    }

    public Integer nextInt() {
        return rng.nextInt();
    }

    public Integer nextInt(int n) {
        return rng.nextInt(n);
    }

    public Boolean nextBoolean() {
        return rng.nextBoolean();
    }

    public double nextDouble() {
        return rng.nextDouble();
    }

    private Rng() {
        Random r = new Random();
        seed = r.nextLong();
        rng = new Random(seed);
    }

    private Rng(long seed) {
        this.seed = seed;
        rng = new Random(this.seed);
    }
}

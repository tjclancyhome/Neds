package org.tjc.neds.simulation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tjc.neds.Asserts;

/**
 * @author thomascl
 *
 */
public class Neds implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Neds.class);
    private final ReentrantLock lock = new ReentrantLock();

    private final Map<Coordinate, Set<Ned>> nedMap;
    private Patch[][] field;
    private Ned oldestNed;
    private final Dimensions dimension;
    private final List<NedsEventListener> eventListeners;
    private long delay;
    private final AtomicInteger atomicPopulation;
    private final AtomicInteger births;
    private final AtomicInteger deaths;
    private final Range<Integer> foodRange;
    private int step;

    public Neds(Dimensions d, int population, Range<Integer> foodRange, long delay) {
        this.atomicPopulation = new AtomicInteger(population);
        this.births = new AtomicInteger();
        this.deaths = new AtomicInteger();
        this.nedMap = new HashMap<>();
        this.dimension = d;
        this.delay = delay;
        this.eventListeners = new LinkedList<>();
        this.foodRange = foodRange;
        populateField();
    }

    @Override
    public void run() {
        fireEvent("running");
        try {
            while (atomicPopulation.get() > 0) {
                step();
                fireEvent("step");
                Thread.sleep(getDelay());
            }
        }
        catch (InterruptedException e) {
        }
        fireEvent("stopped");
    }

    private void fireEvent(String event) {
        eventListeners.stream().
            forEach((listener) -> {
                listener.handle(new NedsEvent(event));
            });
    }

    public Ned getOldestNed() {
        return oldestNed;
    }

    public int getBirths() {
        return births.get();
    }

    public int getDeaths() {
        return deaths.get();
    }

    public void step() {
        lock.lock();
        try {
            step++;
            if (atomicPopulation.get() > 0) {
                Set<Ned> deadNeds = null;
                Map<Ned, Coordinate> movedNeds = null;
                LinkedList<Ned> males = null;
                LinkedList<Ned> females = null;
                Set<Coordinate> keys = nedMap.keySet();
                for (Coordinate coord : keys) {
                    Set<Ned> neds = nedMap.get(coord);
                    for (Ned ned : neds) {
                        if (ned.isAlive()) {
                            if (oldestNed == null) {
                                oldestNed = ned;
                            }
                            else {
                                oldestNed = max(oldestNed, ned);
                            }
                            int instr = ned.next();
                            switch (instr) {
                                case 0:
                                    ned.turnLeft();
                                    break;
                                case 1:
                                    if (movedNeds == null) {
                                        movedNeds = new HashMap<>();
                                    }
                                    ned.setPoint(null);
                                    Coordinate old = new Coordinate(ned.getCoord().getX(), ned.
                                        getCoord().getY());
                                    movedNeds.put(ned, old);
                                    break;
                                case 2:
                                    if (ned.isFemale()) {
                                        if (females == null) {
                                            females = new LinkedList<>();
                                        }
                                        females.add(ned);
                                    }
                                    else {
                                        if (males == null) {
                                            males = new LinkedList<>();
                                        }
                                        males.add(ned);
                                    }
                                    break;
                            }
                            ned.setAge(ned.getAge() + 1);
                            Patch p = getPatch(ned.getCoord());
                            if (!ned.eatFrom(p)) {
                                if (deadNeds == null) {
                                    deadNeds = new HashSet<>();
                                }
                                deadNeds.add(ned.kill());
                            }
                        }
                    }
                }

                if (deadNeds != null) {
                    cleanTheDead(deadNeds);
                }

                if (movedNeds != null) {
                    Set<Ned> keySet = movedNeds.keySet();
                    for (Ned ned : keySet) {
                        if (ned.isAlive()) {
                            Coordinate previous = movedNeds.get(ned);
                            moveNed(ned, previous);
                        }
                    }
                }

                if (females != null && males != null) {
                    mateNeds(females, males);
                }
                replantPatches();
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void mateNeds(LinkedList<Ned> females, LinkedList<Ned> males) {
        List<Ned> dead = null;
        lock.lock();
        try {
            for (Ned male : males) {
                if (male.isAlive()) {
                    if (!females.isEmpty()) {
                        Ned female = females.remove();
                        while (!female.isAlive() && !females.isEmpty()) {
                            female = females.remove();
                        }
                        if (female.isAlive()) {
                            Ned offspring = male.mate(getPatch(male), female);
                            if (offspring != null) {
                                births.incrementAndGet();
                                add(offspring.getCoord(), offspring);
                            }
                            if (!male.isAlive()) {
                                if (dead == null) {
                                    dead = new LinkedList<>();
                                }
                                dead.add(male);
                            }
                            if (!female.isAlive()) {
                                if (dead == null) {
                                    dead = new LinkedList<>();
                                }
                                dead.add(female);
                            }
                        }
                        else {
                            break;
                        }
                    }
                    else {
                        break;
                    }
                }
            }
        }
        finally {
            lock.unlock();
        }

        if (dead != null) {
            this.cleanTheDead(dead);
        }
    }

    private Ned max(Ned n1, Ned n2) {
        if (n1.getAge() > n2.getAge()) {
            return n1;
        }
        else {
            return n2;
        }
    }

    public void addNedsEventListener(NedsEventListener nel) {
        eventListeners.add(nel);
    }

    public int getPopulation() {
        return atomicPopulation.get();
    }

    private void add(Coordinate c, Ned ned) {
        if (ned != null && ned.isAlive()) {
            lock.lock();
            try {
                Set<Ned> s = nedMap.get(c);
                Asserts.assertTrue(!s.contains(ned), "" + ned + " already at " + c);
                ned.setCoord(c);
                s.add(ned);
                atomicPopulation.getAndIncrement();
            }
            finally {
                lock.unlock();
            }
        }
    }

    private void populateField() {
        lock.lock();
        try {
            int w = dimension.getWidth();
            int h = dimension.getHeight();
            field = new Patch[h][w];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    field[x][y] = new Patch(foodRange, this, new Coordinate(x, y));
                    Set<Ned> hs = new HashSet<>();
                    nedMap.put(new Coordinate(x, y), hs);
                }
            }
            for (int i = 0; i < atomicPopulation.get(); i++) {
                Ned ned = new Ned();
                Coordinate c = new Coordinate(Rng.getInstance().nextInt(w), Rng.getInstance().
                    nextInt(h));
                ned.setCoord(c);
                nedMap.get(c).add(ned);
            }
        }
        finally {
            lock.unlock();
        }
    }

    public Coordinate move(Ned ned, Dimensions d) {
        int x = ned.getX();
        int y = ned.getY();
        switch (ned.getFacing()) {
            case North:
                y--;
                break;
            case NorthEast:
                y--;
                x++;
                break;
            case NorthWest:
                y--;
                x--;
                break;
            case South:
                y++;
                break;
            case SouthEast:
                y++;
                x++;
                break;
            case SouthWest:
                y++;
                x--;
                break;
            case East:
                x++;
                break;
            case West:
                x--;
                break;
        }
        Coordinate newCoord = new Coordinate(x, y);
        d.updateCoordinates(newCoord, false);
        return newCoord;
    }

    public void moveNed(Ned ned, Coordinate previous) {
        lock.lock();
        try {
            Asserts.assertTrue(ned.isAlive(), " can't move a dead ned: " + ned);
            Coordinate newCoord = move(ned, dimension);
            if (!newCoord.equals(previous)) {
                Set<Ned> o = nedMap.get(previous);
                Asserts.assertTrue(o.contains(ned), "" + ned + " not at " + previous);

                Set<Ned> n = nedMap.get(newCoord);
                Asserts.assertTrue(!n.contains(ned), "" + ned + " already at " + newCoord);

                o.remove(ned);
                ned.setCoord(newCoord);
                n.add(ned);

                Asserts.assertTrue(n.contains(ned), "" + ned + " not in new patch");
                Asserts.assertTrue(!o.contains(ned), "" + ned + " still in old patch");
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void replantPatches() {
        int w = dimension.getWidth();
        int h = dimension.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                field[x][y].replant(step);
            }
        }
    }

    public int getMaleCount(Coordinate loc) {
        return getNedCount(loc, 0);
    }

    public int getFemaleCount(Coordinate loc) {
        return getNedCount(loc, 1);
    }

    public int getNedCount(Coordinate loc) {
        Set<Ned> nedSet = getNeds(loc);
        if (nedSet != null) {
            return nedSet.size();
        }
        return 0;
    }

    private int getNedCount(Coordinate loc, int type) {
        int count = 0;
        lock.lock();
        try {
            Set<Ned> nedSet = getNeds(loc);
            if (nedSet != null) {
                for (Ned ned : nedSet) {
                    if (type == 0) {
                        if (ned.isMale()) {
                            count++;
                        }
                    }
                    else if (type == 1) {
                        if (ned.isFemale()) {
                            count++;
                        }
                    }
                    else {
                        count++;
                    }
                }
            }
        }
        finally {
            lock.unlock();
        }
        return count;
    }

    public Set<Ned> getNeds(Coordinate c) {
        lock.lock();
        try {
            return nedMap.get(c);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     *
     * @return
     */
    public Set<Ned> getAllNeds() {
        lock.lock();
        try {
            Set<Ned> allNeds = new HashSet<>();
            Set<Coordinate> coordinates = nedMap.keySet();
            coordinates.stream().
                forEach((c) -> {
                    allNeds.addAll(nedMap.get(c));
                });
            return Collections.unmodifiableSet(allNeds);
        }
        finally {
            lock.unlock();
        }
    }

    public static long newSeed() {
        Random r = new Random();
        return r.nextLong();
    }

    public Dimensions getDimension() {
        return dimension;
    }

    public Patch[][] getFields() {
        return field;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    private void cleanTheDead(Collection<Ned> deadNeds) {
        lock.lock();
        try {
            deadNeds.stream().
                map((ned) -> {
                    Set<Ned> s = nedMap.get(ned.getCoord());
                    Asserts.assertTrue(s.contains(ned), "can't delete " + ned + " from patch: " +
                        ned.
                        getCoord());
                    s.remove(ned);
                    Asserts.assertTrue(!s.contains(ned), "" + ned + " not removed from patch: " +
                        ned.
                        getCoord());
                    return ned;
                }).
                map((_item) -> {
                    atomicPopulation.getAndDecrement();
                    return _item;
                }).
                forEach((_item) -> {
                    deaths.getAndIncrement();
                });
        }
        finally {
            lock.unlock();
        }
    }

    /**
     *
     */
    public void resetNedPoint() {
        lock.lock();
        try {
            Set<Ned> neds = getAllNeds();
            neds.stream().
                forEach((ned) -> {
                    ned.setPoint(null);
                });
        }
        finally {
            lock.unlock();
        }
    }

    public Patch getPatch(Ned ned) {
        return getPatch(ned.getCoord());
    }

    public Patch getPatch(Coordinate c) {
        return field[c.getX()][c.getY()];
    }

    private void dumpMap() {
        log.debug("---------- Dump ----------");
        Set<Coordinate> s = nedMap.keySet();
        s.stream().
            map((c) -> {
                Set<Ned> ns = nedMap.get(c);
                ns.stream().
                forEach((ned) -> {
                    log.debug("\tcoord: {}, {}", c, ned);
                });
                return c;
            }).
            forEach((_item) -> {
                System.out.println();
            });
    }

}

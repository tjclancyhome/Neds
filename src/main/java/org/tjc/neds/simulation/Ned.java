package org.tjc.neds.simulation;

import java.awt.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ned {

    private static final Logger log = LoggerFactory.getLogger(Ned.class.getName());
    private static final int FoodSackCapacity = 3;
    private long dna;
    private int ip = 0;
    private long curr;
    private boolean alive = true;
    private int instruction = 0;
    private char sex;
    private Direction facing;
    private int age = 0;
    private Coordinate coord;
    private Point point;
    private int foodSack;

    public Ned() {
        init();
    }

    public int next() {
        if (ip == 19) {
            ip = 1;
            curr = dna;
        }
        else {
            ip++;
        }
        instruction = (int) curr & 3;
        curr >>= 2;
        return instruction;
    }

    public void turnLeft() {
        turn(0);
    }

    public boolean isMale() {
        return sex == 'm';
    }

    public boolean isFemale() {
        return !isMale();
    }

    public void setSexToMale() {
        sex = 'm';
    }

    public void setSexToFemale() {
        sex = 'f';
    }

    public void turnRight() {
        turn(1);
    }

    public int getInstruction() {
        return instruction;
    }

    public boolean at(int x, int y) {
        return coord.getX() == x && coord.getY() == y;
    }

    public boolean at(Coordinate c) {
        return this.coord.equals(c);
    }

    public void setPoint(Point p) {
        this.point = p;
    }

    public Point getPoint() {
        return point;
    }

    private void turn(int dir) {
        switch (facing) {
            case North:
                if (dir == 0) {
                    facing = Direction.NorthWest;
                }
                else {
                    facing = Direction.NorthEast;
                }
                break;
            case NorthEast:
                if (dir == 0) {
                    facing = Direction.North;
                }
                else {
                    facing = Direction.East;
                }
                break;
            case NorthWest:
                if (dir == 0) {
                    facing = Direction.West;
                }
                else {
                    facing = Direction.North;
                }
                break;
            case South:
                if (dir == 0) {
                    facing = Direction.SouthEast;
                }
                else {
                    facing = Direction.SouthWest;
                }
                break;
            case SouthEast:
                if (dir == 0) {
                    facing = Direction.East;
                }
                else {
                    facing = Direction.South;
                }
                break;
            case SouthWest:
                if (dir == 0) {
                    facing = Direction.South;
                }
                else {
                    facing = Direction.West;
                }
                break;
            case East:
                if (dir == 0) {
                    facing = Direction.NorthEast;
                }
                else {
                    facing = Direction.SouthEast;
                }
                break;
            case West:
                if (dir == 0) {
                    facing = Direction.SouthWest;
                }
                else {
                    facing = Direction.NorthWest;
                }
                break;
        }
    }

    public boolean eatFrom(Patch p) {
        int required = (age / 80) + 1;

        if (foodSack >= required) {
            foodSack -= required;
            return true;
        }
        else {
            int needThisMuch = required - foodSack;
            if (needThisMuch + foodSack <= FoodSackCapacity) {
                if (p.getFood() >= needThisMuch) {
                    foodSack += p.pick(needThisMuch);
                    foodSack -= required;
                    if (p.getFood() >= FoodSackCapacity) {
                        int pick = FoodSackCapacity - foodSack;
                        if (pick > 0) {
                            foodSack += p.pick(pick);
                        }
                        return true;
                    }
                    else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean eat(int n) {
        if (foodSack >= n) {
            foodSack -= n;
            return true;
        }
        return false;

    }

    public long getDna() {
        return dna;
    }

    public int getIp() {
        return ip;
    }

    public Ned mate(Patch p, Ned mate) {
        if (isAlive() && mate.isAlive() && (mate.getSex() != this.getSex()) && oldEnoughToMate(mate)) {
            return mateWith(p, mate);
        }
        return null;
    }

    private boolean oldEnoughToMate(Ned mate) {
        return true;
        //return this.age > 5 && mate.getAge() > 5;
    }

    private Ned mateWith(Patch p, Ned mate) {
        Ned offspring;
        long newDna;
        long myDna = mate.getDna();
        long mateDna = mate.getDna();

        int splicePoint = Rng.getInstance().nextInt(39) + 1;

        long s1 = 0;
        for (int i = 0; i < splicePoint; i++) {
            s1 += Math.pow(2.0, i);
        }

        long s2 = 0;
        for (int i = splicePoint; i < 40; i++) {
            s2 += Math.pow(2.0, i);
        }

        newDna = (myDna & s1) + (mateDna & s2);

        offspring = new Ned();
        offspring.setDna(newDna);
        offspring.setCoord(this.getCoord());
        offspring.setAge(0);

        eatFrom(p);
        mate.eatFrom(p);

        return offspring;
    }

    public boolean isAlive() {
        return alive;
    }

    public char getSex() {
        return sex;
    }

    public Direction getFacing() {
        return facing;
    }

    public int getAge() {
        return age;
    }

    public Ned kill() {
        alive = false;
        return this;
    }

    @Override
    public int hashCode() {
        return (int) dna;
    }

    @Override
    public String toString() {
        return "ned: " + dna + "  sex: " + sex + "  coord: " + coord + "  facing: " + facing.
            toString() + "  age: " + age + "  alive: " + alive + "  last instr: " + instruction;
    }

    public void placeRandomly(Dimensions d) {
        coord = new Coordinate(Rng.getInstance().nextInt(d.getWidth()), Rng.getInstance().nextInt(d.
            getHeight()));
    }

    private void init() {
        dna = Rng.getInstance().nextLong();
        curr = dna;
        sex = Rng.getInstance().nextBoolean() ? 'm' : 'f';
        coord = new Coordinate(0, 0);
        facing = Direction.values()[Rng.getInstance().nextInt(8)];
    }

    public Coordinate getCoord() {
        return coord;
    }

    public int getX() {
        return coord.x;
    }

    public int getY() {
        return coord.getY();
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setDna(long dna) {
        this.dna = dna;
    }
}

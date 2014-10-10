package org.tjc.neds;

public class Asserts {

    /**
     *
     * @param e
     * @param msg
     */
    public static void assertTrue(boolean e, String msg) {
        if (!e) {
            throw new AssertionError(msg);
        }
    }

    /**
     *
     * @param e
     * @param msg
     */
    public static void assertNotTrue(boolean e, String msg) {
        if (e) {
            throw new AssertionError(msg);
        }
    }

    /**
     *
     * @param o
     * @param msg
     */
    public static void assertNotNull(Object o, String msg) {
        if (o == null) {
            throw new AssertionError(msg);
        }
    }

    private Asserts() {

    }

}

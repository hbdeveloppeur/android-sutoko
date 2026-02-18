package fr.purpletear.friendzone4.custom;

public class Timer {
    /**
     * Start of the Timer
     */
    long start;

    /**
     * End of the timer
     */
    long end;

    /**
     * Sets start value with the current time in ms
     */
    public void start() {
        start = System.currentTimeMillis();
    }

    /**
     * Sets end value with the current time in ms
     */
    public void end() {
        end = System.currentTimeMillis();
    }

    /**
     * Return the time between start and end.
     */
    public long result() {
        return end - start;
    }

    /**
     * Returns a String explaining how long it took for the method to reach completion.
     *
     * @param name the calling method name
     * @return String
     */
    public String toString(String name) {
        return "Timer for " + name + " took " + result() + " ms.";
    }
}

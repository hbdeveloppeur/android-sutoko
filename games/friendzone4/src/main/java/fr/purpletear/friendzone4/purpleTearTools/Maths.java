package fr.purpletear.friendzone4.purpleTearTools;

public class Maths {

    /**
     * Returns the percent value of current compared to max
     * @param current double
     * @param max double
     * @return double
     */
    public static double percent(double current, double max) {
        if(0 == current) {
            return 0;
        } else if(0 == max) {
            return 0;
        }
        return current * 100 / max;
    }
}

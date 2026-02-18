package fr.purpletear.friendzone4.game.activities.hacking;

import fr.purpletear.friendzone4.factories.Std;

/**
 * A Code is used to display a line inside a fake shell.
 * It has a time (long)
 * and a text (String)
 */
class Code {
    /**
     * Contains the code to display
     */
    private String code;

    /**
     * Contains the time to makes it appear
     */
    private long time;

    Code(String line) {
        readFromLine(line);
    }

    /**
     * Converts a line into a code
     * Examples lines in assets/code.txt
     *
     * @param line String
     */
    void readFromLine(String line) {
        String timeCode = Std.find(line, "\\[([0-9]{4})\\]([ ]*)");
        String formatedTimeCode = timeCode.replace(" ", "").replace("[", "").replace("]", "");
        time = Long.parseLong(formatedTimeCode);
        code = line.replace(timeCode, "");
    }

    final String getCode(int position) {
        code = code.replace("%d", String.valueOf(position));
        return code;
    }

    public final long getTime() {
        return time;
    }
}

package fr.purpletear.friendzone4.custom;

public class Chapter {

    /**
     * Determines the alpha of the chapter's alternative from the chapter code,
     * Example : return a from 6a
     * @param code the chapter code
     * @return the alpha of the chapter's alternative
     */
    public static String alphaAlternative(final String code){
        if(code == null || code.length() < 2)
            return "a";
        return code.substring(code.length() - 1, code.length()).toLowerCase();
    }

    /**
     * Determines the n° of the chapter from the chapter code,
     * Example : return 6 from 6a
     * @param str the chapter code
     * @return the number of the chapter
     */
    public static int nbChapter(final String str){
        if(str.length() < 2)
            return 1;
        return Integer.parseInt(str.substring(0, str.length() - 1));
    }
}

package fr.purpletear.friendzone4.game.activities.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by purpletear on 06/03/2018.
 */

public class Emojis {
    public static String Translate(String name) {
        switch (name) {
            case "[tilt]" :
                return new String(java.lang.Character.toChars(0x1F60F));
            case "xD" :
                return new String(java.lang.Character.toChars(0x1F602));
            case "x'D" :
                return new String(java.lang.Character.toChars(0x1F602));
            case "^^'" :
                return new String(java.lang.Character.toChars(0x1F605));
            case ":)":
                return new String(java.lang.Character.toChars(0x1F60A));
            case ":D":
                return new String(java.lang.Character.toChars(0x1F604));
            case "*.*":
                return new String(java.lang.Character.toChars(0x1F60D));
            case "|_|":
                return new String(java.lang.Character.toChars(0x1F64C));
            case "[king]":
                return new String(java.lang.Character.toChars(0x1F451));
            case "[celebration]":
                return new String(java.lang.Character.toChars(0x1F389));
            case "[MELODY]":
                return new String(java.lang.Character.toChars(0x1F3B6));
            default: return "";
        }
    }

    static String translate(String sentence) {
        List<EmojiTranslated> list = new ArrayList<>();
        list.add(new EmojiTranslated("xD", new String(java.lang.Character.toChars(0x1F602))));
        list.add(new EmojiTranslated("x'D", new String(java.lang.Character.toChars(0x1F602))));
        list.add(new EmojiTranslated("|o|", new String(java.lang.Character.toChars(0x1F631))));
        list.add(new EmojiTranslated("[tilt]", new String(java.lang.Character.toChars(0x1F60F))));
        list.add(new EmojiTranslated("[MELODY]", new String(java.lang.Character.toChars(0x1F3B6))));
        String s = sentence;
        for (EmojiTranslated e : list) {
            s = s.replace(e.str, e.translation);
        }
        return s;
    }

    private static class EmojiTranslated {
        private String str;
        private String translation;

        private EmojiTranslated(String str, String translation) {
            this.str = str;
            this.translation = translation;
        }
    }
}



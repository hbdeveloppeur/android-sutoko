package friendzone3.purpletear.fr.friendzon3.custom;

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
            default: return "";
        }
    }
}

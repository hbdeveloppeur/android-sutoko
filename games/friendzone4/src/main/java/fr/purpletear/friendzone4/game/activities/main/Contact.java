package fr.purpletear.friendzone4.game.activities.main;

import android.content.Context;

import fr.purpletear.friendzone4.GameData;

public class Contact {
    private String code;
    private String drawable;
    private String name;
    private String text;
    private Type type;
    private boolean blurred;
    public enum  Type {
        SIMPLE,
        ACTION
    }

    public Contact(Context context, String code, String drawable, String name, String text, Type type, boolean blurred) {
        this.code = code;
        this.drawable = drawable;
        this.name = GameData.INSTANCE.updateNames(context, name);
        this.text = GameData.INSTANCE.updateNames(context, text);
        this.type = type;
        this.blurred = blurred;
    }

    public Contact(Context context, String drawable, String name, String text, Type type, boolean blurred) {
        this.code = "";
        this.drawable = drawable;
        this.name = GameData.INSTANCE.updateNames(context, name);
        this.text = GameData.INSTANCE.updateNames(context, text);
        this.type = type;
        this.blurred = blurred;
    }

    public String getCode() {
        return code;
    }

    public String getDrawable() {
        return drawable;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public boolean isBlurred() {
        return blurred;
    }
}

package friendzone3.purpletear.fr.friendzon3.config;

public class Var {
    private String name;
    private String value;
    private int chapterNumber;

    public Var(String name, String value, int chapterNumber) {
        this.name = name;
        this.value = value;
        this.chapterNumber = chapterNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof Var))
            return false;
        final Var v = (Var) obj;
        return v.getName().equals(getName()) && v.getValue().equals(getValue());
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    @Override
    public String toString() {
        return "Var :("+name+";"+value+")";
    }
}
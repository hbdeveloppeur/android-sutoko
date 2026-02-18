package fr.purpletear.friendzone4.purpleTearTools;


import androidx.annotation.Nullable;

public abstract class Runnable2 implements Runnable {
    private String name;
    private long duration;

    public Runnable2(String name,long duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Runnable2)) {
            return false;
        }
        Runnable2 other = (Runnable2) o;
        return other.getDuration() == getDuration() && other.getName().equals(getName());

    }
}

/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package purpletear.fr.purpleteartools;
import androidx.annotation.Nullable;

public abstract class Runnable2 implements Runnable {
    private String name;
    private long duration;

    public Runnable2(String name,int duration) {
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

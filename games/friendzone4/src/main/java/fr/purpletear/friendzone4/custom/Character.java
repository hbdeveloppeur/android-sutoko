package fr.purpletear.friendzone4.custom;

import fr.purpletear.friendzone4.R;

public class Character {

    /**
     * Contains the character's id
     */
    private int id;

    /**
     * Contains the id of the profilPicture
     */
    private String imageId;

    /**
     * Contains the small image id
     */
    private String smallImageId;

    /**
     * Contains the first name of the character
     */
    private String name;

    /**
     * Contains the color's id
     * int
     */
    private int colorId;


    public Character(int id, String imageId, String smallImageId, String name, int colorId) {
        this.id = id;
        this.imageId = imageId;
        this.smallImageId = smallImageId;
        this.name = name;
        this.colorId = colorId;
    }

    public final int getId() {
        return id;
    }

    public final String getImageId() {
        return imageId;
    }

    public final String getSmallImageId() {
        return smallImageId;
    }

    public final String getName() {
        return name;
    }

    public final int getColorId() {
        return colorId;
    }

    public final int getTypingAnim(){
        if (getColorId() == R.color.mainBackground) {
            return R.drawable.fz4_anim_istyping_gray;
        } else if (getColorId() == R.color.secondBackground) {
            return R.drawable.fz4_anim_istyping_whitefix;
        }
        return R.drawable.fz4_anim_istyping_gray;
    }

    public final int getTextColorId() {
        if (getColorId() == R.color.mainBackground) {
            return R.color.mainText;
        } else if (getColorId() == R.color.secondBackground) {
            return R.color.colorWhite;
        } else if (getColorId() == R.color.noSeenBackground) {
            return R.color.colorWhite;
        } else if (getColorId() == R.color.noSeenBackgroundDarker) {
            return R.color.colorWhite;
        } else if (getColorId() == R.color.noSeenMeBackground) {
            return R.color.mainText;
        }
        return R.color.mainText;
    }

    public static Character notFound(int colorId) {
        return new Character(-1, "fz4_not_found", "fz4_not_found_seen", "Personnage non trouvé", colorId);
    }

    @Override
    public String toString() {
        return "Character{" +
                "id=" + id +
                ", imageId=" + imageId +
                ", smallImageId=" + smallImageId +
                ", name='" + name + '\'' +
                '}';
    }
}

package fr.purpletear.friendzone4.game.tables;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.BuildConfig;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.custom.Character;

public class TableOfCharacters {
    /**
     * Contains the list of characters of the story
     */
    private List<Character> characters = new ArrayList<>();

    /**
     * Contains the chapter code
     */
    private String code;

    public void load(String chapterCode) {
        code = chapterCode;
        characters = getCharacters(chapterCode);
    }

    /**
     * Returns a list of Characters for the given chapterCode
     *
     * @param chapterCode String
     * @return List Characters
     */
    private List<Character> getCharacters(String chapterCode) {
        List<Character> characters = new ArrayList<>();
        switch (chapterCode.toLowerCase()) {
            case "1a":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.mainBackground));
                break;
            case "2a":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.secondBackground));
                characters.add(getCharacterByName(33, "Lucie Belle", R.color.mainBackground));
                break;
            case "3a":
                characters.add(getCharacterByName(34, "Eva Belle", R.color.mainBackground));
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.secondBackground));
                break;
            case "4a":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.noSeenBackground));
                characters.add(getCharacterByName(35, "Serveur", R.color.mainBackground));
                characters.add(getCharacterByName(36, "Serveur", R.color.mainBackground));
                break;
            case "4b":
                characters.add(getCharacterByName(34, "Eva Belle", R.color.mainBackground));
                break;
            case "5a":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.noSeenBackground));
                characters.add(getCharacterByName(38, "Yorokobi", R.color.mainBackground));
                break;
            case "6a":
                characters.add(getCharacterByName(33, "Lucie Belle", R.color.noSeenBackground));
                break;
            case "7a":
                characters.add(getCharacterByName(38, "Yorokobi", R.color.noSeenBackground));
                break;
            case "7b":
                characters.add(getCharacterByName(34, "Eva Belle", R.color.mainBackground));
                break;
            case "8a":
                characters.add(getCharacterByName(38, "Yorokobi", R.color.noSeenBackground));
                break;
            case "8b":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.noSeenBackground));
                break;
            case "9a":
                characters.add(getCharacterByName(38, "Yorokobi", R.color.noSeenBackground));
                characters.add(getCharacterByName(37, "Unknown", R.color.mainBackground));
                characters.add(getCharacterByName(34, "Eva Belle", R.color.mainBackground));
                break;
            case "9c":
                characters.add(getCharacterByName(28, "Zoé Topaze", R.color.noSeenBackground));
                characters.add(getCharacterByName(39, "Bryan", R.color.noSeenBackgroundDarker));
                characters.add(getCharacterByName(33, "Lucie Belle", R.color.mainBackground));
                characters.add(getCharacterByName(38, "Yorokobi", R.color.mainBackground));
                characters.add(getCharacterByName(5, "Chloé", R.color.mainBackground));
                break;
            case "10a":
                characters.add(getCharacterByName(40, "Emma", R.color.mainBackground));
                break;
            case "10b":
                characters.add(getCharacterByName(41, "Kim", R.color.mainBackground));
                break;
            case "10c":
                characters.add(getCharacterByName(28, "Zoé Candle", R.color.secondBackground));
                characters.add(getCharacterByName(34, "Eva Belle", R.color.mainBackground));
                break;
            default:
                throw new IllegalStateException("Unhandled code " + chapterCode);
        }
        return characters;
    }

    /**
     * Returns a Character given a Character's id
     *
     * @param characterId int
     * @return Character
     * @see Character
     */
    public Character getCharacter(int characterId) {
        for (Character character : characters) {
            if (character.getId() == characterId) {
                return character;
            }
        }
        if (BuildConfig.DEBUG) {
            throw new Resources.NotFoundException("Character not found for " + code + " id : " + characterId);
        }
        return Character.notFound(R.color.mainBackground);
    }

    /**
     * Returns a character given its name.
     *
     * @param id   int
     * @param name String
     * @return Character
      * @see Character
     */
    private Character getCharacterByName(int id, String name, int colorId) {
        switch (name) {
            case "Zoé Topaze":
                return new Character(id, "fz4_zoe", "fz4_zoe_seen"
                        , name, colorId);
            case "Zoé Candle":
                return new Character(id, "","fz4_zoe_candle_seen"
                        , name, colorId);
            case "Lucie Belle":
                return new Character(id, "fz4_lucie2", "fz4_lucie2_seen"
                        , name, colorId);
            case "Eva Belle":
                return new Character(id, "fz4_eva", "fz4_eva_seen"
                        , name, colorId);
            case "Serveur":
            case "Kurusheriwa":
            case "Unknown":
            case "Chloé":
                return new Character(id, "", ""
                        , name, colorId);
            case "Yorokobi":
                return new Character(id, "fz4_yorokobi", "fz4_yorokobi_seen"
                        , name, colorId);
            case "Bryan":
                return new Character(id, "", "fz4_bryan_seen"
                        , name, colorId);
            case "Emma":
                return new Character(id, "fz4_emma", "fz4_emma_seen"
                        , name, colorId);
            case "Kim":
                return new Character(id, "fz4_kim", "fz4_kim_seen"
                        , name, colorId);
        }
        if (BuildConfig.DEBUG) {
            throw new Resources.NotFoundException("Character not found for " + code);
        }
        return Character.notFound(colorId);
    }

    /**
     * Returns the drawable id for the seen icon
     * @return int id
     */
    public String getSeen() {
        switch (code) {
            case "2a":
            case "3a":
            case "10c":
                return "fz4_multi_seen";
            default:
                return getCharacters(code).get(0).getSmallImageId();
        }
    }
}

package friendzone3.purpletear.fr.friendzon3.custom;


import android.content.Context;

import com.example.sutokosharedelements.OnlineAssetsManager;

import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

@SuppressWarnings("DuplicateBranchesInSwitch")
public class Personnage {
    private String name;
    private String profilPicture;
    private String tickImage;
    private PersonnageStyle style;
    private PersonnageStyle.StyleColor styleColor;

    private Personnage(String name, String profilPicture, String tickImage, PersonnageStyle.StyleColor style) {
        this.name = name;
        this.profilPicture = profilPicture;
        this.tickImage = tickImage;
        this.style = PersonnageStyle.determine(style);
        this.styleColor = style;
    }

    public static Personnage who(String chapterCode, int id, TableOfSymbols symbols) {
        switch(chapterCode) {
            case "1a" :
                if(id == 16) {
                    return who("x92", PersonnageStyle.StyleColor.main, symbols);
                } else {
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }
            case "2a" :
                if(id == 11) {
                    return who("Eva Belle", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 15) {
                    return new Personnage("Me", "", "", PersonnageStyle.StyleColor.me);
                } else if(id == 10) {
                    return who("Zoé Topaze", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 12) {
                    return who("Lana Welzitenburg", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 14) {
                    return who("Marwin D.", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 13) {
                    return who("Rick P.", PersonnageStyle.StyleColor.main, symbols);
                } else{
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }
            case "3a" :
                if(id == 16) {
                    return who("x92", PersonnageStyle.StyleColor.main, symbols);
                } else{
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }

            case "4a" :
                if(id == 10) {
                    return who("Zoé Topaze", PersonnageStyle.StyleColor.main, symbols);
                } else  if(id == 11) {
                return who("Eva Belle", PersonnageStyle.StyleColor.main, symbols);
            } else{
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
            }
            case "5a":
                if(id == 9) {
                    return who("Lucie Belle", PersonnageStyle.StyleColor.main, symbols);
                } else {
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }
            case "6a":
                if(id == 11) {
                    return who("Eva Belle", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 10) {
                    return who("Zoé Topaze", PersonnageStyle.StyleColor.second, symbols);
                } else {
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }
            case "7a":
                if(id == 12) {
                    return who("Lana Welzitenburg", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 10) {
                    return who("Zoé Topaze", PersonnageStyle.StyleColor.main, symbols);
                } else {
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }

            case "7b":
                if(id == 9) {
                    return who("Lucie Belle", PersonnageStyle.StyleColor.main, symbols);
                } else{
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }

            case "8a":
                if(id == 10) {
                    return who("Zoé Topaze", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 12) {
                    return who("Lana Welzitenburg", PersonnageStyle.StyleColor.main, symbols);
                } else if(id==13) {
                    return who("Rick P.", PersonnageStyle.StyleColor.main, symbols);
                } else if(id==14) {
                    return who("Marwin D.", PersonnageStyle.StyleColor.main, symbols);
                } else {
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }

            case "8b":
                if(id == 9) {
                    return who("Lucie Belle", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 11) {
                    return who("Eva Belle", PersonnageStyle.StyleColor.main, symbols);
                } else if(id == 4) {
                    return who("Inconnu", PersonnageStyle.StyleColor.main, symbols);
                } else{
                    return new Personnage("System", "", "", PersonnageStyle.StyleColor.system);
                }

            default:
                throw new IllegalArgumentException("Chapter not handled in Personnage.who" + " " + chapterCode);
        }
    }

    private static Personnage who(String name, PersonnageStyle.StyleColor style, TableOfSymbols symbols) {
        switch (name) {
            case "x92" :
                return new Personnage(name, "friendzone3_no_avatar", "friendzone3_no_avatar_seen", style);

            case "Eva Belle" :
                return new Personnage(name, "friendzone3_eva", "friendzone3_eva_seen", style);

            case "Lucie Belle" :
                return new Personnage(name, "friendzone3_lucie_profil", "friendzone3_lucie_seen", style);

            case "Zoé Topaze" : {
                String seen = "friendzone3_zoe1_seen_";
                if (symbols.getChapterNumber() == 6) {
                    seen = "friendzone3_previous_zoe_seen";
                }
                return new Personnage(name, "friendzone3_zoe1_profil", seen, style);
            }

            case "Lana Welzitenburg" :
                return new Personnage(name, "friendzone3_lena", "friendzone3_lena_seen", style);

            case "Rick P." :
                return new Personnage(name, "friendzone3_lena", "friendzone3_rick_seen", style);

            case "Inconnu" :
                return new Personnage(name, "", "", style);

            case "Marwin D." :
                return new Personnage(name, "friendzone3_lena", "friendzone3_marwin_seen", style);
            case "Au Lac" :
                return new Personnage(name, "friendzone3_lake", "", style);

            default:
                throw new IllegalArgumentException();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilPicture(Context context) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(context, GlobalData.Game.FRIENDZONE3.getId(), profilPicture);
    }


    public String getTickImage(Context context) {
        return OnlineAssetsManager.INSTANCE.getImageFilePath(context, GlobalData.Game.FRIENDZONE3.getId(), tickImage);
    }


    public PersonnageStyle getStyle() {
        return style;
    }

    public void setStyle(PersonnageStyle style) {
        this.style = style;
    }

    public PersonnageStyle.StyleColor getStyleColor() {
        return styleColor;
    }
}

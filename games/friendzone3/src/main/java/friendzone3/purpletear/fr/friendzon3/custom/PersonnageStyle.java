package friendzone3.purpletear.fr.friendzon3.custom;


import friendzone3.purpletear.fr.friendzon3.R;

public class PersonnageStyle {
    private int idBackgroundColor;
    private int idTextColor;

    public enum StyleColor {
        me,
        main,
        second,
        third,
        fourth,
        fifth,
        system,
        smsMe,
        smsMain
    }

    private PersonnageStyle(int idBackgroundColor, int idTextColor) {
        this.idBackgroundColor = idBackgroundColor;
        this.idTextColor = idTextColor;
    }

    static PersonnageStyle determine(StyleColor style) {
        switch (style) {
            case me: return new PersonnageStyle(R.color.meBackground, R.color.white);
            case main: return new PersonnageStyle(R.color.mainBackground, R.color.mainText);
            case second: return new PersonnageStyle(R.color.secondBackground, R.color.white);
            case third: return new PersonnageStyle(R.color.thirdBackground, R.color.white);
            case fourth: return new PersonnageStyle(R.color.fourthBackground, R.color.white);
            case fifth: return new PersonnageStyle(R.color.fifthBackground, R.color.white);
            case system: return new PersonnageStyle(R.color.transparent, R.color.systemText);
            case smsMe: return new PersonnageStyle(R.color.meSmsBackground, R.color.white);
            case smsMain: return new PersonnageStyle(R.color.mainSmsBackground, R.color.white);
            default :
                throw new IllegalArgumentException();
        }
    }

    public int getIdBackgroundColor() {
        return idBackgroundColor;
    }

    public int getIdTextColor() {
        return idTextColor;
    }
}

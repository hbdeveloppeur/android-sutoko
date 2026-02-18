package fr.purpletear.friendzone4.game.activities.main;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.purpletear.friendzone4.GameData;
import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.game.tables.Var;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.Symbol;
import purpletear.fr.purpleteartools.TableOfSymbols;


public class Phrase implements Parcelable {

    /**
     * Phrase's id
     */
    private int id;

    /**
     * The id of the author of the hrase.
     */
    private int id_author;

    /**
     * The content of the sentence.
     */
    private String sentence;

    /**
     * How long to see the message
     */
    private int seen;

    /**
     * The time the person waits before answering.
     */
    private int wait;

    /**
     * The type of the phrase.
     */
    private int type;

    String code;

    /**
     * The type of the phrase.
     * /!\ You have to keep a certain logique between the Java code and javascript website code
     * see determineType
     */
    public enum Type {
        dest,
        condition,
        memory,
        info,
        image,
        action,
        typing,
        typingMe,
        meImage,
        vocal,
        nextChapter,
        date,
        alert,
        me,
        meSeen,
        noSignal,
        undetermined,
        gif,
        paused,
        minigame,
        trophy
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Phrase)) {
            return false;
        }

        Phrase other = (Phrase) obj;

        return other.getId() == getId()
                && other.getId_author() == getId_author()
                && other.getSentence().equals(getSentence());
    }

    /* ************/


    protected Phrase(Parcel in) {
        id = in.readInt();
        id_author = in.readInt();
        sentence = in.readString();
        seen = in.readInt();
        wait = in.readInt();
        type = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(id_author);
        dest.writeString(sentence);
        dest.writeInt(seen);
        dest.writeInt(wait);
        dest.writeInt(type);
    }

    public static final Creator<Phrase> CREATOR = new Creator<Phrase>() {
        @Override
        public Phrase createFromParcel(Parcel in) {
            return new Phrase(in);
        }

        @Override
        public Phrase[] newArray(int size) {
            return new Phrase[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    /* **************/

    /**
     * Builds a phrase easily
     *
     * @param id_author int
     * @param type      int
     * @param sentence  String
     * @return Phrase
     */
    @SuppressWarnings("unused")
    public static Phrase fast(int id_author, int type, String sentence, int seen, int wait) {
        return new Phrase(-1, id_author, sentence, seen, wait, type);
    }

    private Phrase(int id, int id_author, String sentence, int seen, int wait, int type) {
        this.id = id;
        this.id_author = id_author;
        this.sentence = sentence;
        this.seen = seen;
        this.wait = wait;
        this.type = type;
    }

    public Phrase(Type type) {
        this.id = 0;
        this.id_author = -1;
        this.sentence = "";
        this.seen = 0;
        this.wait = 0;
        this.type = determineTypeCode(type);
    }

    /**
     * Determines the type of the phrase
     *
     * @return Type
     */
    public static Type determineTypeEnum(int typeCode) {
        switch (typeCode) {
            case 0:
                return Type.typing;
            case 1:
                return Type.dest;
            case 2:
                return Type.condition;
            case 3:
                return Type.memory;
            case 4:
                return Type.info;
            case 5:
                return Type.image;
            case 6:
                return Type.action;
            case 7:
                return Type.date;
            case 8:
                return Type.me;
            case 9:
                return Type.nextChapter;
            case 10:
                return Type.meSeen;
            case 11:
                return Type.gif;
            case 12:
                return Type.paused;
            case 13:
                return Type.minigame;
            case 14 :
                return Type.trophy;
        }
        return Type.undetermined;
    }

    /**
     * Determines the code given the type
     *
     * @param type Type
     * @return int
     */
    public static int determineTypeCode(Type type) {
        switch (type) {
            case typing:
                return 0;
            case dest:
                return 1;
            case condition:
                return 2;
            case memory:
                return 3;
            case info:
                return 4;
            case image:
                return 5;
            case action:
                return 6;
            case date:
                return 7;
            case me:
                return 8;
            case nextChapter:
                return 9;
            case meSeen:
                return 10;
            case gif:
                return 11;
            case paused:
                return 12;
            case minigame:
                return 13;
            case trophy:
                return 14;
        }
        return -1;
    }


    public boolean isTrophy() {
        return sentence.startsWith("[TROPHY$$$")  && (!sentence.split("\\$\\$\\$")[1].replace("]", "").equals("NaN"));
    }

    public int getTrophyId() {
        return Integer.parseInt(sentence.split("\\$\\$\\$")[1].replace("]", ""));
    }

    /**
     * Determines if the sentence can be display or not.
     *
     * @return true if the sentence needs to be skipped
     */
    public boolean needsSkip() {
        if (type == Phrase.determineTypeCode(Type.paused)) return false;
        if (type == Phrase.determineTypeCode(Type.minigame)) return false;
        if (sentence == null) return false;
        if (sentence.replace(" ", "").equals("")) return true;
        if (sentence.length() == 0) return true;
        if (sentence.charAt(0) == '(' && sentence.charAt(sentence.length() - 1) == ')' && id_author == 0)
            return true;
        if (id_author == 0) return false;
        return false;
    }

    boolean isThunder(){
        return sentence.replace(" ", "").toUpperCase().equals("[THUNDER]");
    }

    void formatVars(Context c, TableOfSymbols table) {
        if(sentence == null) {
            return;
        }
        for(Symbol symbol : table.getArray(GlobalData.Game.FRIENDZONE4.getId())) {
            setSentence(c, sentence.replace("[["+symbol.getN()+"]]", symbol.getV()));
        }
    }

    /**
     * Tells if the sentence looks like a piece of code
     * use this to gain time and avoid using matches
     */
    private boolean looksLikeACode() {
        return !(sentence == null || sentence.length() == 0) && sentence.charAt(0) == '[';

    }

    public boolean isFriendAcceptNotification() {
        return looksLikeACode() && sentence.startsWith("[ACCEPT");
    }

    /**
     * Determines if the phrase is of type action.hesitate
     *
     * @return boolean
     */
    public boolean isHesitate() {
        return looksLikeACode() && sentence.replace(" ", "").equals("[ACTION-1]");
    }

    public boolean isFriendNotification() {
        return looksLikeACode() && sentence.startsWith("[FRIEND");
    }

    /**
     * Is the phrase a condition ?
     *
     * @return true if it is a condition
     */
    private boolean isCondition() {
        return looksLikeACode() && sentence.matches("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]");
    }

    /**
     * @param chapterNumber the number of the current chapter
     * @return the var concerned
     */
    public Var getVarFromCondition(int chapterNumber) {
        if (!isCondition()) {
            throw new IllegalArgumentException("CallinggetVarFromConditon on a non isCondition phrase " + sentence);
        }

        Pattern pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]");

        Matcher m = pattern.matcher(sentence);
        if (!m.find()) {
            throw new IllegalArgumentException("Phrase.getVarFromCondition.find");
        }

        String toSplit = m.group().replace("[", "").replace("]", "").replace(" ", "");
        String[] v = toSplit.split("=");
        return new Var(v[0], v[1], chapterNumber);
    }

    /**
     * Determines if the current Phrase is of type gif
     *
     * @return boolean
     */
    public boolean isGif() {
        return looksLikeACode() && sentence.endsWith(".gif]");
    }

    /**
     * Returns the name of the gif
     *
     * @return String
     */
    public String getGifName() {
        if (!isGif()) {
            throw new IllegalStateException("phrase " + toString() + " not a gif");
        }
        return sentence.replace(" ", "")
                .replace("[", "")
                .replace(".gif]", "");
    }

    /**
     * Returns the sentence without (info)
     *
     * @return String
     */
    String withoutInfo() {
        Pattern pattern = Pattern.compile("(\\(.*\\)[ ]*)");
        Matcher m = pattern.matcher(sentence);
        if (!m.find()) {
            return sentence;
        }
        return sentence.replace(m.group(), "");
    }

    public boolean isCode() {
        return looksLikeACode() && sentence.startsWith("[CODE_") && sentence.endsWith("]");
    }

    boolean isOverlayNotification() {
        return looksLikeACode() && sentence.startsWith("[notification");
    }

    /**
     * Returns the sentence inside a notification code.
     *
     * @return String
     */
    String getOverlayNotifSentence() {
        if (!isOverlayNotification()) {
            throw new IllegalStateException("Phrase is not overlaynotification");
        }
        return sentence
                .replace("[notification:\"", "")
                .replace("[notification2:\"", "")
                .replace("\"]", "");
    }

    int getOverlayNotifColorId() {
        if (sentence.contains("notification2")) {
            return R.color.notificationYellow;
        }
        return R.color.notificationPurple;
    }

    int getOverlayNotifTextColorId() {
        if (sentence.contains("notification2")) {
            return R.color.colorSoftBlack;
        }
        return R.color.colorWhite;
    }


    /**
     * Determines if it is a code
     * example : [CODE_1]
     *
     * @return the code
     */
    public String getCode() {
        if (!isCode()) {
            throw new IllegalArgumentException("Calling getCode but the Phrase is not of type code");
        }
        return sentence.replace("[CODE_", "")
                .replace("]", "");
    }

    /**
     * Is the Phrase an asnwer with a condition inside ?
     *
     * @return true if it is
     */
    private boolean isAnswerCondition() {
        return looksLikeACode() && sentence.matches("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zéA-Z0-9]+)\\]([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}");
    }

    public String[] getAnswerCondition() {
        if (!isAnswerCondition()) {
            throw new IllegalArgumentException("Calling getAnswerCondition on a non AnswerCondition");
        }


        Pattern pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zéA-Z0-9]+)\\]");
        Matcher m = pattern.matcher(sentence);

        if (!m.find()) throw new IllegalArgumentException("Phrase.getAnswerCondtion.find error");

        String condition = m.group();

        Pattern p2 = Pattern.compile("\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}");
        Matcher m2 = p2.matcher(sentence);
        String[] values = new String[3];
        values[0] = condition;

        int i = 1;
        while (m2.find()) {
            values[i] = m2.group().replace("{", "").replace("}", "");
            i++;
        }
        return values;
    }

    /**
     * Determines if it is a Content image.
     * [imagename1234.png]
     *
     * @return true if it is a content image
     */
    public boolean isContentImage() {
        return looksLikeACode() && sentence.matches("\\[([a-z0-9A-Z_]+).png\\]");
    }

    /**
     * Returns the name of the image for a ContentImage
     *
     * @return String
     */
    public String getContentImageName() {
        if (!isContentImage()) {
            throw new IllegalStateException("Calling getContentImageName > " + toString());
        }
        return "fz4_" + sentence.replace("[", "").replace(".png]", "");
    }

    /**
     * Determines if the phrase is of type Offline
     *
     * @return true if it is.
     */
    public boolean isOffline() {
        return sentence.equals("[OFFLINE]");
    }

    /**
     * Determines if there is a condition on the choice 's sentence
     *
     * @return true if it is.
     */
    boolean isChoiceEqualCondition() {
        return code != null && code.matches("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*") || sentence.matches("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*");
    }


    /**
     * Determines if there is a condition on the choice 's sentence
     * @return true if it is.
     */
    public boolean isChoiceCondition() {
        return (null != code && code.matches("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*")) || sentence.matches("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*");
    }

    /**
     * Return the condition in a String object
     * For exemple :
     * "[name == value]MySentence" will return [name == value]
     * @return
     */
    public String getChoiceCondition() {
        if(!isChoiceCondition()) {
            throw new IllegalArgumentException("Calling getChoiceCondition on a non choiceCondition sentence");
        }

        Pattern p = Pattern.compile("^\\[([0-9a-zA-Z_]+)([ ]*)==([ ]*)([0-9a-zA-Z_]+)\\]([ ]*)");

        if(code == null) {
            code = "";
        }

        Matcher m = p.matcher(code);
        if(!m.find()) {
            throw new IllegalArgumentException("Phrase.getChoiceCondition.find didin't work with ");
        }

        return m.group().replace(" ", "");
    }

    /**
     * Determines if there is a not equal condition on the choice 's sentence
     *
     * @return true if it is.
     */
    boolean isChoiceNotEqualCondition() {
        return  code != null && code.matches("^\\[([0-9a-zA-Z_]*)([ ]*)!=([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*") || sentence.matches("^\\[([0-9a-zA-Z_]*)([ ]*)!=([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*");
    }

    /**
     * Return the condition in a String object
     * For exemple :
     * "[name == value]MySentence" will return [name == value]
     */
    public String getChoiceEqualCondition() {
        if (!isChoiceEqualCondition()) {
            throw new IllegalArgumentException("Calling getChoiceCondition on a non choiceCondition sentence");
        }

        Pattern p = Pattern.compile("^\\[([0-9a-zA-Z_]+)([ ]*)==([ ]*)([0-9a-zA-Z_]+)\\]([ ]*)");
        Matcher m = p.matcher((code.contains("==") ? code : sentence));
        if (!m.find()) {
            throw new IllegalArgumentException("Phrase.getChoiceCondition.find didin't work with " + sentence);
        }

        return m.group().replace(" ", "");
    }

    /**
     * Return the not equal condition in a String object
     * For exemple :
     * "[name == value]MySentence" will return [name == value]
     */
    public String getChoiceNotEqualCondition() {
        if (!isChoiceNotEqualCondition()) {
            throw new IllegalArgumentException("Calling getChoiceCondition on a non choiceCondition sentence");
        }

        Pattern p = Pattern.compile("^\\[([0-9a-zA-Z_]+)([ ]*)!=([ ]*)([0-9a-zA-Z_]+)\\]([ ]*)");
        Matcher m = p.matcher((code.contains("!=") ? code : sentence));
        if (!m.find()) {
            throw new IllegalArgumentException("Phrase.getChoiceCondition.find didn't work with " + p.toString());
        }

        return m.group().replace(" ", "");
    }

    /**
     * @return the sentence without the condition
     */
    String getChoiceEqualConditionFormated() {
        if(sentence.contains("==")) {
            return sentence.replace(" == ", "==").replace(getChoiceEqualCondition(), "");
        }
        return sentence;
    }

    /**
     * @return the sentence without the not equal condition
     */
    String getChoiceNotEqualConditionFormated() {
        if(sentence.contains("!=")) {
            return sentence.replace(" != ", "!=").replace(getChoiceNotEqualCondition(), "");
        }
        return sentence;
    }

    /**
     * Replace "[prenom]" by the name specified in the paramters
     *
     * @param replacement replace the target with this
     */
    void formatSentence(String replacement, Context context) {
        if (sentence == null) {
            return;
        }

        if(sentence.contains("<DAY>")) {
            sentence = sentence.replace("<DAY>", DateTools.INSTANCE.getCurrentDayName(sentence.startsWith("<DAY>"), context));
        }

        sentence = sentence.replace("[prenom]", replacement);
    }

    boolean isMe() {
        return sentence.startsWith("[me:\"");
    }

    String getMe() {
        if(!isMe()) {
            throw new IllegalStateException();
        }
        return sentence.replace("[me:\"", "").replace("\"]", "");
    }

    /**
     * Determines if the phrase is a next Chapter.
     *
     * @return true if it is
     */
    public boolean isNextChapter() {
        return looksLikeACode()
                && sentence.equals("[ACTION-3]");
    }

    boolean isEnd() {
        return sentence.replace(" ", "").equals("[END]");
    }

    public String getNextChapter() {
        if(code == null) {
            return "1A";
        }
        return code.replace(" ", "");
    }

    boolean isSound() {
        return looksLikeACode() && sentence.endsWith(".mp3]");
    }

    String getSoundName() {
        if (!isSound()) {
            throw new IllegalStateException("Calling getSound on a non-sound");
        }
        return sentence.replace("[", "").replace(".mp3]", "");
    }

    boolean isJumpToId() {
        return sentence.contains("[JUMPTOID");
    }

    int getJumpToId() {
        return Integer.parseInt(sentence
                .replace(" ", "")
                .replace("[JUMPTOID_", "")
                .replace("]", ""));
    }

    boolean isTimedChoice() {
        return sentence.contains("TIMEDCHOICE");
    }

    int getTimedChoiceTime() {
        if (!isTimedChoice()) {
            throw new IllegalStateException();
        }
        return Integer.parseInt(sentence.replace(" ", "")
                .replace("[TIMEDCHOICE/", "")
                .replace("]", ""));
    }


    boolean isConversationStatusChange() {
        return sentence.contains("[conversationStatus=\"");
    }

    String getConversationStatusChange() {
        if(!isConversationStatusChange()) {
            throw new IllegalStateException();
        }
        return sentence.replace("[conversationStatus=\"", "").replace("]", "");
    }

    boolean isBackgroundImage() {
        return looksLikeACode() && sentence.endsWith(".jpeg]");
    }

    String getBackgroundImageName() {
        if (!isBackgroundImage()) {
            throw new IllegalStateException();
        }
        return sentence.replace(" ", "").replace("[", "").replace(".jpeg]", "");
    }

    /**
     * Determines if the phrase is an action to ban the player
     *
     * @return boolean
     */
    boolean isBan() {
        return sentence.equals("[BANNED]");
    }

    public static long nextChapterDelay() {
        return 7000;
    }

    /**
     * Determines if the current Phrase is of type <type>
     *
     * @return boolean
     */
    public boolean is(Type type) {
        return type == Phrase.determineTypeEnum(this.type);
    }

    public int getId() {
        return id;
    }

    public int getId_author() {
        return id_author;
    }

    public String getSentence() {
        return sentence;
    }

    public int getSeen() {
        return seen;
    }

    public int getWait() {
        return wait;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(Type type) {
        this.type = Phrase.determineTypeCode(type);
    }

    public void setSentence(Context context, String sentence) {
        if(determineTypeEnum(type) == Type.info
        || determineTypeEnum(type)  == Type.me
        || determineTypeEnum(type)  == Type.dest) {
            this.sentence = GameData.INSTANCE.updateNames(context, sentence);
            return;
        }
        this.sentence = sentence;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    @NonNull
    @Override
    public String toString() {
        return "Phrase{" +
                "id=" + id +
                ", id_author=" + id_author +
                ", sentence='" + sentence + '\'' +
                ", seen=" + seen +
                ", wait=" + wait +
                ", type=" + type +
                '}';
    }
}

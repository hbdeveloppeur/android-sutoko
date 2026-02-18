package friendzone3.purpletear.fr.friendzon3.custom;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import friendzone3.purpletear.fr.friendzon3.MainActivity;
import friendzone3.purpletear.fr.friendzon3.config.Var;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class Phrase implements Parcelable {

    public int id;

    /**
     * The content of the sentence.
     */
    private String sentence;

    /**
     * The time the person waits before answering.
     */
    private int wait;

    /**
     * The id of the author of the hrase.
     */
    private int id_author;

    /**
     * The type of the phrase.
     */
    private int type;

    public int getSeen() {
        return seen;
    }

    public String code;

    /**
     * How long to see the message
     */
    private int seen;


    public Phrase(Parcel parcel) {
        id = parcel.readInt();
        sentence = parcel.readString();
        wait = parcel.readInt();
        id_author = parcel.readInt();
        type = parcel.readInt();
        code = parcel.readString();
        seen = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(sentence);
        dest.writeInt(wait);
        dest.writeInt(id_author);
        dest.writeInt(type);
        dest.writeString(code);
        dest.writeInt(seen);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Phrase createFromParcel(Parcel in) {
            return new Phrase(in);
        }

        public Phrase[] newArray(int size) {
            return new Phrase[size];
        }
    };


    public enum Type {
        me,
        dest,
        info,
        meSeen,
        image,
        typing,
        meImage,
        vocal,
        nextChapter,
        date,
        hidden,
        robot,
        robotCode,
        unHiding,
        alert,
        trophy
    }

    public Phrase(){}

    public Phrase(@Nullable String sentence, int wait, int id_author, int seen, String code) {
        this.sentence = sentence;
        this.wait = wait;
        this.id_author = id_author;
        this.seen = seen;
        this.code = code;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {this.wait = wait;}

    public void setSeen(int seen) {this.seen = seen;}

    private Type intToType(int type) {
        switch (type) {
            case 99:
                return Type.vocal;
            case 98:
                return Type.date;
            case 97:
                return Type.image;
            case 96:
                return Type.hidden;
            case 95:
                return Type.typing;
            case 94:
                return Type.robot;
            case 93 :
                return Type.alert;
            case 92 :
                return Type.nextChapter;
            case 91:
                return Type.robotCode;
            case 90:
                return Type.unHiding;
            case 89:
                return Type.meSeen;
            case 87:
                return Type.trophy;
            case 4 :
                return Type.info;
            case 1:
                if(id_author == 0 || id_author == 15) {
                    return Type.me;
                } else {
                    return Type.dest;
                }
        }
        return Type.info;
    }

    private int typeToInt(Type type) {
        switch(type) {
            case vocal:
                return 99;
            case date:
                return 98;
            case image:
                return 97;
            case hidden:
                return 96;
            case typing:
                return 95;
            case robot:
                return 94;
            case alert:
                return 93;
            case nextChapter:
                return 92;
            case robotCode:
                return 91;
            case unHiding:
                return 90;
            case meSeen:
                return 89;
            case info :
                return 4;
            case trophy:
                return 87;
            case me :
            case dest :
                return 1;
        }
        return 1;
    }

    public void setType(Type type) {
        this.type = typeToInt(type);
    }

    public Type getType() {
        return intToType(type);
    }

    public int getId() {
        return id;
    }

    public int getId_author() {
        return id_author;
    }
    public void setId_author(int id){
        id_author = id;
    }
    public Phrase getAnswer(Discussion discussion) {
        return discussion.getAnswer(id);
    }

    public ArrayList<Phrase> getAnswers(Discussion discussion) {
        return discussion.getAnswers(id);
    }

    /**
     * Determines if the sentence can be display or not.
     * @return true if the sentence needs to be skipped
     */
    public boolean needsSkip(){
        if(sentence == null) return false;
        if(null != code && !code.replace(" ","").equals("")) return false;
        if(sentence.replace(" ","").equals("")) return true;
        if(sentence.equals("[SCREENSHOT]")) return true;
        if(sentence.length() == 0) return true;
        if(sentence.charAt(0) == '(' && sentence.charAt(sentence.length() - 1) == ')') return true;
        if(id_author == 0) return false;
        return false;
    }

    /**
     * Tells if the sentence looks like a piece of code
     * use this to gain time and avoid using matches
     */
    private boolean looksLikeACode() {
        return !(sentence == null || sentence.length() == 0) && sentence.charAt(0) == '[';

    }

    /**
     * Tells if the sentence is an Annoucement
     * @return true if it is.
     */
    public boolean isAnnouncement() {
        return isCode("ANNOUNCEMENT");
    }

    /**
     * Tells if the sentence is hidden
     * @return true if it is.
     */
    public boolean isHidden() {
        return isCode("deleted_message");
    }

    /**
     * Returns the hidden message from an isHidden message
     * @return the hidden sentence.
     */
    public String getHidden() {
        if(!isHidden()) {
            throw new IllegalArgumentException("Calling getHidden on a non isHidden");
        }
        return sentence;
    }

    /**
     * Tells if the sentence is hidden
     * @return true if it is.
     */
    public boolean isRobot(MainActivity.Support support) {
        return isCode("bot")
                || support == MainActivity.Support.SHELL;
    }

    /**
     * Returns the hidden message from an isHidden message
     * @return the hidden sentence.
     */
    public String getRobot(MainActivity.Support support) {
        if(!isRobot(support)) {
            throw new IllegalArgumentException("Calling getRobot on a non isRobot");
        }
        return sentence;
    }

    /**
     * Determines if it is an ask for online
     * @return true if it is.
     */
    public boolean isAskForOnline() {
        return isCode("online");
    }

    /**
     * Determines if it is an ask for online
     * @return true if it is.
     */
    public boolean isAskForSeen() {
        return isCode("seen");
    }

    /**
     * Tells if the sentence is hidden
     * @return true if it is.
     */
    public boolean isRobotCode(MainActivity.Support support) {
        return isCode("botCode");
    }

    /**
     * Returns the hidden message from an isHidden message
     * @return the hidden sentence.
     */
    public String getRobotCode(MainActivity.Support support) {
        if(!isRobotCode(support)) {
            throw new IllegalArgumentException("Calling getRobotCode on a non isRobotCode");
        }
        return sentence;
    }


    /**
     * Tells if the sentence is an alert
     * @return true if it is.
     */
    public boolean isAlert() {
        return isCode("alert");
    }

    /**
     * Returns the alert from an alert message
     * @return the date sentence.
     */
    public String getAlert() {
        if(!isAlert()) {
            throw new IllegalArgumentException("Calling getAlert on a non isAlert");
        }
        return sentence;
    }

    /**
     * Tells if the sentence is a date
     * @return true if it is.
     */
    public boolean isDate() {
        return code != null && code.replace(" ", "").toLowerCase().equals("date");
    }

    /**
     * Returns the date from an isDate message
     * @return the date sentence.
     */
    public String getDate() {
        if(!isDate()) {
            throw new IllegalArgumentException("Calling getDate on a non isDate");
        }
        return sentence;
    }

    /**
     * Tells if the sentence is a next part.
     * @return true if it is.
     */
    public boolean isNextPart() {
        return looksLikeACode() && sentence.replace(" ", "").equals("[NEXTPART]");
    }

    /**
     * Determines if the author is me or not.
     * @param code the chapter's code
     * @return true if it is.
     */
    public boolean isMe(String code, TableOfSymbols symbols) {
        return !(sentence == null || sentence.length() == 0) && Personnage.who(code, id_author, symbols).getName().equals("Me");
    }

    /**
     * Determines if the phrase has to be in a notification or not.
     * @return true if it has to.
     */
    public boolean isInNotif() {
        return code != null && code.replace(" ", "").toLowerCase().equals("notif");
    }

    /**
     * Returns the formated sentence
     * @return
     */
    public String getInNotf() {
        if(!isInNotif()) {
            throw new IllegalArgumentException("Calls getInNotif on a non isInNotif phrase " + toString());
        }
        return sentence.replace("[notif:\"", "").replace("\"]", "");
    }

    public String getContentImage() {
        if(!isContentImage()) {
            throw new IllegalArgumentException("Calls getContentImage on a non contentImage phrase " + toString());
        }
        return sentence.replace("[", "")
                .replace("]", "")
                .replace(".png", "")
                .replace(" ", "");
    }

    @SuppressWarnings("")
    public boolean isAcceptFriendNotification() {
        return looksLikeACode() && sentence.matches("\\[ACCEPT\\/([a-zA-Z\\xA8-\\xFE ]+)\\/([0-9]+):([0-9]+)\\]");
    }

    public boolean isFriendNotification() {
        return looksLikeACode() && sentence.matches("\\[FRIEND\\/([a-zA-Z\\xA8-\\xFE ]+)\\/([0-9]+):([0-9]+)\\]");
    }

    public boolean isBackgroundVideo() {
        return looksLikeACode() && sentence.matches("\\[([0-9a-zA-Z_]+).mp4\\]");
    }

    public String backgroundVideoName() {
        if(!isBackgroundVideo()) {
            throw new IllegalArgumentException("Calling backgroundVideoName on a not backgroundVideo Phrase.");
        }
        return "friendzone3_" + sentence.replace("[", "")
                .replace(".mp4]", "")
                .replace(" ", "");
    }

    public boolean isSendScreenshot() {
        return sentence.equals("[SENDSCREENSHOT]");
    }

    /**
     * Returns a notification for the type "isFriendNotification
     * @param parent the viewGroup to place the notification to
     * @return
     *
    public NotificationHandler friendNotification(View parent) {
        if(!isFriendNotification()) {
            throw new IllegalArgumentException("The type needs to be isFriendNotification at Phrase.friendNotification");
        }
        Pattern pattern = Pattern.compile("([a-zA-Z\\xA8-\\xFE]+) ([a-zA-Z\\xA8-\\xFE]+)");
        Matcher m = pattern.matcher(getSentence());
        if (!m.find()) Log.v("Console", "Error parsing");
        final String name = m.group();

        Pattern p2 = Pattern.compile("([0-9]+):([0-9]+)");
        Matcher m2 = p2.matcher(getSentence());
        if (!m2.find()) Log.v("Console", "Error parsing");
        final String hour = m2.group(0);

        return new NotificationHandler(
                parent,
                name,
                parent.getContext().getString(R.string.mainactivity_friend, hour),
                R.drawable.add_friend);
    }*/

    /**
     * Is the phrase an info ?
     * @return true if it is an info
     */
    public boolean isInfo() {
        return type == 4;
    }

    /**
     * Is the phrase a condition ?
     * @return true if it is a condition
     */
    public boolean isCondition() {
        return looksLikeACode() &&  sentence.matches("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]");
    }

    /**
     * @param chapterNumber the number of the current chapter
     * @return the var concerned
     */
    public Var getVarFromCondition(int chapterNumber){
        if(!isCondition()) {
            throw new IllegalArgumentException("CallinggetVarFromConditon on a non isCondition phrase " + sentence);
        }

        Pattern pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)=([ ]*)([a-zA-Z0-9]+)\\]");

        Matcher m = pattern.matcher(sentence);
        if (!m.find()){
            throw new IllegalArgumentException("Phrase.getVarFromCondition.find");
        }

        String toSplit = m.group().replace("[", "").replace("]", "").replace(" ", "");
        String[] v = toSplit.split("=");
        return new Var(v[0], v[1], chapterNumber);
    }

    public boolean isCode() {
        return null != code && code.replace(" ", "").toLowerCase().matches("(code_[0-9]+)");
    }

    /**
     * Determines if it is a code
     * example : [CODE_1]
     * @return the code
     */
    public int getCode() {
        if(!isCode()) {
            throw new IllegalArgumentException("Calling getCode but the Phrase is not of type code");
        }
        return Integer.parseInt(code.toLowerCase().replace("code_", "")
                .replace(" ", ""));
    }

    /**
     * Is the Phrase an asnwer with a condition inside ?
     * @return true if it is
     */
    public boolean isAnswerCondition() {
        return looksLikeACode() &&  sentence.matches("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zéA-Z0-9]+)\\]([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}([ ]*)\\{([\\'\\[\\]=)^:…;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}");
    }

    public String[] getAnswerCondition() {
        if(!isAnswerCondition()) {
            throw new IllegalArgumentException("Calling getAnswerCondition on a non AnswerCondition");
        }


        Pattern pattern = Pattern.compile("\\[([a-zA-Z0-9]+)([ ]*)==([ ]*)([a-zA-Z0-9]+)\\]");
        Matcher m = pattern.matcher(sentence);

        if (!m.find()) throw new IllegalArgumentException("Phrase.getAnswerCondtion.find error");

        String condition = m.group();

        Pattern p2 = Pattern.compile("\\{([\\'\\[\\]=)^:;a-zA-Z0-9_\\xA8-\\xFE\\.!?,\\-\\\" ]*)\\}");
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
     * @return true if it is a content image
     */
    public boolean isContentImage() {
        return isCode("user_image");
    }

    private boolean isCode(String str) {
        return null != code && code.replace(" ", "").toLowerCase().equals(str.toLowerCase());
    }

    /**
     * Determines if the phrase represents a lost game.
     * @return true if it is a lost game.
     */
    public boolean isLose() {
        return sentence.equals("[LOST]") || sentence.equals("[LOSE]");
    }

    /**
     * Determines if the phrase represents a sound.
     * @return true if it is
     */
    public boolean isSound() { return isCode("sound");}

    /**
     * Determines the name of the sound.
     * @return the name of the sound.
     */
    public String soundName() {
        if(!isSound()) {
            throw new IllegalArgumentException("Calling soundName but the Phrase is not of type sound");
        }
        return sentence
                .replace("]", "")
                .replace(".mp3", "")
                .replace("[", "");
    }

    public boolean isTrophy() {
        return sentence.startsWith("[TROPHY$$$")   && (!sentence.split("\\$\\$\\$")[1].replace("]", "").equals("NaN"));
    }

    public int getTrophyId() {
        return Integer.parseInt(sentence.split("\\$\\$\\$")[1].replace("]", ""));
    }

    /**
     * Determines if the phrase is of type Offline
     * @return true if it is.
     */
    public boolean isOffline() {
        return isCode("offline");
    }

    /**
     * Determines if the phrase is of type BackgroundPicture
     * @return true if it is
     */
    public boolean isBackgroundPicture() {
        return isCode("background_image");
    }

    /**
     * Determines the name of the picture if it is a backgroundPicture
     * Throws an error else
     * @return the name of the background
     */
    public String backgroundPictureName() {
        if(!isBackgroundPicture()) throw new IllegalArgumentException("Calling Phrase.backgroundPictureName on a non backgroundPicture");
        return sentence.replace("[BACKGROUND_", "")
                .replace(".jpg", "").replace("]", "");
    }

    /**
     * Determines if it is a change of picture from Zoé
     * happens in 7b
     * @return true if it is.
     */
    public boolean isZoeChangePicture() {
        return looksLikeACode() && (sentence.equals("[SETIMAGEZOE1]") || sentence.equals("[SETIMAGEZOE2]"));
    }

    /**
     * Determines if it is a call to lance the Game "Save Zoé"
     * @return true if it is.
     */
    public boolean isGame() {
        return looksLikeACode() && sentence.equals("[SAVEZOE.GAME]");
    }

    /**
     * Determines if the phrase is of type Animation.
     * @return true if it is.
     */
    public boolean isAnimation() {
        return looksLikeACode() && sentence.matches("\\[ANIMATION([0-9]+)\\]");
    }

    /**
     * Determines if there is a condition on the choice 's sentence
     * @return true if it is.
     */
    public boolean isChoiceCondition() {
        return null != code && code.matches("^\\[([0-9a-zA-Z_]*)([ ]*)==([ ]*)([0-9a-zA-Z_]*)\\]([ ]*).*");
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
     * @return the sentence without the condition
     */
    public String getChoiceConditionFormated(){
        return sentence.replace(" == ", "==").replace(getChoiceCondition(), "");
    }

    /**
     * Determines the code of an animation
     * @return the code of an animation
     */
    public int codeAnimation() {
        if(!isAnimation()) throw new IllegalArgumentException("Calling Animation.codeAnimation on an non animation Phrase.");
        return Integer.parseInt(sentence.replace("[ANIMATION", "").replace("]", ""));
    }

    /**
     * Replace "[prenom]" by the name specified in the paramters
     * @param target the occurence to replace
     * @param replacement replace the target with this
     */
    public void formatName(String target, String replacement) {
        if(sentence == null) {
            return;
        }
        sentence = sentence.replace(target, replacement);
    }

    public boolean isDoubleType() {
        return isCode("doubletype") || isCode("double_type");
    }

    /**
     * Determines if the phrase is a next Chapter.
     * @return true if it is
     */
    public boolean isNextChapter() {
        return code != null && code.replace(" ", "").toLowerCase().matches("chapter_([0-9]+)([a-z]+)");
    }

    public void controlEmojis(MainActivity.Support support) {
        if(support != MainActivity.Support.NORMAL
                || looksLikeACode()) {
            return;
        }
        translateEmoji("[tilt]");
        translateEmoji("x'D");
        translateEmoji("xD");
        translateEmoji(":D");
        translateEmoji(":)");
        translateEmoji("^^'");
        translateEmoji("*.*");
        translateEmoji("|_|");
        translateEmoji("[king]");
        translateEmoji("[celebration]");
    }

    private void translateEmoji(String name) {
        sentence = sentence.replace(name, Emojis.Translate(name));
    }

    public static Phrase link(ArrayList<Phrase> array) {
        for(int i = 0; i<array.size() -1; i++) {

        }
        return array.get(0);
    }

    public static Phrase fast(String sentence, int seen, int wait, int id_author, String code){
        return new Phrase(sentence, wait, id_author, seen, code);
    }

    public boolean isBackgroundImage() {
        return code.equals("background_image");
    }

    public String getBackgroundImageName() {
        if(!isBackgroundImage()) {
            throw new IllegalStateException();
        }

        return sentence.replace(" ", "").replace("[BACKGROUND_", "").replace(".jpeg", "").replace(".jpg", "").replace("]", "");
    }

    @Override
    public String toString() {
        return "Phrase{" +
                "sentence='" + sentence + '\'' +
                ", wait=" + wait +
                ", seen=" + seen +
                ", id_author=" + id_author +
                ", code=" + code +
                ", type=" + type +
                '}';
    }
}

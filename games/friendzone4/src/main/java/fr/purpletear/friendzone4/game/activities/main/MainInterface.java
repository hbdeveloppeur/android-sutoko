package fr.purpletear.friendzone4.game.activities.main;

public interface MainInterface {
    void onClickChoice(Phrase p);
    void onInsertPhrase(int position, boolean isSmoothScroll);
    void onClickContact(String code, Contact.Type type);
    void onClickItem(String type, String name);
    void onMissedChoice(Phrase p);
    void onTouchJoystick();
    void onReleaseJoystick();
    void onJoystickInfoHit(long startMs, String code);
}

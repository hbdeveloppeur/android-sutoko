package fr.purpletear.friendzone4.game.activities.end;

class EndModel {
    private boolean isFirstStart;

    public EndModel() {
        isFirstStart = true;
    }

    public boolean isFirstStart() {
        boolean v = isFirstStart;
        isFirstStart = false;
        return v;
    }
}

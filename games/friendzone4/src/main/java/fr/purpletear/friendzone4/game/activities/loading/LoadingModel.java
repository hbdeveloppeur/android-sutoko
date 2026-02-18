package fr.purpletear.friendzone4.game.activities.loading;

import fr.purpletear.friendzone4.purpleTearTools.MemoryHandler;
import fr.purpletear.friendzone4.purpleTearTools.Runnable2;

class LoadingModel {
    private boolean isFirstStart;
    private static final int DELAY = 2000;

    LoadingModel() {
        isFirstStart = true;
    }

    void goToNextActivity(MemoryHandler mh, final Runnable onCompletion) {
        Runnable2 runnable = new Runnable2("Go", 2800) {
            @Override
            public void run() {
                if(onCompletion != null) {
                    onCompletion.run();
                }
            }
        };
        mh.push(runnable);
        mh.run(runnable);
    }

    boolean isFirstStart() {
        boolean value = isFirstStart;
        isFirstStart = false;
        return value;
    }
}

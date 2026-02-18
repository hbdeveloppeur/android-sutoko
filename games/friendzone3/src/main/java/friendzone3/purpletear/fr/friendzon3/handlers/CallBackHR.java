package friendzone3.purpletear.fr.friendzon3.handlers;

import android.os.Handler;

public class CallBackHR {
    private Runnable runnable;
    private Handler handler = new Handler();

    public CallBackHR(Handler handler, Runnable runnable) {
        this.runnable = runnable;
        this.handler = handler;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public Handler getHandler() {
        return handler;
    }
}
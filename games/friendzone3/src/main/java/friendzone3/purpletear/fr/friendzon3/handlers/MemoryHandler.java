package friendzone3.purpletear.fr.friendzon3.handlers;

import android.os.Handler;

import java.util.ArrayList;

public class MemoryHandler {
    private ArrayList<CallBackHR> arrayOfCallBacks = new ArrayList<>();

    public void kill(){
        for(CallBackHR callBack : arrayOfCallBacks) {
            callBack.getHandler().removeCallbacks(callBack.getRunnable());
        }
        arrayOfCallBacks.clear();
    }

    public void push(final Handler handler, final Runnable runnable){
        arrayOfCallBacks.add(new CallBackHR(handler, runnable));
    }
}
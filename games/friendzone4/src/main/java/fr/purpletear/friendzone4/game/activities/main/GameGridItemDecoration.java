package fr.purpletear.friendzone4.game.activities.main;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import fr.purpletear.friendzone4.game.adapters.GameConversationAdapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;


public class GameGridItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private final boolean includeEdge;

    GameGridItemDecoration(int space, boolean includeEdge) {
        this.space = space;
        this.includeEdge = includeEdge;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        outRect.top = space / 2;
        outRect.bottom = space / 2;

        if(parent.getAdapter() instanceof  GameConversationAdapter) {
            GameConversationAdapter adapter = ((GameConversationAdapter) parent.getAdapter());
            if(adapter!= null) {
                int position = parent.getChildAdapterPosition(view);
                if(position == NO_POSITION) {
                    position = parent.getChildViewHolder(view).getOldPosition();
                }
                if(adapter.isEmpty()) {
                    return;
                }
                Phrase.Type type = Phrase.determineTypeEnum(adapter.getItemViewType(position));
                if(type == Phrase.Type.minigame) {
                    outRect.left = 0;
                    outRect.right = 0;
                    return;
                }
            }
        }

        if(includeEdge) {
            outRect.left = space;
            outRect.right = space;
        }
    }
}
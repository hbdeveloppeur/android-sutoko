package fr.purpletear.friendzone4.purpleTearTools;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private final boolean includeEdge;

    public GridSpacingItemDecoration(int space, boolean includeEdge) {
        this.space = space;
        this.includeEdge = includeEdge;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if(position == NO_POSITION) {
            position = parent.getChildViewHolder(view).getOldPosition();
        }

        outRect.bottom = 0;

        if(position == 0) {
            outRect.top = space;
        } else {
            outRect.top = space / 2;
            outRect.bottom = space / 2;
        }

        if(includeEdge) {
            outRect.left = space;
            outRect.right = space;
        }
    }
}
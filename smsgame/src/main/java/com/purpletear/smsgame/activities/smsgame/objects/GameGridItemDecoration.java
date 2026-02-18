/*
 * Copyright (C) PurpleTear, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Hocine Belbouab <hbdeveloppeur@gmail.com>, 1/10/19 11:38 AM
 *
 */

package com.purpletear.smsgame.activities.smsgame.objects;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;


import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import com.purpletear.smsgame.activities.smsgame.adapter.GameConversationAdapter;

public class GameGridItemDecoration extends RecyclerView.ItemDecoration {

    private final int space;
    private final boolean includeEdge;

    public GameGridItemDecoration(int space, boolean includeEdge) {
        this.space = space;
        this.includeEdge = includeEdge;
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {


        outRect.top = space;
        outRect.bottom = 0;
        if (includeEdge) {
            outRect.left = space;
            outRect.right = space;
        }

        if (parent.getAdapter() instanceof GameConversationAdapter) {
            GameConversationAdapter adapter = ((GameConversationAdapter) parent.getAdapter());
            if (adapter != null) {
                int position = parent.getChildAdapterPosition(view);
                if (position == NO_POSITION) {
                    position = parent.getChildViewHolder(view).getOldPosition();
                }

                if (adapter.getItemCount() == 0) {
                    return;
                }


                if (adapter.shouldRemoveSpaceWithPrevious(position)) {
                    outRect.top = space / 2;
                }

                if (adapter.lastHasType(Phrase.Type.rate)) {
                    outRect.bottom = 0;
                }
            }
        }


    }
}

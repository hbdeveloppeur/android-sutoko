package fr.purpletear.friendzone4.game.activities.hacking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fr.purpletear.friendzone4.R;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    /**
     * Contains the displaying list of users
     */
    private ArrayList<String> array;

    /**
     * Represents a View's type
     */
    private enum ViewType {
        ROW
    }

    public void clear() {
        array.clear();
        notifyDataSetChanged();
    }

    Adapter() {
        this.array = new ArrayList<>();
    }


    public void setArray(ArrayList<String> array) {
        this.array.addAll(array);
        notifyDataSetChanged();
    }

    void insert(String str) {
        array.add(str);
        notifyItemInserted(array.size() - 1);
    }

    private void clearArrayList() {
        int size = array.size();
        array.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Returns view's type
     *
     * @param viewType int
     * @return ViewType
     */
    private static ViewType viewType(int viewType) {
        return ViewType.values()[viewType];
    }

    @Override
    public int getItemViewType(int position) {
        return ViewType.ROW.ordinal();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View inflated;
        switch (viewType(viewType)) {
            case ROW:
                inflated = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fz4_layout_intro_code, viewGroup, false);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return new ViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (Adapter.viewType(getItemViewType(i)) == ViewType.ROW) {
            viewHolder.display(array.get(i));
        }

    }

    @Override
    public int getItemCount() {
        return array.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }

        void display(String str) {
            ((TextView) itemView.findViewById(R.id.fz4_item_code)).setText(str);
        }
    }
}

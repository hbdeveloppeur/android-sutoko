package fr.purpletear.friendzone2.activities.phone.photos;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.sharedelements.OnlineAssetsManager;

import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone2.Data;
import fr.purpletear.friendzone2.R;
import purpletear.fr.purpleteartools.GlobalData;


/**
 * Adapter of the calendar in the Calendar Fragment.
 * Reprents 90 days.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private Activity activity;
    private Context context;
    private RequestManager requestManager;
    public List<String> paths = new ArrayList<>();

    public Adapter(Context context, RequestManager requestManager) {
        this.context = context;
        this.requestManager = requestManager;
    }

    public void fill(){
        for (int i = 1;  i < 16;i++) {
            paths.add(OnlineAssetsManager.INSTANCE.getImageFilePath(context, GlobalData.Game.FRIENDZONE2.getId(), "/eva_phone/images/"+i));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.display(paths.get(position));
    }

    @Override
    public int getItemCount() {
        return paths.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        private ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.adapter_photo);
        }


        private void display(final String path) {
            requestManager
                    .load(path)
                    .into(image);
        }
    }
}

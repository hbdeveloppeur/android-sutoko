package fr.purpletear.friendzone4.adapter;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.callback.ViewPagerCallback;

public class GalleryAdapter extends PagerAdapter {
    private final RequestManager glide;
    private List<String> urls;
    private ViewPagerCallback callback;

    public GalleryAdapter(RequestManager glide, ViewPagerCallback callback) {
        this.urls = new ArrayList<>();
        this.glide = glide;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    public void update(List<String> urls) {
        update(urls, true);
    }

    public void update(List<String> urls, boolean withCallBack) {
        Log.e("purpleteardebug", "call with size : " + urls.size());
        this.urls.clear();
        this.urls.addAll(urls);
        notifyDataSetChanged();
        if(withCallBack && callback != null) {
            callback.onItemCountUpdate(urls.size());
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = LayoutInflater.from(container.getContext()).inflate(R.layout.fz4_layout_profil_gallery_, container, false);
        glide.load(urls.get(position))
                .transition(withCrossFade())
                .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                .into((ImageView) v.findViewById(R.id.fz4_profil_gallery_image_));
        v.setBackgroundColor(Color.parseColor("#000000"));
        container.addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if(((View) object).findViewById(R.id.fz4_profil_gallery_image_) != null) {
            glide.clear((View) ((View) object).findViewById(R.id.fz4_profil_gallery_image_));
        }
        container.removeView((View) object);
    }
}

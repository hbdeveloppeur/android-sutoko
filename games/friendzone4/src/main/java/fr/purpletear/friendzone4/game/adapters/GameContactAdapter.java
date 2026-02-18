package fr.purpletear.friendzone4.game.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.sharedelements.OnlineAssetsManager;

import java.util.List;

import fr.purpletear.friendzone4.R;
import fr.purpletear.friendzone4.game.activities.main.Contact;
import fr.purpletear.friendzone4.game.activities.main.MainInterface;
import fr.purpletear.friendzone4.game.activities.main.Phrase;
import purpletear.fr.purpleteartools.GlobalData;

public class GameContactAdapter extends RecyclerView.Adapter<GameContactAdapter.ViewHolder> {
    /**
     * Contains the phrases
     *
     * @see Phrase
     */
    private List<Contact> array;

    private RequestManager Glide;

    private MainInterface callBack;

    private Context context;

    public GameContactAdapter(Context c, RequestManager r, List<Contact> array, MainInterface callBack) {
        this.context = c;
        this.Glide = r;
        this.array = array;
        this.callBack = callBack;
    }

    @Override
    public int getItemCount() {
        return array.size();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflated;
        switch (Contact.Type.values()[viewType]) {
            case ACTION:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_layout_contacts_row_action, parent, false);
                break;
            case SIMPLE:
                inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.fz4_layout_contacts_row_simple, parent, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown Contact.Type in GameConversationAdapter.onCreateViewHolder " + Contact.Type.values()[viewType]);
        }
        return new ViewHolder(inflated);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Contact c = array.get(position);
        holder.display(c);
    }

    @Override
    public int getItemViewType(int position) {
        if (array.get(position).getType() == Contact.Type.ACTION) {
            return Contact.Type.ACTION.ordinal();
        }
        return Contact.Type.SIMPLE.ordinal();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.image != null) {
            Glide.clear(holder.image);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;

        ViewHolder(View itemView) {
            super(itemView);
        }

        void display(final Contact c) {
            switch (Contact.Type.values()[getItemViewType()]) {
                case ACTION: {
                    Glide.load(OnlineAssetsManager.INSTANCE.getImageFilePath(context, GlobalData.Game.FRIENDZONE4.getId(), c.getDrawable()))
                            .into(image = itemView.findViewById(R.id.fz4_action_contacts_row_image));
                    ((TextView) itemView.findViewById(R.id.fz4_action_contacts_row_name)).setText(c.getName());
                    ((TextView) itemView.findViewById(R.id.fz4_action_contacts_row_text)).setText(c.getText());
                    if(c.isBlurred()) {
                        itemView.setAlpha(.3f);
                    }
                    itemView.findViewById(R.id.fz4_contact_row_action_clickable).setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    callBack.onClickContact(c.getCode(), Contact.Type.ACTION);
                                }
                            }
                    );

                    break;
                }
                case SIMPLE: {
                    Glide.load(OnlineAssetsManager.INSTANCE.getImageFilePath(context, GlobalData.Game.FRIENDZONE4.getId(), c.getDrawable()))
                            .into(image = itemView.findViewById(R.id.fz4_simple_contacts_row_image));
                    ((TextView) itemView.findViewById(R.id.fz4_simple_contacts_row_name)).setText(c.getName());
                    ((TextView) itemView.findViewById(R.id.fz4_simple_contacts_row_text)).setText(c.getText());
                    if(c.isBlurred()) {
                        itemView.setAlpha(.3f);
                    }
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unknown Phrase.Type in GameConversationAdapter.ViewHolder.display");
            }
        }
    }
}

package friendzone3.purpletear.fr.friendzon3;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.sutokosharedelements.OnlineAssetsManager;

import friendzone3.purpletear.fr.friendzon3.custom.Character;
import purpletear.fr.purpleteartools.Finger;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;

public class PoeticActivity extends AppCompatActivity {

    private TableOfSymbols symbols;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fz3_activity_poetic);
        load();
    }

    private void load() {
        symbols = getIntent().getParcelableExtra("symbols");
        setText();
        listeners();
        setImage();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void listeners() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                play();
            }
        };

        Finger.Companion.defineOnTouch(findViewById(R.id.poetic_button_top), runnable);
        Finger.Companion.defineOnTouch(findViewById(R.id.poetic_button_bottom), runnable);
    }

    private void play() {
        setResult(RESULT_OK);
        finish();
    }

    private void setText() {
        TextView t = findViewById(R.id.poetic_text_story);
        t.setText(getText(symbols.getChapterCode(), this));
    }

    private static String getText(String code, Context context) {
        String name = "poetry_starter_"+code;
        int id = context.getResources().getIdentifier(name, "string", context.getPackageName());
        if(id == 0) {
            return "NO TITLE YET";
            // throw new IllegalArgumentException(name + " not found");
        }
        return Character.Companion .updateNames(context, context.getResources().getString(id));
    }

    private void setImage() {
        String imageName = "";
        switch (symbols.getChapterCode()) {
            case "5a" :
                imageName = "lac_aube";
                break;
            case "6a" :
                imageName = "lucie_background";
                break;
            case "7b" :
                imageName = "zoe_photograph";
                break;
        }
        ImageView i = findViewById(R.id.poetic_background_image);
        Glide.with(this).load(
                OnlineAssetsManager.INSTANCE.getImageFilePath(this, GlobalData.Game.FRIENDZONE3.getId(), "friendzone3_lac_aube")
        ).into(i);
    }

}

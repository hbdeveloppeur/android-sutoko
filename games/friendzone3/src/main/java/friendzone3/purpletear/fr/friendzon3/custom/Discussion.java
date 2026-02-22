package friendzone3.purpletear.fr.friendzon3.custom;

import com.example.sharedelements.SutokoSharedElementsData;

import java.io.Serializable;
import java.util.ArrayList;

import friendzone3.purpletear.fr.friendzon3.tables.TableOfCharacters;
import friendzone3.purpletear.fr.friendzon3.tables.TableOfLinks;
import friendzone3.purpletear.fr.friendzon3.tables.TableOfPhrases;
import purpletear.fr.purpleteartools.GlobalData;
import purpletear.fr.purpleteartools.TableOfSymbols;


public class Discussion implements Serializable {
    static int STATUS_TALKING = 1, STATUS_FINISH = 0;
    int status;
    private Phrase root;
    TableOfLinks links;
    public TableOfPhrases phrases;
    public TableOfCharacters characters;
    public TableOfSymbols symbols;

    public Discussion() {
        status = STATUS_TALKING;
        symbols = new TableOfSymbols(GlobalData.Game.FRIENDZONE3.getId());
        links = new TableOfLinks();
        phrases = new TableOfPhrases();
        characters = new TableOfCharacters();
    }

    Phrase getRoot() {
        int id = links.getDest(SutokoSharedElementsData.INSTANCE.getSTARTING_PHRASE_ID()).get(0);
        return phrases.getPhrase(id);
    }

    Phrase getAnswer(int phraseId) {
        ArrayList<Integer> ids = links.getDest(phraseId);
        if(ids.size() == 0) {
            return null;
        }
        int id = ids.get(0);
        return phrases.getPhrase(id);
    }

    ArrayList<Phrase> getAnswers(int phraseId) {
        ArrayList<Phrase> answers = new ArrayList<>();
        ArrayList<Integer> ids = links.getDest(phraseId);

        for(int i = 0; i < ids.size(); i++) {
            int id = ids.get(i);
            answers.add(phrases.getPhrase(id));
        }

        return answers;
    }

}
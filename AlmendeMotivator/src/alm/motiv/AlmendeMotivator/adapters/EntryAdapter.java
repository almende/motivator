package alm.motiv.AlmendeMotivator.adapters;

/**
 * Created by AsterLaptop on 4/13/14.
 */

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import alm.motiv.AlmendeMotivator.Database;
import alm.motiv.AlmendeMotivator.R;
import alm.motiv.AlmendeMotivator.models.ChallengeHeader;
import alm.motiv.AlmendeMotivator.models.Challenge;
import alm.motiv.AlmendeMotivator.models.User;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class EntryAdapter extends ArrayAdapter<Item> {

    private Context context;
    private ArrayList<Item> items;
    private LayoutInflater vi;

    public EntryAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        DatabaseThread db = new DatabaseThread();
        final Item i = items.get(position);
        if (i != null) {
            if (i.isSection()) {
                ChallengeHeader si = (ChallengeHeader) i;
                v = vi.inflate(R.layout.list_item_section, null);

                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
                v.setLongClickable(false);

                final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
                sectionView.setText(si.getTitle());

            } else {
                Challenge ei = (Challenge) i;
                v = vi.inflate(R.layout.list_item_entry, null);
                final TextView title = (TextView) v.findViewById(R.id.list_item_entry_title);
                final TextView challengee = (TextView) v.findViewById(R.id.list_item_entry_summary);
                final TextView status = (TextView) v.findViewById(R.id.list_item_entry_status);


                if (title != null)
                    title.setText(ei.getTitle());
                if (challengee != null)


                    //This gem gives the challengee's facebookID to the databaseThread. The dbThread then runs so we give the Thread 2 seconds to complete. After that we -
                    //set the challengeeName with the result of the thread. Added a while loop so application waits until all names are loaded.
                    db.setChallengeeID(ei.getChallengee());
                db.execute();
                while (db.getChallengeeName() == null) {
                    try {
                        db.get(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    challengee.setText(db.getChallengeeName());
                }
                if (status != null)
                    status.setText(ei.getStatus());
            }
        }
        return v;
    }

    class DatabaseThread extends AsyncTask<String, String, String> {

        private String challengeeName;
        private String challengeeID;

        @Override
        protected String doInBackground(String... strings) {
            MongoClient client = Database.getInstance();
            DB db = client.getDB(Database.uri.getDatabase());
            DBCollection userCollection = db.getCollection("user");
            userCollection.setObjectClass(User.class);

            User curUser = new User();
            curUser.put("facebookID", challengeeID);
            User newUser = (User) userCollection.find(curUser).toArray().get(0);
            setChallengeeName(newUser.getName());
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

        }

        public String getChallengeeName() {
            return challengeeName;
        }

        public void setChallengeeName(String challengeeName) {
            this.challengeeName = challengeeName;
        }

        public String getChallengeeID() {
            return challengeeID;
        }

        public void setChallengeeID(String challengeeID) {
            this.challengeeID = challengeeID;
        }
    }

}

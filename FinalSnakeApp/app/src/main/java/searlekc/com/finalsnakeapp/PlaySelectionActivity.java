/**
 * @author: searlekc
 */
package searlekc.com.finalsnakeapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

/**
 * Governs selection screen
 */
public class PlaySelectionActivity extends AppCompatActivity {

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selection);
        user = (User)getIntent().getSerializableExtra("user");

        TextView greeting = findViewById(R.id.textView);
        greeting.setText("Hello, " + user.getUsername() + ", your high score is...");

        TextView score = findViewById(R.id.textView2);
        score.setText("" + user.getHighScore());

        Button play = findViewById(R.id.play_button);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                i.putExtra("user", user);
                startActivity(i);
            }
        });

        Button leaderBoard = findViewById(R.id.leader_btn);
        leaderBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                makePopUp();
            }
        });

        Button playWithFriend = findViewById(R.id.network_button);
        playWithFriend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChallengeActivity.class);
                i.putExtra("user", user);
                startActivity(i);
            }
        });

        Button watchButton = findViewById(R.id.watch_button);
        watchButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                i.putExtra("user", user);
                i.putExtra("type", true);
                startActivity(i);
            }
        });
    }

    /**
     * Generates popup with table view for leaderboard
     */
    private void makePopUp(){
        final Context context = this;
        View fragment = getLayoutInflater().inflate(R.layout.leaderboard, null);
        RelativeLayout parent = findViewById(R.id.selection_layout);
        final float density = this.getResources().getDisplayMetrics().density;
        TableLayout layout = fragment.findViewById(R.id.tableLayout);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").orderBy("highscore", Query.Direction.DESCENDING).limit(5).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for(DocumentSnapshot doc : documentSnapshots){
                    Map<String, Object> userMap = doc.getData();
                    TableRow row = new TableRow(context);
                    TextView name = new TextView(context);
                    name.setTextColor(Color.WHITE);
                    name.setTextSize(30);
                    name.setText(userMap.get("username").toString());
                    name.setPadding(0, 0, (int)(50*density), 0);
                    TextView score = new TextView(context);
                    score.setTextColor(Color.WHITE);
                    score.setTextSize(30);
                    score.setText(userMap.get("highscore").toString());
                    row.addView(name);
                    row.addView(score);
                    layout.addView(row);
                }
                PopupWindow leaderboard = new PopupWindow(context);
                leaderboard.setHeight(parent.getHeight());
                leaderboard.setWidth(parent.getWidth());
                Button exitButton = fragment.findViewById(R.id.exitButton);
                exitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaderboard.dismiss();
                    }
                });
                leaderboard.setContentView(fragment);
                leaderboard.showAtLocation(getWindow().getDecorView().findViewById(android.R.id.content), Gravity.BOTTOM, 10, 10);
            }
        });
    }
}

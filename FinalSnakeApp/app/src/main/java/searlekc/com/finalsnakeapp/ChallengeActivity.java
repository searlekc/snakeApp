package searlekc.com.finalsnakeapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
/**
 * @author: searlekc
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.toIntExact;

public class ChallengeActivity extends Activity {
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenge_layout);
        user = (User)getIntent().getExtras().get("user");
        fillChallenges();

        EditText nameBox = findViewById(R.id.challengeName);
        Button sendButton = findViewById(R.id.challengeButton);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String username = nameBox.getText().toString();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean found = false;
                        for(DocumentSnapshot doc : task.getResult()){
                            if(doc.getId().equals(username)){
                                found = true;
                                Map<String, Object> challengeMap = new HashMap<>();
                                challengeMap.put("playerOne", user.getUsername());
                                challengeMap.put("playerTwo", username);
                                challengeMap.put("result", "");
                                Random random = new Random();
                                int xSeed = random.nextInt();
                                int ySeed = random.nextInt();
                                challengeMap.put("xSeed", xSeed);
                                challengeMap.put("ySeed", ySeed);
                                challengeMap.put("playerTwoScore", 0);
                                challengeMap.put("playerOneScore", 0);
                                String id = UUID.randomUUID().toString();
                                db.collection("challenges").document(id).set(challengeMap);
                                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                                i.putExtra("user", user);
                                i.putExtra("docID", id);
                                i.putExtra("xSeed", xSeed);
                                i.putExtra("ySeed", ySeed);
                                startActivity(i);
                            }
                        }
                        if(!found) {
                            Toast.makeText(getApplicationContext(), "Please enter a valid username", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        Button exitButton = findViewById(R.id.backButton);
        exitButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PlaySelectionActivity.class);
                i.putExtra("user", user);
                startActivity(i);
            }
        });
    }

    private void fillChallenges(){
        TableLayout layout = findViewById(R.id.challengeTable);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("challenges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot doc : task.getResult()){
                    if(doc.get("playerTwo").equals(user.getUsername()) && doc.get("result").equals("")){
                        TableRow row = new TableRow(getApplicationContext());
                        TextView username = new TextView(getApplicationContext());
                        username.setTextSize(20);
                        username.setText(doc.get("playerOne").toString());
                        username.setTextColor(Color.WHITE);
                        row.addView(username);
                        TextView score = new TextView(getApplicationContext());
                        score.setTextSize(20);
                        score.setText(doc.get("playerOneScore").toString());
                        score.setTextColor(Color.WHITE);
                        row.addView(score);
                        Button accept = new Button(getApplicationContext());
                        accept.setBackground(getDrawable(R.drawable.green_check));
                        TableRow.LayoutParams responseButtons = new TableRow.LayoutParams(dpToPx(40), TableRow.LayoutParams.WRAP_CONTENT);
                        responseButtons.setMargins(dpToPx(20), 0, dpToPx(10), 0);
                        accept.setLayoutParams(responseButtons);
                        accept.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                int xSeed = toIntExact((long)doc.get("xSeed"));
                                int ySeed = toIntExact((long)doc.get("ySeed"));
                                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                                i.putExtra("xSeed", xSeed);
                                i.putExtra("ySeed", ySeed);
                                i.putExtra("user", user);
                                i.putExtra("docID", doc.getId());
                                startActivity(i);
                            }
                        });
                        row.addView(accept);
                        Button decline = new Button(getApplicationContext());
                        decline.setBackground(getDrawable(android.R.drawable.ic_delete));
                        decline.setLayoutParams(responseButtons);
                        decline.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DocumentReference challenge = db.collection("challenges").document(doc.getId());
                                challenge.update("result", "declined");
                            }
                        });
                        row.addView(decline);
                        layout.addView(row);
                    }
                }
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return Math.round((float) dp * density);
    }
}

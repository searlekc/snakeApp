/**
 * @author: searlekc
 */
package searlekc.com.finalsnakeapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import static java.lang.Math.toIntExact;

/**
 * Governs login screen
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login = findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    /**
     * Checks if the user exists in the FireStore database
     * If so, pulls the user and creates the object
     * If not, creates a new user
     */
    private void attemptLogin(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        EditText userInput = findViewById(R.id.username);
        final String userName = userInput.getText().toString();

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean userFound = false;
                        User user = null;
                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> userMap = document.getData();
                            if(userMap.get("username").equals(userName)){
                                user = new User((String)userMap.get("username"), toIntExact((long)userMap.get("highscore")));
                                userFound = true;
                                checkChallenges(user);
                            }
                        }
                        if(!userFound) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("username", userName);
                            userMap.put("highscore", 0);
                            user = new User(userName);
                            db.collection("users").document(userName).set(userMap);
                        }
                        goToSelectionScreen(user);
                    }
                });
    }

    /**
     * Goes to the selection screen
     * @param user user information to pass
     */
    private void goToSelectionScreen(User user){
        Intent i = new Intent(getApplicationContext(), PlaySelectionActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

    /**
     * Checks if the user logging in has any challenges that have been completed since last time
     * Creates notification(s) appropriately
     * @param user login info
     */
    private void checkChallenges(User user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("challenges").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot doc : task.getResult()){
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default");
                    notification.setContentTitle("Snake Update");
                    notification.setSmallIcon(android.R.drawable.ic_dialog_alert);
                    if(doc.get("playerOne").equals(user.getUsername()) && !doc.get("result").equals("")){
                        int p1 = toIntExact((long)doc.get("playerOneScore"));
                        int p2 = toIntExact((long)doc.get("playerTwoScore"));
                        String opponent = doc.get("playerTwo").toString();
                        String result = doc.get("result").toString();
                        if(result.equals(user.getUsername())){
                            notification.setContentText("You defeated " + opponent + ", " + p1 + "-" + p2);
                        }else if(result.equals(opponent)){
                            notification.setContentText("You lost to " + opponent + ", " + p1 +  "-" + p2);
                        }else{
                            notification.setContentTitle("Your game with " + opponent + " was a draw!");
                        }
                        sendNotification(notification);
                        db.collection("challenges").document(doc.getId()).delete();
                    }
                }
            }
        });
    }

    /**
     * Sends notification to the user
     * @param notification
     */
    private void sendNotification(NotificationCompat.Builder notification){
        NotificationManager notifier = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "notification", NotificationManager.IMPORTANCE_DEFAULT);
        notifier.createNotificationChannel(channel);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(0, notification.build());
    }
}

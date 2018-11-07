/**
 * @author: searlekc
 */
package searlekc.com.finalsnakeapp;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Display;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static java.lang.Math.toIntExact;

/**
 * Governs the start of a game
 */
public class GameActivity extends Activity {
    SnakeLogic game;
    User user;
    Thread gameThread;
    static Handler handler;

    @Override
    /**
     * Creates game window and handlers
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User)getIntent().getSerializableExtra("user");
        boolean perfect = getIntent().getBooleanExtra("type", false);
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        int xSeed = getIntent().getIntExtra("xSeed", -1);
        int ySeed = getIntent().getIntExtra("ySeed", -1);

        display.getSize(p);
        if(xSeed == -1 && ySeed == -1) {
            //Creates a handler for normal games and perfect games
            //Only update the score if it was a user playing!
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    try {
                        gameThread.join();
                        if(!perfect) {
                            updateUserScore(msg.arg1);
                        }
                        goToSelectionActivity();
                    }catch(InterruptedException e){

                    }
                }
            };
            game = new SnakeLogic(this, p.x, p.y, handler, perfect);
        }else{
            //Creates a handler for network games
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    try {
                        gameThread.join();
                        updateChallenge(msg.arg1);
                        updateUserScore(msg.arg1);
                        goToSelectionActivity();
                    }catch(InterruptedException e){

                    }
                }
            };
            game = new SnakeLogic(this, p.x, p.y, handler, xSeed, ySeed);
        }
        gameThread = game.startGame();
        setContentView(game);
    }

    /**
     * Updates the challenge information in the database with the score the player earned
     * @param score
     */
    private void updateChallenge(int score){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference challengeDoc = db.collection("challenges").document(getIntent().getExtras().get("docID").toString());
        db.collection("challenges").document(getIntent().getExtras().get("docID").toString()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                if(user.getUsername().equals(doc.get("playerOne").toString())){
                    challengeDoc.update("playerOneScore", score);
                }else {
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "default");
                    notification.setContentTitle("Snake Update");
                    notification.setSmallIcon(android.R.drawable.ic_dialog_alert);
                    challengeDoc.update("playerTwoScore", score);
                    String result;
                    String notificationText;
                    int p1 = toIntExact((long) doc.get("playerOneScore"));

                    if (p1 > score) {
                        result = doc.get("playerOne").toString();
                        notificationText = "You lost to " + doc.get("playerOne").toString() + ", " + score +  "-" + p1;
                    } else if (p1 < score) {
                        notificationText = "You defeated " + doc.get("playerOne").toString() + ", " + score + "-" + p1;
                        result = doc.get("playerTwo").toString();
                    } else {
                        result = "Draw";
                        notificationText = "Your game with " + doc.get("playerOne").toString() + " was a draw!";
                    }
                    challengeDoc.update("result", result);
                    notification.setContentText(notificationText);
                    sendNotification(notification);
                }
            }
        });
    }

    /**
     * Updates the user's score in the database
     * @param score the score the earned
     */
    private void updateUserScore(int score){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference remoteUser = db.collection("users").document(user.getUsername());
        if(user.getHighScore() < score) {
            user.setHighScore(score);
            remoteUser.update("highscore", score);
        }
    }

    /**
     * Sends notification to the player if they just won or lost a challenge
     * @param notification Notification to send
     */
    private void sendNotification(NotificationCompat.Builder notification){
        NotificationManager notifier = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "notification", NotificationManager.IMPORTANCE_DEFAULT);
        notifier.createNotificationChannel(channel);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(0, notification.build());
    }
    
    private void goToSelectionActivity(){
        Intent i = new Intent(this, PlaySelectionActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }
}

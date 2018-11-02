package searlekc.com.finalsnakeapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class GameActivity extends Activity {
    SnakeLogic game;
    User user;
    Thread gameThread;
    static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = (User)getIntent().getSerializableExtra("user");
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                try {
                    gameThread.join();
                    makeToast(msg.arg1);
                    updateUserScore(msg.arg1);
                }catch(InterruptedException e){

                }
            }
        };

        display.getSize(p);
        game = new SnakeLogic(this, p.x, p.y, user, handler);
        gameThread = game.startGame();
        setContentView(game);
    }

    private void makeToast(int score){
        Toast.makeText(this, "" + score, Toast.LENGTH_LONG).show();
    }

    private void updateUserScore(int score){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference remoteUser = db.collection("users").document(user.getUsername());
        if(user.getHighScore() < score) {
            user.setHighScore(score);
            remoteUser.update("highscore", score);
        }
        Intent i = new Intent(this, PlaySelectionActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}

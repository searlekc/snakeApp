package searlekc.com.finalsnakeapp;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import io.realm.Realm;
import io.realm.SyncUser;

public class GameActivity extends Activity {
    SnakeLogic game;
    User user;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        user = (User)getIntent().getSerializableExtra("user");
        Display display = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        game = new SnakeLogic(this, p.x, p.y, user);
        setContentView(game);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SyncUser.current().logOut();
        //realm.close();
    }
}

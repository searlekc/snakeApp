package searlekc.com.finalsnakeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncUser;

public class PlaySelectionActivity extends AppCompatActivity {

    private Realm realm;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_selection);

        setUpRealm();

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
    }

    private void setUpRealm() {
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SyncUser.current().logOut();
        realm.close();
    }
}

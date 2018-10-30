package searlekc.com.finalsnakeapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

public class LoginActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(this);
        setContentView(R.layout.activity_login);

        if(SyncUser.current() != null){
            realm = Realm.getDefaultInstance();
            User foundUser = realm.where(User.class).equalTo("id", SyncUser.current().getIdentity()).findFirst();
            //Object comes back in a proxy format that is not serializable
            User temp = new User(foundUser.getId(), foundUser.getUsername());
            temp.setHighScore(foundUser.getHighScore());
            goToSelectionScreen(temp);
        }

        Button login = findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin(){
        EditText userInput = findViewById(R.id.username);
        final String userName = userInput.getText().toString();
        SyncCredentials login = SyncCredentials.nickname(userName, false);
        SyncUser.logInAsync(login, Constants.AUTH_URL, new SyncUser.Callback<SyncUser>(){
            @Override
            public void onSuccess(SyncUser user){
                realm = Realm.getDefaultInstance();
                RealmResults<User> realms = realm.where(User.class).findAllAsync();
                User foundUser = realm.where(User.class).equalTo("username", userName).findFirst();
                User newUser;
                if(foundUser == null){
                    newUser = new User(user.getIdentity(), userName);
                    realm.executeTransactionAsync(realm -> {
                        realm.insert(newUser);
                    });
                    goToSelectionScreen(newUser);
                }else{
                    User temp = new User();
                    temp.setHighScore(foundUser.getHighScore());
                    temp.setId(foundUser.getId());
                    temp.setUsername(foundUser.getUsername());
                    goToSelectionScreen(temp);
                }


            }

            @Override
            public void onError(ObjectServerError error) {

            }
        });
    }

    private void goToSelectionScreen(User user){
        Intent i = new Intent(getApplicationContext(), PlaySelectionActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SyncUser.current().logOut();
        realm.close();
    }
}

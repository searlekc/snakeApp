package searlekc.com.finalsnakeapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.lang.Math.toIntExact;

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

    private void goToSelectionScreen(User user){
        Intent i = new Intent(getApplicationContext(), PlaySelectionActivity.class);
        i.putExtra("user", user);
        startActivity(i);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}

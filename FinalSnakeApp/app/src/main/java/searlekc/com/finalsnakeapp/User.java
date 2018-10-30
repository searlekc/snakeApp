package searlekc.com.finalsnakeapp;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class User extends RealmObject implements Serializable{
    @Required
    private String username;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @PrimaryKey
    @Required
    private String id;
    private int highScore;

    public User(){
        highScore = 0;
    }

    public User(String id, String username){
        this.username = username;
        this.id = id;
        highScore = 0;
    }

    public int getHighScore() {
        return highScore;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }
}

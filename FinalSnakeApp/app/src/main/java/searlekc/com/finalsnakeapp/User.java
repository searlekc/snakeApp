/**
 * @author: searlekc
 */
package searlekc.com.finalsnakeapp;

import java.io.Serializable;

/**
 * User object to display and track username and score
 */
public class User implements Serializable{
    private String username;
    private int highScore;

    public User(){
        highScore = 0;
    }

    public User(String username){
        this.username = username;
        highScore = 0;
    }

    public User(String username, int highScore){
        this.highScore = highScore;
        this.username = username;
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

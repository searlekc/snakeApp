package searlekc.com.finalsnakeapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class SnakeLogic extends SurfaceView implements Runnable {
    User user;
    private Handler handler;
    private Thread gameThread;
    private Context appContext;
    private enum Direction {UP, DOWN, LEFT, RIGHT};
    private Direction direction = Direction.UP;
    private int screenX;
    private int screenY;
    private int length;
    private int targetX;
    private int targetY;
    private int score;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private final int FPS = 10;
    private int blockSize;
    private final int HEIGHT = 40;
    private int width;
    private ArrayList<Integer> xPositions;
    private ArrayList<Integer> yPositions;
    private long MILLIS_PER_SECOND = 1000;
    private long nextFrameTime;
    public volatile boolean isPlaying;

    private Random randomY;
    private Random randomX;

    @Override
    public void run(){
        while(isPlaying) {
            if (updateRequired()) {
                update();
                draw();
            }
        }
    }

    public SnakeLogic(Context context, int sizeX, int sizeY, User user, Handler handler){
        super(context);
        this.handler = handler;
        this.user = user;

        appContext = context;
        screenX = sizeX;
        screenY = sizeY;
        blockSize = screenY / HEIGHT;
        width = screenX / blockSize;

        xPositions = new ArrayList<>();
        yPositions = new ArrayList<>();

        surfaceHolder = getHolder();
        paint = new Paint();
        randomY = new Random();
        randomX = new Random();

        startGame();
    }

    public Thread startGame()
    {
        xPositions.clear();
        yPositions.clear();
        length = 1;
        xPositions.add(width/2);
        yPositions.add(HEIGHT/2);
        makeTarget();
        score = 0;
        nextFrameTime = System.currentTimeMillis();
        isPlaying = true;
        if(gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
        return gameThread;
    }

    private void makeTarget(){
        targetY = randomY.nextInt(HEIGHT - 1) + 1;
        targetX = randomX.nextInt(width - 1) + 1;
    }

    private void eatTarget(){
        length++;
        xPositions.add(1);
        yPositions.add(1);
        makeTarget();
        score += 10;
    }

    private void moveSnake(){
        for(int i = length-1; i > 0; i--){
            xPositions.set(i, xPositions.get(i - 1));
            yPositions.set(i, yPositions.get(i - 1));
        }

        switch(direction){
            case UP:
                yPositions.set(0, yPositions.get(0)-1);
                break;
            case RIGHT:
                xPositions.set(0, xPositions.get(0)+1);
                break;
            case LEFT:
                xPositions.set(0, xPositions.get(0)-1);
                break;
            case DOWN:
                yPositions.set(0, yPositions.get(0)+1);
                break;
        }
    }

    private boolean checkDeath(){
        boolean dead = false;
        if(xPositions.get(0) == -1 || xPositions.get(0) >= width){
            dead = true;
        }
        if(yPositions.get(0) == -1 || yPositions.get(0) >= HEIGHT){
            dead = true;
        }

        for(int i=1; i<length; i++){
                if (xPositions.get(0) == xPositions.get(i) && yPositions.get(0) == yPositions.get(i)) {
                    dead = true;
                }
        }
        return dead;
    }

    private void update(){
        if(xPositions.get(0) == targetX && yPositions.get(0) == targetY){
            eatTarget();
        }
        
        moveSnake();

        if(checkDeath()){
            isPlaying = false;
            Message msg = new Message();
            msg.arg1 = score;
            handler.sendMessage(msg);
        }
    }

    private void draw(){
        if(surfaceHolder.getSurface().isValid()){
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);

            paint.setTextSize(90);
            canvas.drawText("Score: " + score, 10, 70, paint);

            for(int i=0; i<length; i++){
                canvas.drawRect(xPositions.get(i) * blockSize, yPositions.get(i) * blockSize, xPositions.get(i)*blockSize + blockSize, yPositions.get(i)*blockSize + blockSize, paint);
            }

            paint.setColor(Color.BLUE);

            canvas.drawRect(targetX * blockSize, targetY * blockSize, targetX * blockSize + blockSize, targetY * blockSize + blockSize, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private boolean updateRequired(){
        boolean update = false;
        if(nextFrameTime <= System.currentTimeMillis()){
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND/FPS;
            update = true;
        }
        return update;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch(direction){
                        case UP:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.UP;
                            break;
                    }
                } else {
                    switch(direction){
                        case UP:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }
}

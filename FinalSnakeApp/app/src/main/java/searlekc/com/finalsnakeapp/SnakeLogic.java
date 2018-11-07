/**
 * @author: searlekc
 */
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

/**
 * Handles the game logic
 * Adapted from tutorial found here:
 * http://gamecodeschool.com/android/coding-a-snake-game-for-android/
 */
public class SnakeLogic extends SurfaceView implements Runnable {
    private Handler handler;
    private Thread gameThread;
    private enum Direction {UP, DOWN, LEFT, RIGHT}
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
    private boolean smartGame = false;

    private Random randomY;
    private Random randomX;

    @Override
    public void run(){
        while(isPlaying) {
            if (updateRequired()) {
                if(smartGame){
                    checkSnakeAI();
                }
                update();
                draw();
            }
        }
    }

    /**
     * Constructor for networked game
     * No type needed as this constructor is only for network, never watch logic
     * @param context appContext
     * @param sizeX Width of the screen in pixels
     * @param sizeY Height of the screen in pixels
     * @param handler Messaging service to join threads
     * @param xSeed X-Random generator seed
     * @param ySeed Y-Random generator seed
     */
    public SnakeLogic(Context context, int sizeX, int sizeY, Handler handler, int xSeed, int ySeed){
        super(context);
        randomY = new Random(xSeed);
        randomX = new Random(ySeed);
        setUpGame(sizeX, sizeY, handler);
    }

    /**
     * Constructor for all other game types
     * @param context app context
     * @param sizeX Width of the screen in pixels
     * @param sizeY Height of the screen in pixels
     * @param handler Messaging service to join threads
     * @param type Type of game: True for smart game, false for human player
     */
    public SnakeLogic(Context context, int sizeX, int sizeY, Handler handler, boolean type){
        super(context);
        randomY = new Random();
        randomX = new Random();
        smartGame = type;
        setUpGame(sizeX, sizeY, handler);

    }

    /**
     * Initializes all the common variables for both constructors
     * @param sizeX Width of screen in pixels
     * @param sizeY Height of screen in pixels
     * @param handler Messaging service to join threads
     */
    private void setUpGame(int sizeX, int sizeY, Handler handler){
        this.handler = handler;

        screenX = sizeX;
        screenY = sizeY;
        blockSize = screenY / HEIGHT;
        width = screenX / blockSize;

        xPositions = new ArrayList<>();
        yPositions = new ArrayList<>();

        surfaceHolder = getHolder();
        paint = new Paint();
    }

    /**
     * Starts the game by creating snake and target
     * @return thread the game runs on
     */
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

    /**
     * Gives the target coordinates, resets snake path for smart game
     */
    private void makeTarget(){
        do {
            targetY = randomY.nextInt(HEIGHT - 4) + 2;
            targetX = randomX.nextInt(width - 4) + 2;
        }while(!PathFinder.isEmpty(targetX, targetY, xPositions, yPositions));
        if(smartGame){
            checkSnakeAI();
        }
    }

    /**
     * Increments length of the snake and triggers new target generation
     */
    private void eatTarget(){
        length++;
        xPositions.add(1);
        yPositions.add(1);
        makeTarget();
        score += 10;
    }

    /**
     * Moves the snake properly based on direction enum
     */
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

    /**
     * Checks if the snake has hit a wall or runs into itself
     * @return true if snake is dead
     */
    private boolean checkDeath(){
        boolean dead = false;
        if(xPositions.get(0) == 0 || xPositions.get(0) > width-2){
            dead = true;
        }
        if(yPositions.get(0) < 2 || yPositions.get(0) > HEIGHT-2){
            dead = true;
        }

        for(int i=1; i<length; i++){
                if (xPositions.get(0) == xPositions.get(i) && yPositions.get(0) == yPositions.get(i)) {
                    dead = true;
                }
        }
        return dead;
    }

    /**
     * Triggers snake movement and checks for death
     * If dead returns true, sends a message to the UI thread
     */
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

    /**
     * Draws walls, snake, and score
     */
    private void draw(){
        if(surfaceHolder.getSurface().isValid()){
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.WHITE);
            canvas.drawRect(0, 0, screenX*blockSize, 2*blockSize, paint);
            canvas.drawRect(0, (HEIGHT-1)*blockSize, screenX*blockSize, HEIGHT*blockSize + blockSize, paint);
            canvas.drawRect((width-1)*blockSize, 0, screenX*blockSize, HEIGHT*blockSize+blockSize, paint);
            canvas.drawRect(0, 0, blockSize, HEIGHT*blockSize+blockSize, paint);

            paint.setTextSize(90);
            paint.setColor(Color.BLACK);
            canvas.drawText("Score: " + score, 10, 70, paint);
            paint.setColor(Color.WHITE);

            for(int i=0; i<length; i++){
                canvas.drawRect(xPositions.get(i) * blockSize, yPositions.get(i) * blockSize, xPositions.get(i)*blockSize + blockSize, yPositions.get(i)*blockSize + blockSize, paint);
            }

            paint.setColor(Color.BLUE);

            canvas.drawRect(targetX * blockSize, targetY * blockSize, targetX * blockSize + blockSize, targetY * blockSize + blockSize, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Checks if it's time to update the screen
     * @return True if screen needs an update
     */
    private boolean updateRequired(){
        boolean update = false;
        if(nextFrameTime <= System.currentTimeMillis()){
            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND/FPS;
            update = true;
        }
        return update;
    }

    @Override
    /**
     * Reacts to user touch input.
     * Left side of the screen turns counterclockwise.
     * Right side turns clockwise
     * Disabled if smart game is on
     */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if(!smartGame) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    if (motionEvent.getX() >= screenX / 2) {
                        switch (direction) {
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
                        switch (direction) {
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
        }
        return true;
    }

    /**
     * Runs the pathfinding algorithm to determine where to send the snake
     */
    private void checkSnakeAI(){
        ArrayList<Node> path = PathFinder.findPath(xPositions.get(0), yPositions.get(0), targetX, targetY, xPositions, yPositions, HEIGHT, width);
        int snakeX = xPositions.get(0);
        int snakeY = yPositions.get(0);
        //Path did not find a possibility, so just tell the snake to survive
        if(path == null || path.isEmpty() || path.get(0) == null){
            survive(snakeX, snakeY);
            return;
        }
        //Path found an option, follow the option
        Node next = path.get(0);
        if (next.x > snakeX) {
            direction = Direction.RIGHT;
        }
        if (next.x < snakeX) {
            direction = Direction.LEFT;
        }
        if (next.y > snakeY) {
            direction = Direction.DOWN;
        }
        if (next.y < snakeY) {
            direction = Direction.UP;
        }
        path.remove(0);
    }

    /**
     * The pathfinding failed to find a proper path, just push the snake to an empty square
     * @param snakeX Current X of head
     * @param snakeY Current Y of head
     */
    private void survive(int snakeX, int snakeY){
        switch(direction){
            case UP:
                if(PathFinder.isEmpty(snakeX, snakeY-1, xPositions, yPositions)){
                    return;
                }else{
                    if(PathFinder.isEmpty(snakeX+1, snakeY, xPositions, yPositions)){
                        direction = Direction.RIGHT;
                        return;
                    }
                    if(PathFinder.isEmpty(snakeX-1, snakeY, xPositions, yPositions)){
                        direction = Direction.LEFT;
                        return;
                    }
                }
            case DOWN:
                if(PathFinder.isEmpty(snakeX, snakeY+1, xPositions, yPositions)){
                    return;
                }else{
                    if(PathFinder.isEmpty(snakeX+1, snakeY, xPositions, yPositions)){
                        direction = Direction.RIGHT;
                        return;
                    }
                    if(PathFinder.isEmpty(snakeX-1, snakeY, xPositions, yPositions)){
                        direction = Direction.LEFT;
                        return;
                    }
                }
            case RIGHT:
                if(PathFinder.isEmpty(snakeX+1, snakeY, xPositions, yPositions)){
                    return;
                }else{
                    if(PathFinder.isEmpty(snakeX, snakeY-1, xPositions, yPositions)){
                        direction = Direction.UP;
                        return;
                    }
                    if(PathFinder.isEmpty(snakeX, snakeY+1, xPositions, yPositions)){
                        direction = Direction.DOWN;
                        return;
                    }
                }
            case LEFT:
                if(PathFinder.isEmpty(snakeX-1, snakeY, xPositions, yPositions)){
                    return;
                }else{
                    if(PathFinder.isEmpty(snakeX, snakeY-1, xPositions, yPositions)){
                        direction = Direction.UP;
                        return;
                    }
                    if(PathFinder.isEmpty(snakeX, snakeY+1, xPositions, yPositions)){
                        direction = Direction.DOWN;
                        return;
                    }
                }
        }
    }
}

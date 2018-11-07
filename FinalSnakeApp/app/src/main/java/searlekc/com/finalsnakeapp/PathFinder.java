/**
 * @author: searlekc
 */
package searlekc.com.finalsnakeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Snake pathfinding logic
 */
public class PathFinder {
    /**
     * A* search algorithm to find an open path
     * @param snakeX X position of snake head
     * @param snakeY Y position of snake head
     * @param targetX X position of target
     * @param targetY Y position of target
     * @param xPositions List of snake body X positions
     * @param yPositions List of snake body Y positions
     * @param height Height of screen
     * @param width Width of screen
     * @return List of nodes to hit to get to the target
     */
    public static ArrayList<Node> findPath(int snakeX, int snakeY, int targetX, int targetY, ArrayList<Integer> xPositions, ArrayList<Integer> yPositions, int height, int width){
        ArrayList<Node> closed = new ArrayList<>();
        ArrayList<Node> open = new ArrayList<>();
        Node firstNode = new Node(snakeX, snakeY, null);
        open.add(firstNode);

        while(open.size() != 0){
            Collections.sort(open, new Comparator<Node>(){
                @Override
                public int compare(Node o1, Node o2) {
                    int compare = o1.fScore - o2.fScore;
                    return compare;
                }
            });
            Node currentNode = open.get(0);
            open.remove(currentNode);
            closed.add(currentNode);
            if(currentNode.x == targetX && currentNode.y == targetY) {
                ArrayList<Node> path = new ArrayList<>();
                while(!currentNode.equals(firstNode)){
                    path.add(0, currentNode);
                    currentNode = currentNode.parent;
                }
                return path;
            }else{
                ArrayList<Node> children = new ArrayList<>();
                if(currentNode.x-1 > 0) {
                    children.add(new Node(currentNode.x - 1, currentNode.y, currentNode));
                }
                if(currentNode.x+1 < width) {
                    children.add(new Node(currentNode.x + 1, currentNode.y, currentNode));
                }
                if(currentNode.y-1 > 0) {
                    children.add(new Node(currentNode.x, currentNode.y - 1, currentNode));
                }
                if(currentNode.y+1 < height) {
                    children.add(new Node(currentNode.x, currentNode.y + 1, currentNode));
                }
                for(Node child : children){
                    if(!closed.contains(child)){
                        if(isEmpty(child.x, child.y, xPositions, yPositions)) {
                            child.gScore = currentNode.gScore + 1;
                            child.hScore = (int)(Math.pow((child.x - targetX), 2) + Math.pow(child.y - targetY, 2));
                            child.fScore = child.gScore + child.hScore;
                            boolean shouldAdd = true;
                            for(Node n : open){
                                if(n.x == child.x && n.y == child.y && n.gScore <= child.gScore){
                                    shouldAdd = false;
                                }
                            }
                            if(shouldAdd){
                                open.add(child);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Static helper method to decide of a space is empty
     * @param x X position of snake head
     * @param y Y position of snake head
     * @param xPositions X body position list
     * @param yPositions Y body position list
     * @return True if the space is empty
     */
    public static boolean isEmpty(int x, int y, ArrayList<Integer> xPositions, ArrayList<Integer> yPositions){
        for(int i=0; i<xPositions.size(); i++){
            if(xPositions.get(i) == x && yPositions.get(i) == y){
                return false;
            }
        }
        return true;
    }
}

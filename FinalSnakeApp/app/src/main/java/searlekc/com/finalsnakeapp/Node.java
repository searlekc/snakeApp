/**
 * @author: searlekc
 */

package searlekc.com.finalsnakeapp;

/**
 * Node helper class for search algorithm
 */
public class Node {
        public int x;
        public int y;
        public int gScore;
        public int hScore;
        public int fScore;
        public Node parent;

        public Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            fScore = 0;
            this.parent = parent;
        }

        @Override
        /**
         *
         */
        public boolean equals(Object v){
            boolean equal = false;
            Node other = (Node)v;
            if(other.x == x && other.y == y){
                equal = true;
            }
            return equal;
        }
}

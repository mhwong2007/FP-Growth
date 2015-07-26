import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhwong on 7/26/15.
 */
public class FPTree {
//    int item;                    // stored item's name
    String item;
    int count;                      // count the occurrences of item
    FPTree parent;                  // the parent node
    List<FPTree> children;     // children of current node, numbers of element of array list imply the number of path

    FPTree next;                    // the next node with same item name
    boolean isRoot;                 // indicate if the current node is root

//    public FPTree(int item) {
//        this.item = item;
//        this.count = 1;
//        this.parent = null;
//        this.children = new ArrayList<>();
//        this.next = null;
//        this.isRoot = false;
//    }
public FPTree(String item) {
    this.item = item;
    this.count = 1;
    this.parent = null;
    this.children = new ArrayList<>();
    this.next = null;
    this.isRoot = false;
}
}

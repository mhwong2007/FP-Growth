import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by mhwong on 7/26/15.
 */
public class FPGrowth {

    private String filepath;
    private double minsup;
    private int threshold;
//    private Map<Integer, Integer> fList;
    private Map<String, Integer> fList;
//    private List<Integer> sortedFList;
    private List<String> sortedFList;



    public FPGrowth(String filepath, double minsup) {
        this.filepath = filepath;
        this.minsup = minsup;
        this.fList = new HashMap<>();

        // build frequent item list
        buildFrequentItemList();

        // build fp-tree
//        FPTree fpTree = new FPTree(0);
        FPTree fpTree = new FPTree(null);
        fpTree.isRoot = true;
        buildFPTree(fpTree);
    }

    public FPGrowth() {
        this("/home/mhwong/Desktop/dataset/test.txt", 0.6);    // default file path and minsup
    }

    public FPGrowth(double minsup) {
        this("/home/mhwong/Desktop/dataset/test.txt", minsup);    // default file path
    }

    private void buildFrequentItemList() {
        int lineNum = 0;
        File inputFile = new File(filepath);
        try {
            Scanner lineScanner = new Scanner(inputFile);
            while(lineScanner.hasNextLine()) {
                lineNum++;
                Scanner itemScanner = new Scanner(lineScanner.nextLine());
                itemScanner.useDelimiter("[ ,]+");
                while(itemScanner.hasNext()) {
//                    int item = itemScanner.nextInt();
                    String item = itemScanner.next();
                    if(fList.containsKey(item)) {
                        fList.put(item, fList.get(item) + 1);
                    }
                    else {
                        fList.put(item, 1);
                    }
                }
            }

            // calculate the occurrence threshold of transaction item
            threshold = (int) Math.floor(minsup * lineNum);
//            for(Iterator<Integer> iterator = fList.keySet().iterator(); iterator.hasNext();) {
            for(Iterator<String> iterator = fList.keySet().iterator(); iterator.hasNext();) {
                if(fList.get(iterator.next()) < threshold) {
                    iterator.remove();
                }
            }

            // build a sorted frequent item list
            sortedFList = new ArrayList<>(fList.keySet());
            Collections.sort(sortedFList, (o1, o2) -> {
                // the input is the key of frequent list
                // sort using fList value then key
                if (Integer.compare(fList.get(o1), fList.get(o2)) == 0) { // equal, sort by key in ascending order
//                    return Integer.compare(o1, o2);
                    return o1.compareToIgnoreCase(o2);
                } else {
                    return Integer.compare(fList.get(o2), fList.get(o1)); // sort by value in descending order
                }
            });

//            printDebugMessage();



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void buildFPTree(FPTree fpTree) {
        ArrayList<FPTree> headerTable = new ArrayList<>();
        for(String key: sortedFList) {
//        for(int key: sortedFList) {
            FPTree node = new FPTree(key);
            headerTable.add(node);
        }
        File inputFile = new File(filepath);
//        ArrayList<Integer> transaction = new ArrayList<Integer>();
        ArrayList<String> transaction = new ArrayList<>();
        try {
            Scanner lineScanner = new Scanner(inputFile);
            while(lineScanner.hasNextLine()) {
                Scanner itemScanner = new Scanner(lineScanner.nextLine());
                itemScanner.useDelimiter("[ ,]+");
                while(itemScanner.hasNext()) {
//                    int item = itemScanner.nextInt();
                    String item = itemScanner.next();
                    if(sortedFList.contains(item)) { // is a frequent item
                        transaction.add(item);
                    }
                }

                // sort transaction using index of element in sortedFList
                Collections.sort(transaction, (o1, o2) ->
                        (Integer.compare(sortedFList.indexOf(o1), sortedFList.indexOf(o2))));

                insertTree(fpTree, transaction, headerTable);
                // clear transaction before next line
                transaction.clear();
            }
            printDebugMessage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    private void insertTree(FPTree fpTree, ArrayList<Integer> transaction, ArrayList<FPTree> headerTable) {
    private void insertTree(FPTree fpTree, ArrayList<String> transaction, ArrayList<FPTree> headerTable) {
    // to prevent empty transaction either from recursive call or first call from buildFPTree
        if(transaction.isEmpty()) {
            return;
        }
//        int item = transaction.get(0);
        String item = transaction.get(0);

        FPTree node = null;
        // check if first element of transaction has same name with one of the children of fptree
        boolean found = false;
        for(FPTree child: fpTree.children) {
            if(child.item.compareToIgnoreCase(item) == 0) {
//            if(child.item == item) {
                found = true;
                child.count++;
                node = child;
                break;
            }
        }

        if(!found) {
            node = new FPTree(item);
            node.parent = fpTree;
            fpTree.children.add(node);
            // use an iterator to find the correct header
            Iterator<FPTree> iterator = headerTable.iterator();
            FPTree header = null;
            while(iterator.hasNext()) {
                header = iterator.next();
//                if(header.item == item) {
                if(header.item.compareToIgnoreCase(item) == 0) {
                    break;
                }
            }
            while(header.next != null) {
                header = header.next;
            }
            header.next = node;
        }

        // remove the first element of transaction
        transaction.remove(0);

        // recursively call insertTree, stop at the beginning when transaction has no element
        insertTree(node, transaction, headerTable);


    }

    private void printDebugMessage() {

        // debug message, print sorted item list
        System.out.printf("Threshold: %d\n", threshold);
//        for(int key: sortedFList) {
        for(String key: sortedFList) {
//            System.out.printf("%d -> %d\n", key, fList.get(key));
            System.out.printf("%s -> %d\n", key, fList.get(key));

        }
    }
}

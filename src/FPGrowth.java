import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
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
        ArrayList<FPTree> headerTable = new ArrayList<>();
        buildFPTree(fpTree, headerTable);

        // run fp growth
        HashMap<String, Integer> frequentPatternSet = new HashMap<>();
        fpGrowth(fpTree, "", Integer.MAX_VALUE, headerTable, fList, frequentPatternSet);

        // print frequent pattern
        for(String key: frequentPatternSet.keySet()) {
            System.out.printf("%s\t(%d)\n", key, frequentPatternSet.get(key));
        }
    }

    public FPGrowth() {
        this("/home/mhwong/Desktop/dataset/test.csv", 0.6);    // default file path and minsup
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

    private void buildFPTree(FPTree fpTree, ArrayList<FPTree> headerTable) {

        for(String key: sortedFList) {
//        for(int key: sortedFList) {
            FPTree node = new FPTree(key);
            headerTable.add(node);
        }

        // sort header table
        Collections.sort(headerTable, new HeaderTableComparator(sortedFList));
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

    // the conditional FP Tree insert function
    private void insertTree(FPTree fpTree, ArrayList<String> transaction, int countOfTransaction, ArrayList<FPTree> headerTable) {
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
                child.count += countOfTransaction;
                node = child;
                break;
            }
        }

        if(!found) {
            node = new FPTree(item);
            node.count = countOfTransaction;
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
        insertTree(node, transaction, countOfTransaction, headerTable);


    }

    public void fpGrowth(FPTree fpTree, String alpha, int alphaSupport, ArrayList<FPTree> headerTable, Map<String, Integer> frequentList, HashMap<String, Integer> frequentPatternSet) {
//        // first check if the FPTree contains a single path
//        boolean singlePath = true;
//        int singlePathSupport = 0;
//
//        // a node pointer
//        FPTree currentNode = null;
//
//        // use an array list to store the single path prefix itemset
//        ArrayList<String> singlePathPrefixItemset = new ArrayList<>();
//
//        // the P(single-prefix path) generated pattern set
//        Map<String, Integer> PFreqPatternSet = new HashMap<>();
//
//        // the Q(multipath) generated pattern set
//        Map<String, Integer> QFreqPatternSet = new HashMap<>();
//
//        // the final return pattern set
//        HashMap<String, Integer> finalPatternSet = new HashMap<>();
//
//        // the return pattern set as this is the recursive call
//        Map<String, Integer> returnedPatternSet = null;
//
//        // if the root has more than one child, it is not a single path
//        if(fpTree.children.size() > 1) {
//            singlePath = false;
//        }
//        else {
//            // otherwise, if the root has exactly one child, we need to recursively check children of child
//            // to see if they also have one child
//            currentNode = fpTree.children.get(0);
//            while(true) {
//                // if the current child has more than one child, it is not a single path!
//                if(currentNode.children.size() >1) {
//                    singlePath = false;
//                    break;
//                }
//
//                // otherwise, we store the current item in buffer
//                singlePathPrefixItemset.add(currentNode.item);
//
//                // keep the support of the path
//                singlePathSupport = currentNode.count;
//
//                // if this child has no child, that means this is the end of this path
//                if(currentNode.children.size() == 0) {
//                    break;
//                }
//                currentNode = currentNode.children.get(0);
//            }
//        }
//        FPTree Q;
//        // if the FPtree contains a single path
//        if(singlePath && singlePathSupport >= threshold) {
//            // let Q be the multipath part and replace the top branching node with a null root
//            // if currentNode has no child, then it ist the end of this path
//
//            if(currentNode != null && currentNode.children.size() == 0) {
//                Q = null;
//            }
//            else {
//                Q = currentNode;
//            }
//            Q.parent.children = null;
//            Q.parent = new FPTree(null);
//            Q.parent.isRoot = true;
//
//            // save the path and generate all the possible combination
//            for(int from = 0; from < singlePathPrefixItemset.size(); from++) {
//                for(int to = from + 1; to <= singlePathPrefixItemset.size(); to++) {
//                    List<String> tempList = singlePathPrefixItemset.subList(from, to);
//                    // get the minimum support of nods in templist
//                    int betaMinSup = headerTable.get(headerTable.indexOf(tempList.get(tempList.size()-1))).count;
//                    String beta = "";
//                    for(String temp: tempList) {
//                        beta += temp + " ";
//                    }
//
//                    // concatenate the generated pattern Beta with Alpha(the passing in pattern)
//                    beta = pattern + beta;
//                    PFreqPatternSet.put(beta, betaMinSup);
//                }
//            }
//        }
//        // if it is not a single path, let Q be the tree
//        else {
//            Q = fpTree;
//        }
//
//        // mining the multipart FPTree
//        // for each item in Q ( each item in header table)
//        for(FPTree item: headerTable) {
//            // generate pattern Beta = item + Alpha(the passing in pattern) with support = item's count
//            String beta = "";
//            if(pattern != null && !pattern.isEmpty()) {
//                beta = pattern + " " + item.item;
//            }
//            else {
//                beta = item.item;
//            }
//            int betaSupport = frequentList.get(item.item);
//
//            // put beta pattern into Q Frequent item set
//            QFreqPatternSet.put(beta, betaSupport);
//            // construct beta's conditional pattern base
//            // use a hash map to build flist
//            HashMap<String, Integer> conditionalFList = new HashMap<>();
//
//            // first pass to collect all the items
//            // start from current item until it meets the root
//            FPTree currentPath = item.next;
//            while(currentPath != null) {
//                FPTree currentPathItem = currentPath.parent;
//                while(true) {
//                    if(currentPathItem.isRoot) {
//                        break;
//                    }
//                    if(conditionalFList.containsKey(currentPathItem.item)) {
//                        conditionalFList.put(currentPathItem.item, conditionalFList.get(currentPathItem.item) + 1);
//                    }
//                    else {
//                        conditionalFList.put(currentPathItem.item, 1);
//                    }
//                    currentPathItem = currentPathItem.parent;
//                }
//                currentPath = currentPath.next;
//            }
//
//            // remove non frequent from the conditional pattern base
//            for(Iterator<String> iterator = conditionalFList.keySet().iterator(); iterator.hasNext();) {
//                if(conditionalFList.get(iterator.next()) < threshold) {
//                    iterator.remove();
//                }
//            }
//
//            // build a sorted conditional frequent item list
//            ArrayList<String> conditionalSortedFList = new ArrayList<>(conditionalFList.keySet());
//            Collections.sort(conditionalSortedFList, (o1, o2) -> {
//                // the input is the key of frequent list
//                // sort using fList value then key
//                if (Integer.compare(conditionalFList.get(o1), conditionalFList.get(o2)) == 0) { // equal, sort by key in ascending order
////                    return Integer.compare(o1, o2);
//                    return o1.compareToIgnoreCase(o2);
//                } else {
//                    return Integer.compare(conditionalFList.get(o2), conditionalFList.get(o1)); // sort by value in descending order
//                }
//            });
//
//            // second pass to build the conditional FP tree
//            FPTree conditionalFPTree = new FPTree(null);
//            conditionalFPTree.isRoot = true;
//
//            // first build conditional header table
//            ArrayList<FPTree> conditionalHeaderTable = new ArrayList<>();
//            for(String key: conditionalSortedFList) {
////        for(int key: sortedFList) {
//                FPTree node = new FPTree(key);
//                conditionalHeaderTable.add(node);
//            }
//
//            // sort header table
//            Collections.sort(conditionalHeaderTable, new HeaderTableComparator(conditionalSortedFList));
//
//            ArrayList<String> conditionalTransaction = new ArrayList<>();
//            currentPath = item.next;
//            while(currentPath != null) {
//                FPTree currentPathItem = currentPath.parent;
//                while(true) {
//                    if(currentPathItem.isRoot) {
//                        break;
//                    }
//                    if(conditionalSortedFList.contains(currentPathItem.item)) {
//                        conditionalTransaction.add(currentPathItem.item);
//                    }
//                    currentPathItem = currentPathItem.parent;
//                }
//                // sort conditional transaction using index of element in conditonalSortedFList
//                Collections.sort(conditionalTransaction, (o1, o2) ->
//                        (Integer.compare(conditionalSortedFList.indexOf(o1), conditionalSortedFList.indexOf(o2))));
//
//                insertTree(conditionalFPTree, conditionalTransaction, conditionalHeaderTable);
//                conditionalTransaction.clear();
//                currentPath = currentPath.next;
//            }
//
//            // if conditional FPTree is non empty, call FP-Growth (Tree, Beta) again
//            if(conditionalFPTree.children.size() > 0) {
//                returnedPatternSet = fpGrowth(conditionalFPTree, beta, conditionalHeaderTable, conditionalFList);
//            }
//        }
//
//        // generated return pattern
//        for(String key: PFreqPatternSet.keySet()) {
//            finalPatternSet.put(key, PFreqPatternSet.get(key));
//        }
//        for(String key: QFreqPatternSet.keySet()) {
//            finalPatternSet.put(key, QFreqPatternSet.get(key));
//        }
//
//        for(String key1: PFreqPatternSet.keySet()) {
//            for(String key2: QFreqPatternSet.keySet()) {
//                int support = 0;
//                if(PFreqPatternSet.get(key1) <= QFreqPatternSet.get(key2)) {
//                    support = PFreqPatternSet.get(key1);
//                }
//                else {
//                    support = QFreqPatternSet.get(key2);
//                }
//                finalPatternSet.put(key1 + " " + key2, support);
//            }
//        }
//
//        if(returnedPatternSet != null) {
//            for(String key: returnedPatternSet.keySet()) {
//                finalPatternSet.put(key, returnedPatternSet.get(key));
//            }
//        }
//
//        return finalPatternSet;



        // Did not optimise on single-prefix path FPTree, come back later
        // TODO: Optimise single-prefix path FPTree

        // mining multipath FPTree Q
        // for each item in Q == for each item in Q's header table
        for(FPTree item: headerTable) {
            // generate pattern beta = item + alpha(incoming pattern) with support = item's support
            String beta = "";
            if(alpha != null && !alpha.isEmpty()) {
                beta = alpha + " " + item.item;
            }
            else {
                beta = item.item;
            }
            int support = frequentList.get(item.item);
            int betaSupport = (alphaSupport < support) ? alphaSupport: support;
            // put pattern beta to Q frequent pattern set


            // construct beta's conditional pattern base
            HashMap<String, Integer> conditionalPatternBase = new HashMap<>();

            // first pass to collect all the conditional patterns and their occurrences
            // start from current item until it meets the root
            FPTree currentPath = item.next;
            while(currentPath != null) {
                String conditionalPattern = "";
                int conditionalPatternSupport = currentPath.count;
                FPTree currentPathItem = currentPath.parent;
                while(true) {
                    if(currentPathItem.isRoot) {
                        break;
                    }
                    conditionalPattern = conditionalPattern + " " + currentPathItem.item;
                    currentPathItem = currentPathItem.parent;
                }
                if(!conditionalPattern.isEmpty()) {
                    conditionalPatternBase.put(conditionalPattern, conditionalPatternSupport);
                }
                currentPath = currentPath.next;
            }
//            while(item.next != null) {
//                item = item.next;
//                betaSupport += item.count;
//                String conditionalPattern = "";
//                FPTree conditionalItem = item.parent;
//
//                while(!conditionalItem.isRoot) {
//                    conditionalPattern = conditionalPattern + " " + conditionalItem.item;
//                    conditionalItem = conditionalItem.parent;
//                }
//                if(!conditionalPattern.isEmpty()) {
//                    conditionalPatternBase.put(conditionalPattern, item.count);
//                }
//            }
            frequentPatternSet.put(beta, betaSupport);
            // build conditional FPTree
            FPTree conditionalFPTree = new FPTree(null);
            conditionalFPTree.isRoot = true;
            ArrayList<FPTree> conditionalHeaderTable = new ArrayList<>();

            // first pass to build the conditional frequent list
            // sorted conditional frequent list will be built along with conditional FP Tree
            HashMap<String, Integer> conditionalFList = new HashMap<>();
            for(String conditionalPattern: conditionalPatternBase.keySet()) {
                StringTokenizer tokenizer = new StringTokenizer(conditionalPattern);
                while(tokenizer.hasMoreTokens()) {
                    String singleItem = tokenizer.nextToken();
                    if(conditionalFList.containsKey(singleItem)) {
                        int count = conditionalFList.get(singleItem);
                        count += conditionalPatternBase.get(conditionalPattern);
                        conditionalFList.put(singleItem, count);
                    }
                    else {
                        conditionalFList.put(singleItem, conditionalPatternBase.get(conditionalPattern));
                    }
                }
            }

            buildConditionalFPTree(conditionalFPTree, conditionalHeaderTable, conditionalPatternBase, conditionalFList);

            // if conditional FPTree is not empty, call FPGrowth(conditionalFPTree, beta)
            if(!conditionalFPTree.children.isEmpty()) {
                fpGrowth(conditionalFPTree, beta, betaSupport, conditionalHeaderTable, conditionalFList, frequentPatternSet);
            }
        }
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

    private void buildConditionalFPTree(FPTree conditionalFPTree,
                                        ArrayList<FPTree> conditionalHeaderTable,
                                        HashMap<String, Integer> conditionalPatternBase,
                                        HashMap<String, Integer> conditionalFList) {
        // the conditional pattern base's key store the transaction and value is their occurrence


        // remove the non-frequent from the conditional frequent list
        for(Iterator<String> iterator = conditionalFList.keySet().iterator(); iterator.hasNext();) {
            if(conditionalFList.get(iterator.next()) < threshold) {
                iterator.remove();
            }
        }

        // build a sorted conditional frequent item list
        ArrayList<String> conditionalSortedFList = new ArrayList<>(conditionalFList.keySet());
        Collections.sort(conditionalSortedFList, (o1, o2) -> {
            // the input is the key of frequent list
            // sort using fList value then key
            if (Integer.compare(conditionalFList.get(o1), conditionalFList.get(o2)) == 0) { // equal, sort by key in ascending order
                return o1.compareToIgnoreCase(o2);
            } else {
                return Integer.compare(conditionalFList.get(o2), conditionalFList.get(o1)); // sort by value in descending order
            }
        });

        // create conditional header table
        for(String key: conditionalSortedFList) {
            FPTree node = new FPTree(key);
            conditionalHeaderTable.add(node);
        }

        // sort header table
        Collections.sort(conditionalHeaderTable, new HeaderTableComparator(conditionalSortedFList));

        // second pass to build the conditional FPTree
        ArrayList<String> transaction = new ArrayList<>();
        for(String conditionalPattern: conditionalPatternBase.keySet()) {
            StringTokenizer tokenizer = new StringTokenizer(conditionalPattern);
            while(tokenizer.hasMoreTokens()) {
                String item = tokenizer.nextToken();
                if(conditionalFList.containsKey(item)) { // is a frequent item
                    transaction.add(item);
                }
            }
            // sort transaction using index of element in conditionalSortedFList
            Collections.sort(transaction, (o1, o2) ->
                    (Integer.compare(conditionalSortedFList.indexOf(o1), conditionalSortedFList.indexOf(o2))));

            insertTree(conditionalFPTree, transaction, conditionalPatternBase.get(conditionalPattern), conditionalHeaderTable);

            // clear transaction before next line
            transaction.clear();
        }
    }

    // inner class comparator that sort header table in order against sorted frequent item list
    class HeaderTableComparator implements Comparator<FPTree> {
        List<String> sortedFList;
        HeaderTableComparator(List<String> sortedFList) {
            this.sortedFList = sortedFList;
        }
        @Override
        public int compare(FPTree o1, FPTree o2) {
            return Integer.compare(sortedFList.indexOf(o2.item), sortedFList.indexOf(o1.item));
        }
    }
}

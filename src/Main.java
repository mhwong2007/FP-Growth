/**
 * Created by mhwong on 7/26/15.
 */
public class Main {
    public static void main(String[] args) {
        if(args.length != 3) {
            System.out.println("Usage: java " + Main.class.getCanonicalName() + " input_file minsup minconf");
        }
        else {
            Runtime runtime = Runtime.getRuntime();
            long startTime = System.currentTimeMillis();
            System.out.println("File: " + args[0]);
            System.out.println("MinSup: " + args[1]);
            System.out.println("MinConf: " + args[2]);
            new FPGrowth(args[0], Double.valueOf(args[1]), Double.valueOf(args[2]));
            long endTime = System.currentTimeMillis();
            System.out.println("Execution Time: " + (endTime - startTime)/1000 + "s");
            System.out.println("Execution Time: " + (endTime - startTime) + "ms");
            runtime.gc();
            long memory = runtime.totalMemory() - runtime.freeMemory();
            System.out.println("Used Memory: " + memory + "b");
            System.out.println("Used Memory: " + memory/(1024L*1024L) + "MB");
            System.out.println();
            System.out.println();
        }
    }
}

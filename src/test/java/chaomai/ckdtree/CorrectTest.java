package chaomai.ckdtree;

import java.util.List;
import java.util.Random;


public class CorrectTest {
  private static Random random = new Random();
  private static int dimension = 1;
  private static int range = 0;
  private static int count = 1000000;
  private static int insertThreadCount = 10;
  private static int deleteThreadCount = 10;

  private static double[] generateRandomKey() {
    double[] key = new double[dimension];
    for (int i = key.length - 1; i >= 0; --i) {
      // key[i] = random.nextLong() % range;
      key[i] = (range == 0L) ? random.nextDouble() : random.nextLong() % range;
    }
    return key;
  }

  private static double[][] generateRandomKeys() {
    double[][] keys = new double[count][];
    for (int i = 0; i < count; i++) {
      keys[i] = generateRandomKey();
    }
    return keys;
  }

  public static void main(String[] args) throws InterruptedException {
    final double[][] keys = generateRandomKeys();

    //final KDTreeMapInterface<Integer> tree = new zyx.kdtree.lockfree.KDTreeMap<Integer>(dimension);
    final KDTreeMapInterface<Integer> tree = new KDTreeMap<Integer>(dimension);
    //final KDTreeMapInterface<Integer> tree = new zyx.kdtree.sequential.SequentialKDTreeMap<Integer>(dimension);
    //final KDTreeMapInterface<Integer> tree = new zyx.kdtree.adapter.WluKDTreeAdapter<Integer>(dimension);
    //final KDTreeMapInterface<Integer> tree = new zyx.kdtree.adapter.SkipListAdapter();

    Thread[] insertThreads = new Thread[insertThreadCount];
    for (int i = 0; i < insertThreadCount; i++) {
      final int tid = i;
      final int countPerThread = count / 2 / insertThreadCount;
      insertThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          final int start = tid * countPerThread;
          for (int j = 0; j < countPerThread; j++) {
            if (!tree.insert(keys[start + j], tid)) ;//System.out.println("ERROR insert");
          }
        }
      });
    }
    System.out.println("Stage1: Inserting...");
    for (int i = 0; i < insertThreadCount; i++) {
      insertThreads[i].start();
    }
    for (int i = 0; i < insertThreadCount; i++) {
      insertThreads[i].join();
    }


    for (int i = 0; i < insertThreadCount; i++) {
      final int tid = i;
      final int countPerThread = count / 2 / insertThreadCount;
      insertThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          final int start = count / 2 + tid * countPerThread;
          for (int j = 0; j < countPerThread; j++) {
            tree.insert(keys[start + j], tid);
          }
        }
      });
    }

    Thread[] deleteThreads = new Thread[deleteThreadCount];
    for (int i = 0; i < deleteThreadCount; i++) {
      final int tid = i;
      final int countPerThread = count / 2 / deleteThreadCount;
      deleteThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          final int start = tid * countPerThread;
          for (int j = 0; j < countPerThread; j++) {
            if (!tree.delete(keys[start + j])) ;//System.out.println("ERROR delete");
            //                        double[] target = new double[]{100};
            //                        List<NearestResult> nearest = tree.nearest(target, 3);
            //                        System.out.print(">>>>" + nearest.size());
            //                        for (NearestResult r : nearest) {
            //                            if (r != null)
            //                                System.out.print(r.key[0] + ", " + r.distance);
            //                            else
            //                                System.out.print("null");
            //                        }
          }
        }
      });
    }

    System.out.println("Stage2: Inserting and deleting...");
    for (int i = 0; i < insertThreadCount; i++) {
      insertThreads[i].start();
    }
    for (int i = 0; i < deleteThreadCount; i++) {
      deleteThreads[i].start();
    }
    for (int i = 0; i < insertThreadCount; i++) {
      insertThreads[i].join();
    }
    for (int i = 0; i < deleteThreadCount; i++) {
      deleteThreads[i].join();
    }

    System.out.println("Stage2: Checking...");
    int errorCount = 0;
    for (int i = count / 2 - 1; i >= 0; i--) {
      if (tree.get(keys[i]) != null) errorCount++;
    }
    for (int i = count / 2; i < count; i++) {
      if (tree.get(keys[i]) == null) errorCount++;
    }

    System.out.println("Error count: " + errorCount);

    double[] target = new double[]{100};
    List<NearestResult> nearest = tree.nearest(target, 3);
    System.out.println(nearest);

  }
}

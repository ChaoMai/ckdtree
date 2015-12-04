package chaomai.ckdtree;

//import chaomai.ckdtree.snapshot1.CKDTreeMap;

//import chaomai.ckdtree.snapshot2.CKDTreeMap;

import chaomai.ckdtree.snapshot3.CKDTreeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by chaomai on 11/26/15.
 */
public class CKDTreeMapCommonTest {
  int dimensionSteps = 1;
  int sampleSteps = 20000;
  int threadsSteps = 1;
  int rounds = 10;
  boolean isVerbose = true;

  private void addKeysToCKD(double[][] k, CKDTreeMap ckd) {
    for (int i = 0; i < k.length; i++) {
      ckd.add(k[i], i);
    }
  }

  private void checkKeysInCKDWithRange(double[][] k, int b, int e, CKDTreeMap ckd, boolean isIn) {
    if (isIn) {
      for (int i = b; i < e; ++i) {
        Assert.assertTrue(ckd.contains(k[i]));
      }
    } else {
      for (int i = b; i < e; ++i) {
        Assert.assertFalse(ckd.contains(k[i]));
      }
    }
  }

  private void checkKeysInCKD(double[][] k, CKDTreeMap ckd, boolean isIn) {
    if (isIn) {
      for (double[] key : k) {
        Assert.assertTrue(ckd.contains(key));
      }
    } else {
      for (double[] key : k) {
        Assert.assertFalse(ckd.contains(key));
      }
    }
  }

  private void addOneKey() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k1 = {1};

    ckd.add(k1, 1);
    Assert.assertTrue(ckd.contains(k1));
  }

  private void addMultipleDimensionKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    double[][] k =
        {{1, 2, 3}, {2, 3, 4}, {3, 4, 5}, {4, 5, 6}, {5, 6, 7}, {6, 7, 8}, {7, 8, 9}, {9, 10, 11},
         {11, 12, 13}};

    if (isVerbose) {
      System.out.println("adding");
    }

    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(k.length, ckd.size());
  }

  private void addMultipleDimensionKeys1() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    // at some point, the key of newInternal would equal to its parent's key.
    double[][] k = {{5.305068244152987, 5.084449022627336, 4.155634301794545},
                    {4.658607614580709, 1.112285238547236, 7.6704533893483875},
                    {3.135000004662376, 4.737773994443383, 3.8336349759006993},
                    {1.0351259060545581, 4.21039722994082, 2.4693577126537414},
                    {5.877263378165557, 2.2656014079486053, 0.358466039752825}};

    if (isVerbose) {
      System.out.println("adding");
    }

    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(k.length, ckd.size());
  }

  private void addMultipleDimensionKeys2(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding");
    }

    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());
  }

  private void addMultipleDimensionDuplicateKeys(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    if (isVerbose) {
      System.out.println("adding");
    }

    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples - duplicateCount, ckd.size());
  }

  private void addMultipleDimensionDimensionDuplicateKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    // at some point, the key of newInternal would equal to its parent's key.
    double[][] k = {{5.305068244152987, 5.084449022627336, 4.155634301794545},
                    {4.658607614580709, 1.112285238547236, 7.6704533893483875},
                    {3.135000004662376, 4.737773994443383, 3.8336349759006993},
                    {1.0351259060545581, 4.21039722994082, 2.4693577126537414},
                    {5.877263378165557, 2.2656014079486053, 0.358466039752825}};

    k[0][0] = k[1][0];
    k[0][1] = k[1][1];

    k[3][1] = k[4][1];
    k[3][2] = k[4][2];

    if (isVerbose) {
      System.out.println("adding");
    }

    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(k.length, ckd.size());
  }

  @Test
  public void testSingleThreadAdd() throws Exception {
    if (isVerbose) {
      System.out.println("\nadd One Key");
    }

    addOneKey();

    for (int i = 1; i <= rounds; ++i) {
      if (isVerbose) {
        System.out.println("\nround " + i);
      }

      int samples = i * sampleSteps;
      int dimension = i * dimensionSteps;

      if (isVerbose) {
        System.out.println(
            String.format("\nadd Multiple (%d) Dimension (%d) Keys", dimension, samples));
      }
      addMultipleDimensionKeys2(samples, dimension);

      if (isVerbose) {
        System.out.println(
            String.format("\nadd Multiple (%d) Dimension Duplicate (%d) Keys", dimension, samples));
      }
      addMultipleDimensionDuplicateKeys(samples, dimension);
    }

    if (isVerbose) {
      System.out.println("\nadd Multiple Dimension Keys");
    }
    addMultipleDimensionKeys();

    if (isVerbose) {
      System.out.println("\nadd Multiple Dimension Keys1");
    }
    addMultipleDimensionKeys1();

    if (isVerbose) {
      System.out.println("\nadd Multiple Dimension, Dimension Duplicate Keys");
    }
    addMultipleDimensionDimensionDuplicateKeys();
  }

  private void invokeAndWait(ArrayList<Callable<Integer>> taskList) {
    ExecutorService executor = Executors.newFixedThreadPool(taskList.size());

    try {
      executor.invokeAll(taskList);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void startInsertWork(int threads, double[][] k, CKDTreeMap ckd) {
    ArrayList<Callable<Integer>> taskList = new ArrayList<>();

    int workPerThread = k.length / threads;

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k[workIndex + j], j);
        }
        return 0;
      });
    }

    invokeAndWait(taskList);
  }

  private void multithreadAddOneDimensionDuplicateKeys(int samples, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, 1);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    if (isVerbose) {
      System.out.println("adding");
    }

    startInsertWork(threads, k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples - duplicateCount, ckd.size());
  }

  private void multithreadAddMultipleDimensionKeys(int samples, int dimension, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding");
    }

    startInsertWork(threads, k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());
  }

  private void multithreadAddMultipleDimensionDuplicateKeys(int samples, int dimension,
                                                            int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    if (isVerbose) {
      System.out.println("adding");
    }

    startInsertWork(threads, k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples - duplicateCount, ckd.size());
  }

  private void multithreadAddHighContention() {
    int dimension = 2;
    int samples = 20000;
    int threads = 40;

    for (int i = 0; i < rounds; ++i) {
      if (isVerbose) {
        System.out.println(String.format("\nHigh Contention, round %d", i));
      }

      CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

      if (isVerbose) {
        System.out.println("generating keys");
      }

      double[][] k = Utilities.generateRandomArrays(samples, dimension);

      if (isVerbose) {
        System.out.println("adding");
      }

      startInsertWork(threads, k, ckd);
      checkKeysInCKD(k, ckd, true);
      Assert.assertEquals(samples, ckd.size());
    }
  }

  @Test
  public void testMultithreadAdd() throws Exception {
    for (int i = 1; i <= rounds; ++i) {
      if (isVerbose) {
        System.out.println("\nround " + i);
      }
      int samples = i * sampleSteps;
      int dimension = i * dimensionSteps;
      int threads = i * threadsSteps;

      if (isVerbose) {
        System.out.println(
            String.format("\nMultiple (%d) threads add One Dimension Duplicate (%d) Keys", threads,
                          samples));
      }
      multithreadAddOneDimensionDuplicateKeys(samples, threads);

      if (isVerbose) {
        System.out.println(
            String.format("\nMultiple (%d) threads add Multiple (%d) Dimension (%d) Keys", threads,
                          dimension, samples));
      }
      multithreadAddMultipleDimensionKeys(samples, dimension, threads);

      if (isVerbose) {
        System.out.println(
            String.format("\nMultiple (%d) threads add Multiple (%d) Dimension Duplicate (%d) Keys",
                          threads, dimension, samples));
      }
      multithreadAddMultipleDimensionDuplicateKeys(samples, dimension, threads);
    }

    if (isVerbose) {
      System.out.println("\n High Contention Case");
    }
    multithreadAddHighContention();
  }

  private void deleteKeysFromCKD(double[][] k, CKDTreeMap ckd) {
    for (double[] key : k) {
      ckd.remove(key);
    }
  }

  private void deleteOneDimensionKeys(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, 1);

    if (isVerbose) {
      System.out.println("adding");
    }
    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    deleteKeysFromCKD(k, ckd);
    checkKeysInCKD(k, ckd, false);
    Assert.assertEquals(0, ckd.size());
  }

  private void deleteMultipleDimensionKeys(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding");
    }
    addKeysToCKD(k, ckd);
    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    deleteKeysFromCKD(k, ckd);
    checkKeysInCKD(k, ckd, false);
    Assert.assertEquals(0, ckd.size());
  }

  private void multithreadDeleteMultipleDimensionKeys(int samples, int dimension, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    int workPerThread = samples / 2 / threads;

    ArrayList<Callable<Integer>> taskList = new ArrayList<>();

    // add first 1/2 samples
    if (isVerbose) {
      System.out.println("adding");
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k[workIndex + j], j);
        }
        return 0;
      });
    }

    invokeAndWait(taskList);

    checkKeysInCKDWithRange(k, 0, samples / 2, ckd, true);
    checkKeysInCKDWithRange(k, samples / 2, samples, ckd, false);
    Assert.assertEquals(samples / 2, ckd.size());

    taskList.clear();

    // delete first 1/2 samples and add second 1/2 samples
    if (isVerbose) {
      System.out.println("deleting and adding");
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.remove(k[workIndex + j]);
        }
        return 0;
      });
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread + samples / 2;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k[workIndex + j], j);
        }
        return 0;
      });
    }

    invokeAndWait(taskList);

    System.out.println(String.format("%d, %d", samples / 2, ckd.size()));
    checkKeysInCKDWithRange(k, 0, samples / 2, ckd, false);
    checkKeysInCKDWithRange(k, samples / 2, samples, ckd, true);
    Assert.assertEquals(samples / 2, ckd.size());
  }

  @Test
  public void testDelete() throws Exception {
    for (int i = 1; i <= rounds; ++i) {
      if (isVerbose) {
        System.out.println("\nround " + i);
      }

      int samples = i * sampleSteps;
      int dimension = i * dimensionSteps;
      int threads = i * threadsSteps;

      if (isVerbose) {
        System.out.println(String.format("\ndelete One Dimension (%d) Keys", samples));
      }
      deleteOneDimensionKeys(samples);

      if (isVerbose) {
        System.out.println(
            String.format("\ndelete Multiple (%d) Dimension (%d) Keys", dimension, samples));
      }
      deleteMultipleDimensionKeys(samples, dimension);

      if (isVerbose) {
        System.out.println(
            String.format("\nMultiple (%d) threads delete Multiple Dimension (%d) Keys", threads,
                          samples));
      }
      multithreadDeleteMultipleDimensionKeys(samples, dimension, threads);
    }
  }

  private void snapshotOnOneDimensionCKD(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k1 = Utilities.generateRandomArrays(samples, 1);
    double[][] k2 = Utilities.generateRandomArrays(samples, 1);

    addKeysToCKD(k1, ckd);

    // check ckd
    checkKeysInCKD(k1, ckd, true);

    CKDTreeMap<Integer> snapshot = ckd.snapshot();

    addKeysToCKD(k2, ckd);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);

    //    Assert.assertEquals(samples, snapshot.size());

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, true);

    // check snapshot again
    checkKeysInCKD(k1, snapshot, true);
  }

  private void snapshotOnMultipleDimensionCKD(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k1 = Utilities.generateRandomArrays(samples, dimension);
    double[][] k2 = Utilities.generateRandomArrays(samples, dimension);

    addKeysToCKD(k1, ckd);

    // check ckd
    checkKeysInCKD(k1, ckd, true);

    CKDTreeMap<Integer> snapshot = ckd.snapshot();

    addKeysToCKD(k2, ckd);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, true);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);
  }

  private void multithreadSnapshotOnMultipleDimensionCKD(int samples, int threads, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    ArrayList<Callable<Integer>> taskList = new ArrayList<>();

    int workPerThread = samples / threads;

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = workIndex; j < workIndex + workPerThread; ++j) {
          ckd.add(k[j], j);
        }
        return 0;
      });
    }

    taskList.add(() -> {
      for (int i = 0; i < threads; ++i) {
        ckd.snapshot();
        TimeUnit.MICROSECONDS.sleep(50);
      }
      return 0;
    });

    try {
      executor.invokeAll(taskList);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    try {
      executor.shutdown();
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // check ckd
    //    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());
  }

  @Test
  public void testSnapshot() throws Exception {
    for (int i = 1; i <= rounds; ++i) {
      if (isVerbose) {
        System.out.println("\nround " + i);
      }

      int samples = i * sampleSteps;
      int dimension = i * dimensionSteps;
      int threads = i * threadsSteps;

      if (isVerbose) {
        System.out.println(String.format("\nsnapshot On One Dimension (%d) Keys", samples));
      }
      snapshotOnOneDimensionCKD(samples);

      if (isVerbose) {
        System.out.println(
            String.format("\nsnapshot On Multiple (%d) Dimension (%d) Keys", dimension, samples));
      }
      snapshotOnMultipleDimensionCKD(samples, dimension);

      if (isVerbose) {
        System.out.println(
            String.format("\nmultithread (%d) Snapshot On Multiple (%d) Dimension (%d) Keys",
                          threads, dimension, samples));
        multithreadSnapshotOnMultipleDimensionCKD(samples, threads, dimension);
      }
    }
  }

  private void simpleKeysIteration() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    // at some point, the key of newInternal would equal to its parent's key.
    double[][] k = {{5.305068244152987, 5.084449022627336, 4.155634301794545},
                    {4.658607614580709, 1.112285238547236, 7.6704533893483875},
                    {3.135000004662376, 4.737773994443383, 3.8336349759006993},
                    {1.0351259060545581, 4.21039722994082, 2.4693577126537414},
                    {5.877263378165557, 2.2656014079486053, 0.358466039752825}};

    addKeysToCKD(k, ckd);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(k.length, ckd.size());

    if (isVerbose) {
      for (Map.Entry<double[], Integer> l : ckd) {
        Utilities.printEntry(l);
      }
    }
  }

  private void randomKeysIteration() {
    int dimension = 10;
    int samples = 500;
    int threads = 4;

    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    int workPerThread = samples / threads;
    startInsertWork(threads, k, ckd);

    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    ArrayList<Map.Entry<double[], Integer>> list = new ArrayList<>();

    for (Map.Entry<double[], Integer> l : ckd) {
      if (isVerbose) {
        Utilities.printEntry(l);
      }
      list.add(l);
    }

    Assert.assertEquals(samples, list.size());
  }

  @Test
  public void testIterator() throws Exception {
    simpleKeysIteration();
    randomKeysIteration();
  }

  @Test
  public void testSpecial() {
    for (int i = 0; i < 500; ++i) {
      try {
        testDelete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
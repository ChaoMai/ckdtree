package chaomai.ckdtree.snapshot1;

import chaomai.ckdtree.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by chaomai on 11/3/15.
 */

// note!!!!!!!!!
// double[][] k1 = Utilities.generateRandomArrays(samples, dimension);
// double[][] k2 = Utilities.generateRandomArrays(samples, dimension);
// ...
// some test is based on this assumption:
// unless Utilities.makeDuplicateKeys(k1) is called,
// all keys in k1, k2, ... are NOT duplicated.

public class CKDTreeMapTest {
  int dimensionSteps = 1;
  int sampleSteps = 20000;
  int threadsSteps = 10;
  int rounds = 5;
  double delta = 0.001;
  boolean isVerbose = true;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    InternalNode<Integer> root = ckd.RDCSS_READ_ROOT();

    Assert.assertEquals(Double.POSITIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
  }

  private void searchDummy() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k = {Double.POSITIVE_INFINITY};

    SearchRes<Integer> res1 = ckd.search(k);

    Assert.assertTrue(ckd.contains(k));
  }

  @Test
  public void testSearch() throws Exception {
    searchDummy();
  }

  private void addKeysToCKD(double[][] k, CKDTreeMap ckd) {
    for (int i = 0; i < k.length; i++) {
      ckd.add(k[i], i);
    }
  }

  private void checkKeysInCKD(double[][] k, CKDTreeMap ckd, boolean isIn) {
    if (isIn) {
      for (double[] key : k) {
        Assert.assertTrue(ckd.contains(key));

        SearchRes<Integer> res = ckd.search(key);
        Assert.assertNotEquals(null, res);

        Assert.assertArrayEquals(key, res.l.key, delta);
      }
    } else {
      for (double[] key : k) {
        Assert.assertFalse(ckd.contains(key));

        SearchRes<Integer> res = ckd.search(key);
        Assert.assertNotEquals(null, res);
      }
    }
  }

  private void addOneKey() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k1 = {1};
    ckd.add(k1, 1);

    Assert.assertTrue(ckd.contains(k1));

    SearchRes<Integer> res = ckd.search(k1);
    Assert.assertNotEquals(null, res);

    Assert.assertArrayEquals(k1, res.l.key, delta);
  }

  private void addOneDimensionKeys(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilities.generateRandomArrays(samples, 1);

    addKeysToCKD(k, ckd);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples, ckd.size());
  }

  private void addOneDimensionDuplicateKeys(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilities.generateRandomArrays(samples, 1);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    addKeysToCKD(k, ckd);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples - duplicateCount, ckd.size());
  }

  private void addMultipleDimensionKeys() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    double[][] k =
        {{1, 2, 3}, {2, 3, 4}, {3, 4, 5}, {4, 5, 6}, {5, 6, 7}, {6, 7, 8}, {7, 8, 9}, {9, 10, 11},
         {11, 12, 13}};

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

    addKeysToCKD(k, ckd);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(k.length, ckd.size());
  }

  private void addMultipleDimensionKeys2(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    addKeysToCKD(k, ckd);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples, ckd.size());
  }

  private void addMultipleDimensionDuplicateKeys(int samples, int dimension) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

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
        System.out.println(String.format("\nadd One Dimension (%d) Keys", samples));
      }
      addOneDimensionKeys(samples);

      if (isVerbose) {
        System.out.println(String.format("\nadd One Dimension Duplicate (%d) Keys", samples));
      }
      addOneDimensionDuplicateKeys(samples);

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
      System.out.println("\nadd Special Key Sequences");
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

  private void addInsertWorkToThreads(Thread[] ts, double[][] k, CKDTreeMap ckd,
                                      int workPerThread) {
    for (int i = 0; i < ts.length; ++i) {
      final int workIndex = i * workPerThread;
      ts[i] = new Thread(() -> {
        for (int j = workIndex; j < workIndex + workPerThread; ++j) {
          ckd.add(k[j], j);
        }
      });
    }
  }

  private void startThreads(Thread[] ts) {
    for (Thread t : ts) {
      t.start();
    }

    for (Thread t : ts) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void multithreadAddOneDimensionKeys(int samples, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilities.generateRandomArrays(samples, 1);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples, ckd.size());
  }

  private void multithreadAddOneDimensionDuplicateKeys(int samples, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilities.generateRandomArrays(samples, 1);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples - duplicateCount, ckd.size());
  }

  private void multithreadAddMultipleDimensionKeys(int samples, int dimension, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples, ckd.size());
  }

  private void multithreadAddMultipleDimensionDuplicateKeys(int samples, int dimension,
                                                            int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);
    int duplicateCount = Utilities.makeDuplicateKeys(k);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

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
      double[][] k = Utilities.generateRandomArrays(samples, dimension);

      Thread[] ts = new Thread[threads];
      int workPerThread = samples / threads;

      addInsertWorkToThreads(ts, k, ckd, workPerThread);

      startThreads(ts);

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
            String.format("\nMultiple (%d) threads add One Dimension (%d) Keys", threads, samples));
      }
      multithreadAddOneDimensionKeys(samples, threads);

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
      System.out.println("\nadd Special Key Sequences");
    }

    if (isVerbose) {
      System.out.println("\n High Contention Case");
    }
    multithreadAddHighContention();
  }

  private void deleteKeysFromCKD(double[][] k, CKDTreeMap ckd) {
    for (int i = 0; i < k.length; i++) {
      ckd.remove(k[i]);
    }
  }

  private void deleteOnekey() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    double[] k1 = {1};

    if (isVerbose) {
      System.out.println("adding");
    }
    ckd.add(k1, 1);

    Assert.assertTrue(ckd.contains(k1));

    SearchRes<Integer> res = ckd.search(k1);
    Assert.assertNotEquals(null, res);

    Assert.assertArrayEquals(k1, res.l.key, delta);


    if (isVerbose) {
      System.out.println("deleting");
    }
    ckd.remove(k1);

    Assert.assertFalse(ckd.contains(k1));

    SearchRes<Integer> res1 = ckd.search(k1);
    InternalNode<Integer> root = ckd.RDCSS_READ_ROOT();

    Assert.assertEquals(null, res1.gp);
    Assert.assertEquals(root, res1.p);
    Assert.assertEquals(root.left, res1.l);
  }

  private void deleteOneDimensionKeys(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
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

  private void addDeleteWorkToThreads(Thread[] ts, double[][] k, CKDTreeMap ckd,
                                      int workPerThread) {
    for (int i = 0; i < ts.length; ++i) {
      final int workIndex = i * workPerThread;
      ts[i] = new Thread(() -> {
        for (int j = workIndex; j < workIndex + workPerThread; ++j) {
          ckd.remove(k[j]);
        }
      });
    }
  }

  private void multithreadDeleteOneDimensionKeys(int samples, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k = Utilities.generateRandomArrays(samples, 1);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    if (isVerbose) {
      System.out.println("adding");
    }
    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    addDeleteWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    Assert.assertEquals(0, ckd.size());
  }

  private void multithreadDeleteMultipleDimensionKeys(int samples, int dimension, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    if (isVerbose) {
      System.out.println("adding");
    }
    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    addDeleteWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    Assert.assertEquals(0, ckd.size());
  }

  @Test
  public void testDelete() {
    if (isVerbose) {
      System.out.println("\ndelete One Key");
    }

    deleteOnekey();

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
            String.format("\nMultiple (%d) threads delete One Dimension (%d) Keys", threads,
                          samples));
      }
      multithreadDeleteOneDimensionKeys(samples, threads);

      if (isVerbose) {
        System.out.println(
            String.format("\nMultiple (%d) threads delete Multiple Dimension (%d) Keys", threads,
                          samples));
      }
      multithreadDeleteMultipleDimensionKeys(samples, dimension, threads);
    }
  }

  private void snapshotOnEmptyCKD(int samples) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    CKDTreeMap<Integer> snapshot = ckd.snapshot();

    double[][] k = Utilities.generateRandomArrays(samples, 1);

    addKeysToCKD(k, ckd);

    // check snapshot
    Assert.assertEquals(0, snapshot.size());

    InternalNode<Integer> root = snapshot.RDCSS_READ_ROOT();

    Assert.assertEquals(Double.POSITIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
    Assert.assertEquals(null, root.left.left);
    Assert.assertEquals(null, root.left.right);

    // check ckd
    checkKeysInCKD(k, ckd, true);
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

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, true);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
  }

  private void multithreadUpdateSnapshotOnOneDimensionCKD(int samples, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[][] k1 = Utilities.generateRandomArrays(samples, 1);
    double[][] k2 = Utilities.generateRandomArrays(samples, 1);
    double[][] k3 = Utilities.generateRandomArrays(samples, 1);
    double[][] k4 = Utilities.generateRandomArrays(samples, 1);
    double[][] k5 = Utilities.generateRandomArrays(samples, 1);

    // add k1 to ckd
    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k1, ckd, workPerThread);

    startThreads(ts);

    // check ckd
    checkKeysInCKD(k1, ckd, true);

    CKDTreeMap<Integer> snapshot = ckd.snapshot();

    // add k2 to ckd
    addInsertWorkToThreads(ts, k2, ckd, workPerThread);

    startThreads(ts);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, true);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);

    // add k3 to snapshot
    addInsertWorkToThreads(ts, k3, snapshot, workPerThread);

    startThreads(ts);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);
    checkKeysInCKD(k3, snapshot, true);

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, true);
    checkKeysInCKD(k3, ckd, false);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);
    checkKeysInCKD(k3, snapshot, true);

    // delete k3 in snapshot, delete k2 in ckd, insert k4 in snapshot and inset k5 in ckd
    addDeleteWorkToThreads(ts, k3, snapshot, workPerThread);

    Thread[] ts1 = new Thread[threads];
    addDeleteWorkToThreads(ts1, k2, ckd, workPerThread);

    Thread[] ts2 = new Thread[threads];
    addInsertWorkToThreads(ts2, k4, snapshot, workPerThread);

    Thread[] ts3 = new Thread[threads];
    addInsertWorkToThreads(ts3, k5, ckd, workPerThread);

    Thread[] ats = new Thread[threads * 4];

    for (int i = 0; i < threads; ++i) {
      ats[i] = ts[i];
    }

    for (int i = 0; i < threads; ++i) {
      ats[i + threads] = ts1[i];
    }

    for (int i = 0; i < threads; ++i) {
      ats[i + threads * 2] = ts2[i];
    }

    for (int i = 0; i < threads; ++i) {
      ats[i + threads * 3] = ts3[i];
    }

    startThreads(ats);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);
    checkKeysInCKD(k3, snapshot, false);
    checkKeysInCKD(k4, snapshot, true);
    checkKeysInCKD(k5, snapshot, false);

    // check ckd
    checkKeysInCKD(k1, ckd, true);
    checkKeysInCKD(k2, ckd, false);
    checkKeysInCKD(k3, ckd, false);
    checkKeysInCKD(k4, ckd, false);
    checkKeysInCKD(k5, ckd, true);

    // check snapshot
    checkKeysInCKD(k1, snapshot, true);
    checkKeysInCKD(k2, snapshot, false);
    checkKeysInCKD(k3, snapshot, false);
    checkKeysInCKD(k4, snapshot, true);
    checkKeysInCKD(k5, snapshot, false);
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
      snapshotOnEmptyCKD(samples);

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
            String.format("\nmultithread Update Snapshot On Multiple (%d) Dimension (%d) Keys",
                          dimension, samples));
        multithreadUpdateSnapshotOnOneDimensionCKD(samples, threads);
      }
    }
  }

  private void printEntry(Map.Entry<double[], Integer> l) {
    System.out.println(
        String.format("<[%s], %d>", Utilities.KeyToString(l.getKey()), l.getValue()));
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
        printEntry(l);
      }
    }
  }

  private void randomKeysIteration() {
    int dimension = 10;
    int samples = 500;
    int threads = 4;

    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);
    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    Thread[] ts = new Thread[threads];
    int workPerThread = samples / threads;

    addInsertWorkToThreads(ts, k, ckd, workPerThread);

    startThreads(ts);

    checkKeysInCKD(k, ckd, true);

    Assert.assertEquals(samples, ckd.size());

    ArrayList<Map.Entry<double[], Integer>> list = new ArrayList<>();

    for (Map.Entry<double[], Integer> l : ckd) {
      if (isVerbose) {
        printEntry(l);
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
}
package chaomai.ckdtree;

import chaomai.ckdtree.snapshot1.CKDTreeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;

//import chaomai.ckdtree.snapshot2.CKDTreeMap;

//import chaomai.ckdtree.snapshot3.CKDTreeMap;

/**
 * Created by chaomai on 11/26/15.
 */
public class CKDTreeMapCommonTest {
  int dimensionSteps = 1;
  int sampleSteps = 20000;
  int threadsSteps = 1;
  int rounds = 10;
  boolean isVerbose = true;

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

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
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

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
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

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
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

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
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

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
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

    Utilities.invokeAndWait(taskList);
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
    Utilities.checkKeysInCKD(k, ckd, true);
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
    Utilities.checkKeysInCKD(k, ckd, true);
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
    Utilities.checkKeysInCKD(k, ckd, true);
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
      Utilities.checkKeysInCKD(k, ckd, true);
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
    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    deleteKeysFromCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, false);
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
    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("deleting");
    }
    deleteKeysFromCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, false);
    Assert.assertEquals(0, ckd.size());
  }

  private void multithreadDeleteMultipleDimensionKeys(int samples, int dimension, int threads) {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k1 = Utilities.generateRandomArrays(samples, dimension);
    double[][] k2 = Utilities.generateRandomArrays(samples, dimension);

    int workPerThread = samples / threads;

    ArrayList<Callable<Integer>> taskList = new ArrayList<>();

    // add k1
    if (isVerbose) {
      System.out.println("adding");
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k1[workIndex + j], j);
        }
        return 0;
      });
    }

    Utilities.invokeAndWait(taskList);

    Utilities.checkKeysInCKD(k1, ckd, true);
    Utilities.checkKeysInCKD(k2, ckd, false);
    Assert.assertEquals(samples, ckd.size());

    taskList.clear();

    // delete k1, add k2
    if (isVerbose) {
      System.out.println("deleting and adding");
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.remove(k1[workIndex + j]);
        }
        return 0;
      });
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k2[workIndex + j], j);
        }
        return 0;
      });
    }

    Utilities.invokeAndWait(taskList);

    Utilities.checkKeysInCKD(k1, ckd, false);
    Utilities.checkKeysInCKD(k2, ckd, true);
    Assert.assertEquals(samples, ckd.size());
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

  private void simpleKeysIteration() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(3);
    // at some point, the key of newInternal would equal to its parent's key.
    double[][] k = {{5.305068244152987, 5.084449022627336, 4.155634301794545},
                    {4.658607614580709, 1.112285238547236, 7.6704533893483875},
                    {3.135000004662376, 4.737773994443383, 3.8336349759006993},
                    {1.0351259060545581, 4.21039722994082, 2.4693577126537414},
                    {5.877263378165557, 2.2656014079486053, 0.358466039752825}};

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);

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

    startInsertWork(threads, k, ckd);

    Utilities.checkKeysInCKD(k, ckd, true);
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
  public void testGet() throws Exception {
    int samples = 200000;
    int dimension = 20;
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding");
    }

    Utilities.addKeysToCKD(k, ckd);
    Utilities.checkKeysInCKD(k, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("getting");
    }

    for (int i = 0; i < samples; ++i) {
      Assert.assertEquals((Integer) i, ckd.get(k[i]));
    }

    double[] nonKey = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    Assert.assertEquals(null, ckd.get(nonKey));
  }

  @Test
  public void testSpecial() {
    for (int i = 0; i < 5; ++i) {
      try {
        testMultithreadAdd();
        testDelete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
package chaomai.ckdtree.snapshot2;

import chaomai.ckdtree.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by chaomai on 11/22/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;
  boolean isVerbose = true;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    InternalNode root = ckd.root;

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

  @Test
  public void deleteOnekey() throws Exception {
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
    InternalNode root = ckd.root;

    Assert.assertEquals(null, res1.gp);
    Assert.assertEquals(root, res1.p);
    Assert.assertEquals(root.left, res1.l);
  }

  private void snapshotOnEmptyTree() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);

    CKDTreeMap<Integer> snap = ckd.snapshot();

    Assert.assertEquals(0, snap.size());
  }

  private void snapshotOnTreeWithMultipleDimension() {
    int samples = 20000;
    int dimension = 20;
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k1 = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding");
    }

    Utilities.addKeysToCKD(k1, ckd);
    Utilities.checkKeysInCKD(k1, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    if (isVerbose) {
      System.out.println("snapshot");
    }

    CKDTreeMap<Integer> snap = ckd.snapshot();

    Assert.assertEquals(samples, snap.size());

    ArrayList<Map.Entry<double[], Integer>> ls1 = new ArrayList<>();
    for (Map.Entry<double[], Integer> l : snap) {
      ls1.add(l);
    }

    Assert.assertEquals(samples, ls1.size());

    for (Map.Entry<double[], Integer> l : snap) {
      Assert.assertTrue(ckd.contains(l.getKey()));
    }

    double[][] k2 = Utilities.generateRandomArrays(samples, dimension);

    if (isVerbose) {
      System.out.println("adding into snapshot");
    }

    Utilities.addKeysToCKD(k2, snap);
    Utilities.checkKeysInCKD(k1, snap, true);
    Utilities.checkKeysInCKD(k2, snap, true);
    Assert.assertEquals(samples * 2, snap.size());

    ArrayList<Map.Entry<double[], Integer>> ls2 = new ArrayList<>();
    for (Map.Entry<double[], Integer> l : snap) {
      ls2.add(l);
    }

    Assert.assertEquals(samples * 2, ls2.size());
  }

  private void snapshotOnTreeWithMultithreadUpdate()
      throws ExecutionException, InterruptedException {
    int samples = 20000;
    int dimension = 20;
    int threads = 10;

    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(dimension);

    if (isVerbose) {
      System.out.println("generating keys");
    }

    double[][] k = Utilities.generateRandomArrays(samples * 2, dimension);
    double[][] k1 = new double[samples][];
    System.arraycopy(k, 0, k1, 0, samples);
    double[][] k2 = new double[samples][];
    System.arraycopy(k, samples, k2, 0, samples);

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
        return null;
      });
    }

    Utilities.invokeAndWait(taskList);

    Utilities.checkKeysInCKD(k1, ckd, true);
    Utilities.checkKeysInCKD(k2, ckd, false);
    Assert.assertEquals(samples, ckd.size());

    // delete k1, add k2 and snapshot
    ArrayList<Callable<CKDTreeMap<Integer>>> taskList2 = new ArrayList<>();

    if (isVerbose) {
      System.out.println("deleting, adding and getting snapshot");
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList2.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.remove(k1[workIndex + j]);
        }
        return null;
      });
    }

    for (int i = 0; i < threads; ++i) {
      int workIndex = i * workPerThread;
      taskList2.add(() -> {
        for (int j = 0; j < workPerThread; ++j) {
          ckd.add(k2[workIndex + j], j);
        }
        return null;
      });
    }

    for (int i = 0; i < threads; ++i) {
      taskList2.add(() -> {
        for (int j = 0; j < 1000; ++j) {
          ckd.snapshot();
        }
        return ckd.snapshot();
      });
    }

    ExecutorService executor = Executors.newFixedThreadPool(taskList2.size());

    Collection<Future<CKDTreeMap<Integer>>> futures = executor.invokeAll(taskList2);
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

    Utilities.checkKeysInCKD(k1, ckd, false);
    Utilities.checkKeysInCKD(k2, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    for (Future<CKDTreeMap<Integer>> future : futures) {
      CKDTreeMap<Integer> snap = future.get();

      if (snap != null) {
        // all snapshot() will be delayed until all updates finished.
        Assert.assertEquals(samples, snap.size());

        for (Map.Entry<double[], Integer> e : snap) {
          Assert.assertTrue(ckd.contains(e.getKey()));
          Assert.assertTrue(ckd.contains(k2[e.getValue()]));
        }
      }
    }
  }

  @Test
  public void testSnapshot() throws Exception {
    if (isVerbose) {
      System.out.println("\nsnapshot On Empty Tree");
    }
    snapshotOnEmptyTree();

    if (isVerbose) {
      System.out.println("\nsnapshot On Tree With Multiple Dimension");
    }
    snapshotOnTreeWithMultipleDimension();

    if (isVerbose) {
      System.out.println("\nsnapshot On Tree With Multithread Update");
    }
    snapshotOnTreeWithMultithreadUpdate();
  }
}
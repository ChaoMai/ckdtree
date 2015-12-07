package chaomai.ckdtree.snapshot3;

import chaomai.ckdtree.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by chaomai on 12/3/15.
 */
public class CKDTreeMapTest {
  double delta = 0.001;
  boolean isVerbose = true;

  @Test
  public void testConstructor() throws Exception {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    Node<Integer> root = ckd.root;

    Assert.assertEquals(Double.POSITIVE_INFINITY, root.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.left.key[0], delta);
    Assert.assertEquals(Double.POSITIVE_INFINITY, root.right.key[0], delta);
  }

  private void searchDummy() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    double[] k = {Double.POSITIVE_INFINITY};

    Assert.assertTrue(ckd.contains(k));
  }

  @Test
  public void testSearch() throws Exception {
    searchDummy();
  }

  private void snapshotOnEmptyTree() {
    CKDTreeMap<Integer> ckd = new CKDTreeMap<>(1);
    ArrayList<Map.Entry<double[], Integer>> snap = ckd.snapshot();

    Assert.assertEquals(0, snap.size());
  }

  private void snapshotOnTreeWithMultipleDimension() {
    int samples = 20000;
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
      System.out.println("snapshot");
    }

    ArrayList<Map.Entry<double[], Integer>> snap = ckd.snapshot();

    Assert.assertEquals(samples, snap.size());

    for (Map.Entry<double[], Integer> e : snap) {
      Assert.assertTrue(ckd.contains(e.getKey()));
      Assert.assertTrue(ckd.contains(k[e.getValue()]));
    }
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
    ArrayList<Callable<ArrayList<Map.Entry<double[], Integer>>>> taskList2 = new ArrayList<>();

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
        for (int j = 0; j < 100; ++j) {
          TimeUnit.MICROSECONDS.sleep(100);
          ckd.snapshot();
        }
        return ckd.snapshot();
      });
    }

    ExecutorService executor = Executors.newFixedThreadPool(taskList2.size());

    Collection<Future<ArrayList<Map.Entry<double[], Integer>>>> futures =
        executor.invokeAll(taskList2);
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

    Utilities.checkKeysInCKD(k1, ckd, false);
    Utilities.checkKeysInCKD(k2, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    for (Future<ArrayList<Map.Entry<double[], Integer>>> future : futures) {
      ArrayList<Map.Entry<double[], Integer>> snap = future.get();

      if (snap != null) {
        // all snapshot() will be also delayed until all updates finished.
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
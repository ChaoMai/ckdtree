package chaomai.ckdtree.snapshot3;

import chaomai.ckdtree.Utilities;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
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
    Object[] snap = ckd.snapshot();

    Assert.assertEquals(0, snap.length);
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

    Object[] snap = ckd.snapshot();

    Assert.assertEquals(samples, snap.length);

    for (Object o : snap) {
      Node<Integer> l = (Node<Integer>) o;
      Assert.assertTrue(ckd.contains(l.key));
      Assert.assertTrue(ckd.contains(k[l.value]));
    }
  }

  private void snapshotOnTreeWithMultithreadUpdate()
      throws ExecutionException, InterruptedException {
    int samples = 20000;
    int dimension = 10;
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
    ArrayList<Callable<Object[]>> taskList2 = new ArrayList<>();

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

    Collection<Future<Object[]>> futures = executor.invokeAll(taskList2);
    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

    Utilities.checkKeysInCKD(k1, ckd, false);
    Utilities.checkKeysInCKD(k2, ckd, true);
    Assert.assertEquals(samples, ckd.size());

    for (Future<Object[]> future : futures) {
      Object[] snap = future.get();

      if (snap != null) {
        // all snapshot() will be delayed until all update finished.
        Assert.assertEquals(samples, snap.length);

        for (Object o : snap) {
          Node<Integer> l = (Node<Integer>) o;
          Assert.assertTrue(ckd.contains(l.key));
          Assert.assertTrue(ckd.contains(k2[l.value]));
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
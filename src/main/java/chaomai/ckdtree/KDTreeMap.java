package chaomai.ckdtree;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KDTreeMap<TData> implements KDTreeMapInterface<TData> {
  private final InternalNode root;
  private final int dimension;
  private final AtomicInteger count;

  public KDTreeMap(int k) {
    this.dimension = k;
    double[] rootKey = new double[k];
    for (int i = 0; i < k; i++) {
      rootKey[i] = Double.NEGATIVE_INFINITY;
    }
    root = new InternalNode(rootKey, null, new Leaf<TData>(rootKey));
    count = new AtomicInteger(0);
  }

  private boolean keyEqual(double[] k1, double[] k2) {
    if (k1 == k2) return true;
    for (int i = 0; i < dimension; i++) {
      if (k1[i] != k2[i]) return false;
    }
    return true;
  }

  // private int depthToIndex(int depth) {
  // return depth % dimension;
  // }

  private int keyCompare(double[] k1, double[] k2, int depth) {
    if (k1 == k2) return 0;
    // if (k2 == null)
    // return 1;
    // if (k1 == null)
    // return -1;
    int index = depth % dimension;
    // return k1[index] - k2[index];
    if (k1[index] < k2[index]) return -1;
    if (k1[index] > k2[index]) return 1;
    return 0;
  }

  @SuppressWarnings("unchecked")
  private SearchResult<TData> Search(InternalNode node, double[] key) {
    InternalNode gp = null;
    Flag gpVersion = null;
    InternalNode p = root;
    Flag pVersion = p.flag;
    Node l = root.right;
    int depth = 0;
    while (l instanceof InternalNode) {
      gp = p;
      p = (InternalNode) l;
      gpVersion = pVersion;
      pVersion = p.flag;
      depth += p.skippedDepth;
      if (keyCompare(key, p.key, depth++) < 0) l = p.left;
      else l = p.right;
    }
    return new SearchResult<TData>(gp, p, (Leaf<TData>) l, depth, gpVersion, pVersion);
  }

  private SearchResult<TData> Search(double[] key) {
    return Search(root, key);
  }

  public boolean containsKey(double[] key) {
        /*
         * if (key == null || key.getDimension() != dimension) throw new
		 * InvalidParameterException();
		 */
    SearchResult<TData> sr = Search(key);
    return keyEqual(sr.leaf.key, key);
  }

  public TData get(double[] key) {
        /*
         * if (key == null || key.getDimension() != dimension) throw new
		 * InvalidParameterException();
		 */
    SearchResult<TData> sr = Search(key);
    if (!keyEqual(sr.leaf.key, key)) return null;
    return sr.leaf.data;
  }

  public boolean set(double[] key, TData data) {
        /*
         * if (key == null || key.getDimension() != dimension) throw new
		 * InvalidParameterException();
		 */
    SearchResult<TData> sr = Search(key);
    if (!keyEqual(sr.leaf.key, key)) return false;
    sr.leaf.data = data;
    return true;
  }

  public boolean insert(double[] key, TData data) {
    // if (key == null || key.getDimension() != dimension)
    // throw new InvalidParameterException();
    // int retry = 0;
    while (true) {
      SearchResult<TData> sr = Search(key);
      if (keyEqual(sr.leaf.key, key)) return false;
      if (tryInsert(key, data, sr)) {
        count.incrementAndGet();
        return true;
      }
      // Thread.yield();
      // System.out.println(++retry);
    }
  }

  private boolean tryInsert(double[] key, TData data, SearchResult<TData> sr) {
    Flag pv = sr.parentVersion;
    if (pv.state != State.CLEAN) return false;
    InternalNode p = sr.parent;
    Leaf<TData> l = sr.leaf;
    Leaf<TData> newSibling = new Leaf<TData>(key, data);
    int depth = sr.leafDepth;
    int compareResult;
    int skip = 0;
    while ((compareResult = keyCompare(key, l.key, depth++)) == 0) ++skip;
    InternalNode newInternalNode = null;
    if (compareResult < 0) {
      newInternalNode = new InternalNode(l.key, newSibling, l, skip);
    } else {
      newInternalNode = new InternalNode(key, l, newSibling, skip);
    }
    if (pv.state != State.CLEAN || !p.casFlag(pv, new Flag(State.INSERTING))) return false;
    if (l == p.left) p.left = newInternalNode;
    else p.right = newInternalNode;

    p.flag = new Flag();
    return true;
  }

  public boolean delete(double[] key) {
    // if (key == null || key.getDimension() != dimension)
    // throw new InvalidParameterException();
    // int retry = 0;
    while (true) {
      SearchResult<TData> sr = Search(key);
      if (!keyEqual(sr.leaf.key, key)) return false;
      if (tryDelete(key, sr)) {
        count.decrementAndGet();
        return true;
      }
      // Thread.yield();
      // System.out.println(++retry);
    }
  }

  public boolean tryDelete(double[] key, SearchResult<TData> sr) {
    InternalNode gp = sr.grandparent;
    InternalNode p = sr.parent;
    Flag gpv = sr.grandparentVersion;
    Flag pv = sr.parentVersion;
    Leaf<TData> l = sr.leaf;
    if (gpv.state != State.CLEAN || pv.state != State.CLEAN) return false;
    if (!gp.casFlag(gpv, new Flag(State.DELETING))) return false;
    if (!p.casFlag(pv, new Flag(State.MARKED))) {
      gp.flag = new Flag();
      return false;
    }
    Node other = (l == p.left ? p.right : p.left);
    if (other instanceof InternalNode) {
      InternalNode o = (InternalNode) other;
      InternalNode newInternal = new InternalNode(o.key, null, null, (p.skippedDepth + o.skippedDepth + 1));
      Flag ov;
      do {
        ov = o.flag;
      } while (ov.state != State.CLEAN || !o.casFlag(ov, new Flag(State.MARKED)));
      newInternal.left = o.left;
      newInternal.right = o.right;
      other = newInternal;
    }
    if (p == gp.left) gp.left = other;
    else gp.right = other;
    gp.flag = new Flag();
    return true;
  }

  private double distanceBetweenLeaves(double[] a, double[] b) {
    if (a == b) return 0;
    if (a == null || b == null) return Double.MAX_VALUE;
    double d = 0.0;
    for (int i = 0; i < this.dimension; i++) {
      double t = a[i] - b[i];
      d += t * t;
    }
    return d;
  }

  private double distanceToHyperplane(double[] a, double[] b, int depth) {
    if (a == b) return 0;
    if (a == null || b == null) return Double.MAX_VALUE;
    int i = depth % dimension;
    // if (index < 0)
    // System.out.println("indexindexindexindexindexindexindexindex");
    double d = a[i] - b[i];
    return d * d;
  }

  private static class BacktrackingData {
    public final InternalNode parent;
    public final Node node;
    public final int depth;

    public BacktrackingData(InternalNode p, Node n, int d) {
      parent = p;
      node = n;
      depth = d;
    }
  }

  public NearestResult nearest(double[] target) {
    List<NearestResult> r = nearest(target, 1);
    return r == null || r.isEmpty() ? null : r.get(0);
  }

  public List<NearestResult> nearest(double[] target, int n) {
    NearestResult[] result = new NearestResult[n];
    //int times = 0;
    PriorityQueue<NearestResult> q = new PriorityQueue<NearestResult>(n, new Comparator<NearestResult>() {
      @Override
      public int compare(NearestResult a, NearestResult b) {
        if (a.distance > b.distance) return -1;
        if (a.distance < b.distance) return 1;
        return 0;
      }
    });

    Stack<BacktrackingData> stack = new Stack<>();
    stack.push(new BacktrackingData(root, root.right, 1));
    q.add(new NearestResult(null, Double.POSITIVE_INFINITY, null));

    while (!stack.empty()) {
      BacktrackingData data = stack.pop();
      InternalNode p = data.parent;
      Node l = data.node;
      int depth = data.depth;
      double bin = distanceToHyperplane(p.key, target, depth - 1);
      double radius = q.peek().distance;
      //++times;
      if (bin > radius || bin >= radius && l == p.left) continue;
      while (l instanceof InternalNode) {
        p = (InternalNode) l;
        depth += p.skippedDepth;
        double keyCompareResult = keyCompare(target, p.key, depth++);
        if (keyCompareResult < 0) {
          l = p.left;
          stack.push(new BacktrackingData(p, p.right, depth));

        } else {
          l = p.right;
          stack.push(new BacktrackingData(p, p.left, depth));
        }
        //++times;
      }
      double d = distanceBetweenLeaves(l.key, target);
      if (d < radius) {
        q.add(new NearestResult(l.key, d, ((Leaf<TData>) l).data));
        if (q.size() > n) q.poll();
      }
    }

    for (int i = q.size() - 1; i >= 0; --i) {
      NearestResult item = q.poll();
      if (item.key != null) result[i] = item;
    }
    // System.out.println(times);
    return Arrays.asList(result);
  }

  @Override
  public int getNodeCount() {
    return count.get() * 2 + 2;
  }

  public String toString() {
    return "Our_kd-Tree_without_helpers";
  }
}

package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
class InternalNode<V> extends Node {
  Node left;
  Node right;
  final Update update;
  final int skippedDepth;
  final Gen gen;

  InternalNode prev;

  public static final AtomicReferenceFieldUpdater<InternalNode, InternalNode> updater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, InternalNode.class, "prev");

  InternalNode(double[] key, Node left, Node right, Update update, int skippedDepth, Gen gen) {
    super(key);
    this.left = left;
    this.right = right;
    this.update = update;
    this.skippedDepth = skippedDepth;
    this.gen = gen;
  }

  InternalNode renewed(Gen newGen) {
    // todo: should perform a deep copy here(everything here and things in `update`), any optimization?
    return new InternalNode(key, left, right, new Update(), skippedDepth, gen);
  }

  void WRITE_PREV(InternalNode old) {
    updater.set(this, old);
  }

  InternalNode GCAS_Complete(InternalNode n, CKDTreeMap<V> ckd) {
    if (n == null) {
      return null;
    } else {
      InternalNode prev = n.prev;
      InternalNode root = ckd.readRoot();

      // todo: finish this
      if (prev == null) {
        return n;
      } else if (prev instanceof InternalNode) {
        if (root.gen == this.gen) {

        }
      }
    }

    return null;
  }

  boolean GCAS(InternalNode old, InternalNode n, CKDTreeMap<V> ckd) {
    n.WRITE_PREV(old);
    if (updater.compareAndSet(this, old, n)) {
      GCAS_Complete(n, ckd);
      return n.prev == null;
    } else {
      return false;
    }
  }
}
package chaomai.ckdtree.snapshot2;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/22/15.
 */
class InternalNode<V> extends Node<V> {
  private static final AtomicReferenceFieldUpdater<InternalNode, Update> updateUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Update.class, "update");
  final int skippedDepth;
  private volatile Update update;

  InternalNode(double[] key, Node<V> left, Node<V> right, int skippedDepth) {
    this(key, left, right, new Update(), skippedDepth);
  }

  InternalNode(double[] key, Node<V> left, Node<V> right, Update update, int skippedDepth) {
    super(key, left, right);
    this.update = update;
    this.skippedDepth = skippedDepth;
  }

  boolean CAS_UPDATE(Update old, Update n) {
    return updateUpdater.compareAndSet(this, old, n);
  }

  Update GET_UPDATE() {
    return updateUpdater.get(this);
  }

  @Override
  public String toString() {
    String res = "key: [";

    for (double d : this.key) {
      res += d + ", ";
    }

    res += "]\n";

    return res;
  }
}
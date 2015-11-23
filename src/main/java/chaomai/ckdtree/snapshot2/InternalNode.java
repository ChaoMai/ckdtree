package chaomai.ckdtree.snapshot2;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/22/15.
 */
class InternalNode extends Node {
  private static final AtomicReferenceFieldUpdater<InternalNode, Node> leftUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Node.class, "left");
  private static final AtomicReferenceFieldUpdater<InternalNode, Node> rightUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Node.class, "right");
  private static final AtomicReferenceFieldUpdater<InternalNode, Update> updateUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Update.class, "update");
  final int skippedDepth;
  volatile Node left;
  volatile Node right;
  private volatile Update update;

  InternalNode(double[] key, Node left, Node right, int skippedDepth) {
    this(key, left, right, new Update(), skippedDepth);
  }

  InternalNode(double[] key, Node left, Node right, Update update, int skippedDepth) {
    super(key);
    this.left = left;
    this.right = right;
    this.update = update;
    this.skippedDepth = skippedDepth;
  }

  boolean CAS_LEFT(Node old, Node n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_RIGHT(Node old, Node n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_UPDATE(Update old, Update n) {
    return updateUpdater.compareAndSet(this, old, n);
  }

  void WRITE_LEFT(Node left) {
    leftUpdater.set(this, left);
  }

  void WRITE_RIGHT(Node right) {
    rightUpdater.set(this, right);
  }

  Update GET_UPDATE() {
    return updateUpdater.get(this);
  }
}
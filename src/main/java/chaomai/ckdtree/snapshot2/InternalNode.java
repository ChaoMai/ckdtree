package chaomai.ckdtree.snapshot2;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/22/15.
 */
class InternalNode<V> extends Node<V> {
  private static final AtomicReferenceFieldUpdater<InternalNode, Node> leftUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Node.class, "left");
  private static final AtomicReferenceFieldUpdater<InternalNode, Node> rightUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Node.class, "right");
  private static final AtomicReferenceFieldUpdater<InternalNode, Update> updateUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Update.class, "update");
  final int skippedDepth;
  volatile Node<V> left;
  volatile Node<V> right;
  private volatile Update update;

  InternalNode(double[] key, Node<V> left, Node<V> right, int skippedDepth) {
    this(key, left, right, new Update(), skippedDepth);
  }

  InternalNode(double[] key, Node<V> left, Node<V> right, Update update, int skippedDepth) {
    super(key);
    this.left = left;
    this.right = right;
    this.update = update;
    this.skippedDepth = skippedDepth;
  }

  boolean CAS_LEFT(Node<V> old, Node<V> n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_RIGHT(Node<V> old, Node<V> n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_UPDATE(Update old, Update n) {
    return updateUpdater.compareAndSet(this, old, n);
  }

  void WRITE_LEFT(Node<V> left) {
    leftUpdater.set(this, left);
  }

  void WRITE_RIGHT(Node<V> right) {
    rightUpdater.set(this, right);
  }

  Update GET_UPDATE() {
    return updateUpdater.get(this);
  }
}
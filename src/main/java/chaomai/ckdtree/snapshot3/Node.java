package chaomai.ckdtree.snapshot3;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 12/2/15.
 */
class Node<V> {
  private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
  private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
  private static final AtomicReferenceFieldUpdater<Node, Info> infoUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Info.class, "info");
  final double[] key;
  final V value;
  final int skippedDepth;
  volatile Node<V> left;
  volatile Node<V> right;
  volatile Info info;
  volatile Info dirty;

  private Node(final double[] key, final V value, final int skippedDepth, final Node<V> left,
               final Node<V> right) {
    this.key = key;
    this.value = value;
    this.skippedDepth = skippedDepth;
    this.left = left;
    this.right = right;
    this.info = null;
    this.dirty = null;
  }

  Node(final double[] key, final V value) {
    this(key, value, 0, null, null);
  }

  Node(final double[] key, final int skippedDepth, final Node<V> left, final Node<V> right) {
    this(key, null, skippedDepth, left, right);
  }

  boolean CAS_LEFT(final Node<V> old, final Node<V> n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_RIGHT(final Node<V> old, final Node<V> n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  boolean CAS_INFO(final Info old, final Info n) {
    return infoUpdater.compareAndSet(this, old, n);
  }
}
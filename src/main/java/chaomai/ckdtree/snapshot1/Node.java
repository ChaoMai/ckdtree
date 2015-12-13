package chaomai.ckdtree.snapshot1;

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
  private static final AtomicReferenceFieldUpdater<Node, Node> prevUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "prev");
  final double[] key;
  final V value;
  final int skippedDepth;
  final Gen gen;
  volatile Node<V> left;
  volatile Node<V> right;
  volatile Info info;
  volatile Node<V> prev;

  private Node(final double[] key, final V value, final int skippedDepth, final Node<V> left,
               final Node<V> right, final Gen gen) {
    this.key = key;
    this.value = value;
    this.skippedDepth = skippedDepth;
    this.left = left;
    this.right = right;
    this.prev = null;
    this.gen = gen;
  }

  Node(final double[] key, final V value) {
    this(key, value, 0, null, null, null);
  }

  Node(final double[] key, final int skippedDepth, final Node<V> left, final Node<V> right,
       final Gen gen) {
    this(key, null, skippedDepth, left, right, gen);
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

  void WRITE_INFO(final Info n) {
    infoUpdater.set(this, n);
  }

  private boolean CAS_PREV(final Node<V> old, final Node<V> n) {
    return prevUpdater.compareAndSet(this, old, n);
  }

  protected void WRITE_PREV(final Node<V> n) {
    prevUpdater.set(this, n);
  }

  private Node<V> GCAS_LEFT_Complete(final Node<V> n, final CKDTreeMap<V> ckd) {
    if (n == null) {
      return null;
    } else {
      Node<V> prev = n.prev;

      if (prev == null) {
        return n;
      }

      if (prev.getClass() == FailedNode.class) {
        FailedNode<V> fn = (FailedNode<V>) prev;

        if (CAS_LEFT(n, fn.prev)) {
          return fn.prev;
        } else {
          return GCAS_LEFT_Complete(this.left, ckd);
        }
      } else {
        // normal node
        Node<V> ckdr = ckd.RDCSS_READ_ROOT(true);

        if (ckdr.gen == this.gen) {
          if (n.CAS_PREV(prev, null)) {
            return n;
          } else {
            return GCAS_LEFT_Complete(n, ckd);
          }
        } else {
          n.CAS_PREV(prev, new FailedNode<>(prev));
          return GCAS_LEFT_Complete(this.left, ckd);
        }
      }
    }
  }

  Object GCAS_LEFT(final Node<V> old, final Node<V> n, CKDTreeMap<V> ckd) {
    n.WRITE_PREV(old);
    if (CAS_LEFT(old, n)) {
      if (GCAS_LEFT_Complete(n, ckd) == old) {
        return Gen.GenFailed;
      } else {
        return n.prev == null;
      }
    } else {
      return false;
    }
  }

  private Node<V> GCAS_RIGHT_Complete(final Node<V> n, final CKDTreeMap<V> ckd) {
    if (n == null) {
      return null;
    } else {
      Node<V> prev = n.prev;

      if (prev == null) {
        return n;
      }

      if (prev.getClass() == FailedNode.class) {
        FailedNode<V> fn = (FailedNode<V>) prev;

        if (CAS_RIGHT(n, fn.prev)) {
          return fn.prev;
        } else {
          return GCAS_RIGHT_Complete(this.right, ckd);
        }
      } else {
        // normal node
        Node<V> ckdr = ckd.RDCSS_READ_ROOT(true);

        if (ckdr.gen == this.gen) {
          if (n.CAS_PREV(prev, null)) {
            return n;
          } else {
            return GCAS_RIGHT_Complete(n, ckd);
          }
        } else {
          n.CAS_PREV(prev, new FailedNode<>(prev));
          return GCAS_RIGHT_Complete(this.right, ckd);
        }
      }
    }
  }

  Object GCAS_RIGHT(final Node<V> old, final Node<V> n, CKDTreeMap<V> ckd) {
    n.WRITE_PREV(old);
    if (CAS_RIGHT(old, n)) {
      if (GCAS_RIGHT_Complete(n, ckd) == old) {
        return Gen.GenFailed;
      } else {
        return n.prev == null;
      }
    } else {
      return false;
    }
  }

  Node<V> GCAS_READ_LEFT(CKDTreeMap<V> ckd) {
    Node<V> prev = this.left.prev;

    if (prev == null) {
      return this.left;
    } else {
      return GCAS_LEFT_Complete(this.left, ckd);
    }
  }

  Node<V> GCAS_READ_RIGHT(CKDTreeMap<V> ckd) {
    Node<V> prev = this.right.prev;

    if (prev == null) {
      return this.right;
    } else {
      return GCAS_RIGHT_Complete(this.right, ckd);
    }
  }

  Node<V> renew(Gen newGen, CKDTreeMap<V> ckd) {
    Node<V> ol = this.GCAS_READ_LEFT(ckd);
    Node<V> or = this.GCAS_READ_RIGHT(ckd);

    return new Node<>(this.key, this.skippedDepth, ol, or, newGen);
  }
}
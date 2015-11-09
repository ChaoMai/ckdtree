package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
abstract class Node<V> {
  private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
  private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
  private static final AtomicReferenceFieldUpdater<Node, Node> prevUpdater =
      AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "prev");
  final double[] key;
  final Gen gen;
  volatile Node<V> left;
  volatile Node<V> right;
  volatile Node<V> prev;

  Node() {
    this(null);
  }

  Node(double[] key) {
    this(key, null, null, null);
  }

  Node(double[] key, Node<V> left, Node<V> right, Gen gen) {
    this.key = key;
    this.left = left;
    this.right = right;
    this.prev = null;
    this.gen = gen;
  }

  private boolean CAS_LEFT(Node<V> old, Node<V> n) {
    return leftUpdater.compareAndSet(this, old, n);
  }

  private boolean CAS_RIGHT(Node<V> old, Node<V> n) {
    return rightUpdater.compareAndSet(this, old, n);
  }

  private boolean CAS_PREV(Node<V> old, Node<V> n) {
    return prevUpdater.compareAndSet(this, old, n);
  }

  protected void WRITE_LEFT(Node<V> left) {
    leftUpdater.set(this, left);
  }

  protected void WRITE_RIGHT(Node<V> right) {
    rightUpdater.set(this, right);
  }

  protected void WRITE_PREV(Node<V> old) {
    prevUpdater.set(this, old);
  }

  private Node<V> GCAS_COMPLETE(Node<V> n, CKDTreeMap<V> ckd, Direction direction) {
    if (n == null) {
      return null;
    } else {
      Node prev = n.prev;
      InternalNode<V> root = ckd.readRoot();

      if (prev == null) {
        return n;
      }

      if (prev instanceof FailedNode) {
        FailedNode<V> fn = (FailedNode<V>) prev;
        if (direction == Direction.LEFT) {
          if (CAS_LEFT(n, fn.prev)) {
            return fn.prev;
          } else {
            return GCAS_COMPLETE(n, ckd, direction);
          }
        } else {
          if (CAS_RIGHT(n, fn.prev)) {
            return fn.prev;
          } else {
            return GCAS_COMPLETE(n, ckd, direction);
          }
        }
      } else if (prev instanceof InternalNode || prev instanceof Leaf) {
        if (root.gen == this.gen && ckd.nonReadOnly()) {
          if (n.CAS_PREV(prev, null)) {
            return n;
          } else {
            return GCAS_COMPLETE(n, ckd, direction);
          }
        } else {
          n.CAS_PREV(prev, new FailedNode<>(prev));
          return GCAS_COMPLETE(n, ckd, direction);
        }
      }
    }
    throw new RuntimeException("Should not happen");
  }

  protected boolean GCAS(Node<V> old, Node<V> n, CKDTreeMap<V> ckd, Direction direction) {
    n.WRITE_PREV(old);

    switch (direction) {
      case LEFT: {
        if (CAS_LEFT(old, n)) {
          GCAS_COMPLETE(n, ckd, direction);
          return n.prev == null;
        } else {
          return false;
        }
      }
      case RIGHT: {
        n.WRITE_PREV(old);
        if (CAS_RIGHT(old, n)) {
          GCAS_COMPLETE(n, ckd, direction);
          return n.prev == null;
        } else {
          return false;
        }
      }
      default: {
        throw new RuntimeException("Should not happen");
      }
    }
  }

  protected Node<V> GCAS_READ_LEFT_CHILD(CKDTreeMap<V> ckd) {
    Node<V> left = this.left;
    Node<V> prev = left.prev;

    if (prev == null) {
      return left;
    } else {
      return GCAS_COMPLETE(left, ckd, Direction.LEFT);
    }
  }

  protected Node<V> GCAS_READ_RIGHT_CHILD(CKDTreeMap<V> ckd) {
    Node<V> right = this.right;
    Node<V> prev = right.prev;

    if (prev == null) {
      return right;
    } else {
      return GCAS_COMPLETE(right, ckd, Direction.RIGHT);
    }
  }

  @Override
  public String toString() {
    String res = "key: [";

    for (double d : this.key) {
      res += d + ", ";
    }

    res += "]\n";

    if (this.left != null) {
      res += "left: " + this.left.toString() + "\n";
    } else {
      res += "left: null\n";
    }

    if (this.right != null) {
      res += "right: " + this.right.toString() + "\n";
    } else {
      res += "right: null\n";
    }

    return res;
  }
}
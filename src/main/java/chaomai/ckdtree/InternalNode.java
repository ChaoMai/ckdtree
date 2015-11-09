package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by chaomai on 11/1/15.
 */
class InternalNode<V> extends Node<V> {
  private static final AtomicReferenceFieldUpdater<InternalNode, Update> updateUpdater =
      AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Update.class, "update");
  final int skippedDepth;
  private volatile Update update;

  InternalNode(double[] key, Node<V> left, Node<V> right, int skippedDepth, Gen gen) {
    this(key, left, right, new Update(), skippedDepth, gen);
  }

  InternalNode(double[] key, Node<V> left, Node<V> right, Update update, int skippedDepth,
               Gen gen) {
    super(key, left, right, gen);
    this.update = update;
    this.skippedDepth = skippedDepth;
  }

  InternalNode<V> renewed(Gen newGen) {
    // todo: should perform a deep copy here(everything here and things in `update`)
    // todo: or just create new `update`
    // todo: any optimization?
    return new InternalNode<>(this.key, this.left, this.right, new Update(), this.skippedDepth,
                              newGen);
  }

  InternalNode<V> copyRootToGen(Gen newGen) {
    // copy root and the left child of leaf
    InternalNode<V> nr =
        new InternalNode<>(this.key, null, null, new Update(), this.skippedDepth, newGen);

    Node<V> l = this.left;
    Node<V> nl;
    if (l instanceof Leaf) {
      nl = new Leaf<>(l.key, (V) ((Leaf) l).value);
    } else if (l instanceof InternalNode) {
      nl = new InternalNode<>(l.key, l.left, l.right, new Update(), ((InternalNode) l).skippedDepth,
                              newGen);
    } else {
      throw new IllegalStateException("Left of root is neither Leaf or InternalNode");
    }

    nr.left = nl;

    return nr;
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
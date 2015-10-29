package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

class InternalNode extends Node {
  public volatile Flag flag = new Flag();
  public Node left;
  public Node right;
  public int skippedDepth;

  private final static AtomicReferenceFieldUpdater<InternalNode, Flag> flagUpdater = AtomicReferenceFieldUpdater.newUpdater(InternalNode.class, Flag.class, "flag");

  public boolean casFlag(Flag expect, Flag update) {
    return flagUpdater.compareAndSet(this, expect, update);
  }

  public InternalNode(double[] key, Gen gen) {
    super(key, gen);
    skippedDepth = 0;
  }

  public InternalNode(double[] key, int skip, Gen gen) {
    super(key, gen);
    skippedDepth = skip;
  }

  public InternalNode(double[] key, Node l, Node r, Gen gen) {
    super(key, gen);
    left = l;
    right = r;
    skippedDepth = 0;
  }

  public InternalNode(double[] key, Node l, Node r, int skip, Gen gen) {
    super(key, gen);
    left = l;
    right = r;
    skippedDepth = skip;
  }

  public String toString() {
    return "InternalNode " + key + " flag: " + flag.state;
  }
}

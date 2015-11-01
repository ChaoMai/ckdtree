package chaomai.ckdtree;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by chaomai on 11/1/15.
 */
class InternalNode extends Node {
  AtomicReference<Node> left;
  AtomicReference<Node> right;
  final Gen gen;

  InternalNode(double[] key, Node left, Node right, Gen gen) {
    super(key);
    this.left.set(left);
    this.right.set(right);
    this.gen = gen;
  }
}

package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
abstract class Node {
  final double[] key;

  Node() {
    this(null);
  }

  Node(double[] key) {
    this.key = key;
  }
}
package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
abstract class Node {
  final double[] key;

  Node(double[] key) {
    this.key = key;
  }
}

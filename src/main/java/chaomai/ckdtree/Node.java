package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
abstract class Node {
  public final double[] key;

  public Node(double[] key) {
    this.key = key;
  }
}

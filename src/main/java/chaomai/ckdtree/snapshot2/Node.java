package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
abstract class Node<V> {
  final double[] key;
  volatile Node<V> left;
  volatile Node<V> right;

  Node() {
    this(null);
  }

  Node(double[] key) {
    this(key, null, null);
  }

  Node(double[] key, Node<V> left, Node<V> right) {
    this.key = key;
    this.left = left;
    this.right = right;
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
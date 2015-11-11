package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Leaf<V> extends Node<V> {
  final V value;

  Leaf(double[] key) {
    this(key, null);
  }

  Leaf(double[] key, V value) {
    this(key, value, 0);
  }

  Leaf(double[] key, V value, int skippedDepth) {
    super(key, skippedDepth);
    this.value = value;
  }

  @Override
  public String toString() {
    String res = "leaf: <[";

    for (double d : this.key) {
      res += d + ", ";
    }

    res += "]," + this.value + ">";
    return res;
  }
}
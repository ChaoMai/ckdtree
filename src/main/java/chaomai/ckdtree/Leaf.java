package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Leaf<V> extends Node<V> {
  V value;

  Leaf(double[] key) {
    super(key);
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
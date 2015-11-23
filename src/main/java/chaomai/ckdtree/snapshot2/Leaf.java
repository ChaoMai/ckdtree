package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/22/15.
 */
class Leaf<V> extends Node {
  final V value;

  Leaf(double[] key) {
    this(key, null);
  }

  Leaf(double[] key, V value) {
    super(key);
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
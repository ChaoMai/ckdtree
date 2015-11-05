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
    super(key);
    this.value = value;
  }

  @Override
  public String toString() {
    String res = "leaf: ";

    for (double d : key) {
      res += d + " ";
    }

    res += "," + value;
    return res;
  }
}
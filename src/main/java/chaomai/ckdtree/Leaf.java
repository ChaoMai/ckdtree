package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Leaf<V> extends Node {
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

    for (int i = 0; i < key.length; ++i) {
      res += key[i] + " ";
    }
    res += key;
    return res;
  }
}
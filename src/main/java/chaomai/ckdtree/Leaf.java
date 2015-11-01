package chaomai.ckdtree;

/**
 * Created by chaomai on 11/1/15.
 */
class Leaf<V> extends Node {
  V value;

  Leaf(double[] key, V value) {
    super(key);
    this.value = value;
  }
}

package chaomai.ckdtree.snapshot2;

/**
 * Created by chaomai on 11/23/15.
 */
class Pair<K, V> {
  K key;
  V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !o.getClass().equals(getClass())) {
      return false;
    }
    Pair<K, V> p = (Pair<K, V>) o;
    return p.key == key && p.value == value;
  }
}
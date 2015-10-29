package chaomai.ckdtree;

class Leaf<TData> extends Node {
  public TData data;

  public Leaf(double[] key, Gen gen) {
    super(key, gen);
  }

  public Leaf(double[] key, TData data, Gen gen) {
    super(key, gen);
    this.data = data;
  }

  public String toString() {
    return "Leaf " + key.toString() + " value: " + (data == null ? "null" : data.toString());
  }
}

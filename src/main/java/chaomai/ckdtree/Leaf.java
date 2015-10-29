package chaomai.ckdtree;

//class Leaf<TKeyItem extends Comparable<TKeyItem>, TData> extends Node<TKeyItem> {
//	public TData data;
//
//	public Leaf(Vector<TKeyItem> key) {
//		super(key);
//	}
//
//	public Leaf(Vector<TKeyItem>  key, TData data) {
//		super(key);
//		this.data = data;
//	}
//
//	public String toString() {
//		return "Leaf " + key.toString() + " value: " + (data == null ? "null" : data.toString());
//	}
//}
class Leaf<TData> extends Node {
  public TData data;

  public Leaf(double[] key) {
    super(key);
  }

  public Leaf(double[] key, TData data) {
    super(key);
    this.data = data;
  }

  public String toString() {
    return "Leaf " + key.toString() + " value: " + (data == null ? "null" : data.toString());
  }
}

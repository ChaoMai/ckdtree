package chaomai.ckdtree;

public class NearestResult<TData> {
  public final double[] key;
  public final double distance;
  public TData data;

  public NearestResult(double[] k, double d, TData data) {
    this.key = k;
    this.distance = d;
    this.data = data;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    for (int i = 0; i < key.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(key[i]);
    }
    sb.append(" : ").append(distance).append(" : ").append(data).append('>');
    return sb.toString();
  }
}

package chaomai.ckdtree;

class Node {
  public final double[] key;

  public final Gen gen;

  public Node(double[] k, Gen generation) {
    key = k;
    gen = generation;
  }
}

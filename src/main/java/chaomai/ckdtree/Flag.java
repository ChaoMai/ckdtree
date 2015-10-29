package chaomai.ckdtree;

public class Flag {
  public final State state;

  public Flag() {
    state = State.CLEAN;
  }

  public Flag(State f) {
    state = f;
  }
}
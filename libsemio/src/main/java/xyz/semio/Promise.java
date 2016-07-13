package xyz.semio;

public class Promise<T> {
  private boolean _done = false;
  private T _value;
  private Function<T, Void> _after;

  public void complete(final T value) {
    if(this._done) return;

    this._done = true;
    this._value = value;
    this._apply();
  }

  private void _apply() {
    if(this._after == null || !this.isDone()) return;
    System.out.println("Applying " + this._value);
    this._after.apply(this._value);
  }

  public T get() {
    return this._value;
  }

  public boolean isDone() {
    return this._done;
  }

  public <R> Promise<R> then(final Function<T, R> func) {
    if(func == null) throw new NullPointerException("func cannot be null");

    final Promise<R> next = new Promise<R>();
    this._after = new Function<T, Void>() {
      @Override
      public Void apply(T t) {
        next.complete(func.apply(t));
        return null;
      }
    };
    this._apply();
    return next;
  }
}

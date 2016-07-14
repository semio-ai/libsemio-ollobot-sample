package xyz.semio;

/**
 * Represents a value that has not yet been computed and/or received.
 *
 * @param <T> The result type this Promise represents
 */
public class Promise<T> {
  private boolean _done = false;
  private T _value;
  private Function<T, Void> _after;

  /**
   * Marks the promise as complete with a given value.
   *
   * @param value the result of the computation.
   */
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

  /**
   * @return The completion value of this Promise, if any.
   */
  public T get() {
    return this._value;
  }

  /**
   * @return true if the Promise is complete, false otherwise
   */
  public boolean isDone() {
    return this._done;
  }

  /**
   * Chains a function to be executed after this Promise completes.
   *
   * @param func The function to execute upon completion
   * @param <R> The new Promise value type
   * @return A new promise that will be marked complete once the given function has returned.
   */
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

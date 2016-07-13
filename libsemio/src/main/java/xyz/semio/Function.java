package xyz.semio;

public interface Function<T, R> {
  public R apply(T value);
}

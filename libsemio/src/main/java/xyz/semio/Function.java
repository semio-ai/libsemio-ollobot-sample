package xyz.semio;

/**
 * Represents a function that can be applied to a given input for result
 *
 * @param <T> The parameter type
 * @param <R> The return type
 */
public interface Function<T, R> {
  public R apply(T value);
}

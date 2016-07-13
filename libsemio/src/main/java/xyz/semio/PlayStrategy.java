package xyz.semio;

public interface PlayStrategy {
  Promise<String> recognize();
  Promise<Void> emit(final InteractionState state);
}

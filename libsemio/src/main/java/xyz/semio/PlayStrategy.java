package xyz.semio;

/**
 * Represents a method for emitting and recognizing utterances
 */
public interface PlayStrategy {
  Promise<String> recognize();
  Promise<Void> emit(final InteractionState state);
}

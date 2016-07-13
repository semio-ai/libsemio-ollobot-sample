# libsemio-android
An android-based client for interacting with Semio's online API asynchronously.

# Creating a Session

`Promise<Session> Semio.createSession(String username, String password)` can be used to create a new session with Semio's Online API.

# Creating a New Interaction

An interaction is a stateful representation of an underlying dialog graph. All dialog graphs have a unique identifier that is readily seen in Semio's Studio.

After successfully obtaining a `Session`, call `Promise<Interaction> Session.createInteraction(String graphId)` to create a new `Interaction`.

To manually step through an Interaction, you may call `Promise<InteractionState> Interaction.next(String utterance)`.

# Playing an Interaction

`InteractionPlayer` allows you to easily asynchronously execute an entire dialog graph. To begin playback, call `void InteractionPlayer.play()`.

Interaction Players may use different `PlayStrategy`s, allow customization of execution. For example, one provided `PlayStrategy` is `SpeechPlayStrategy`, which uses Android's builtin Text-to-speech and Speech-to-text.

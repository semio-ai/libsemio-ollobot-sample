package xyz.semio.script;

public class Time {
  public void sleep(int milliseconds) {
    System.out.println("Sleeping...");
    try {
      Thread.sleep(milliseconds);
    } catch(final InterruptedException e) {

    }
    System.out.println("Done!");
  }
}

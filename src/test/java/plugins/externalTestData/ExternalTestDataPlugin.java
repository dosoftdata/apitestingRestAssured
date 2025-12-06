package plugins.externalTestData;

import static plugins.externalTestData.CustomGlue.*;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;

/**
 * @author Clement Mukendi
 *
 */
public class ExternalTestDataPlugin implements ConcurrentEventListener {

  public void setEventPublisher(EventPublisher publisher) {
    start();
    publisher.registerHandlerFor(TestRunStarted.class, this.runStartedHandler);
    publisher.registerHandlerFor(TestRunFinished.class, this.runFinishedHandler);
    publisher.registerHandlerFor(EmbedEvent.class, this.embedEventHandler);
  }

  private final EventHandler<EmbedEvent> embedEventHandler = new EventHandler<EmbedEvent>() {
    public void receive(EmbedEvent event) {}
  };

  private final EventHandler<TestRunStarted> runStartedHandler = new EventHandler<TestRunStarted>() {
    public void receive(TestRunStarted event) {}
  };

  private final EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
    public void receive(TestRunFinished event) { stop(); }
  };

}

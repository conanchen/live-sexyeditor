import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class HelloAction extends AnAction {
  public HelloAction() {
    super("Hello");
  }

  public void actionPerformed(AnActionEvent event) {
    Project project = event.getData(PlatformDataKeys.PROJECT);
    HelloWorldClient client = new HelloWorldClient("localhost", 42420);

    Messages.showMessageDialog(project, "Hello world! "+client.greet("Conan"), "Greeting", Messages.getInformationIcon());
  }
}
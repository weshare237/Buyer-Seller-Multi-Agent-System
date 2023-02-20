package containers;

import agents.ConsumerAgent;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConsumerContainer extends Application {

    protected ConsumerAgent consumerAgent;
    protected ObservableList<String> observableList;

    public static void main(String[] args) throws ControllerException {
        launch(args);
    }

    public void startContainer() throws StaleProxyException {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        AgentContainer container = runtime.createAgentContainer(profile);
        AgentController agentController = container.createNewAgent(
                "Consumer",
                "agents.ConsumerAgent",
                new Object[] {this}
        );
        agentController.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        startContainer();
        stage.setTitle("Consumer");

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(10);
        Label label = new Label("Book");
        TextField textField = new TextField();
        Button button = new Button("Buy");
        hBox.getChildren().addAll(label, textField, button);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));

        observableList = FXCollections.observableArrayList();

        ListView<String> listView = new ListView<>(observableList);
        vBox.getChildren().add(listView);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(hBox);
        borderPane.setCenter(vBox);

        Scene scene = new Scene(borderPane, 600, 400);
        stage.setScene(scene);
        stage.show();

        button.setOnAction(e -> {
            String book = textField.getText();
            GuiEvent event = new GuiEvent(this, 1);
            event.addParameter(book);
            consumerAgent.onGuiEvent(event);
        });
    }

    public void setConsumerAgent(ConsumerAgent consumerAgent) {
        this.consumerAgent = consumerAgent;
    }

    public void logMessage(ACLMessage aclMessage) {
        Platform.runLater(() -> {
            observableList.add(aclMessage.getContent() + " from " + aclMessage.getSender().getName());
        });
    }
}

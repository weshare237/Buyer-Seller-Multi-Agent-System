package agents;

import jade.core.ProfileImpl;
import jade.core.Runtime;
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
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BuyerGui extends Application {

    protected BuyerAgent buyerAgent;
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
                "Buyer",
                "agents.BuyerAgent",
                new Object[] {this}
        );
        agentController.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        startContainer();
        stage.setTitle("Buyer");

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));

        observableList = FXCollections.observableArrayList();

        ListView<String> listView = new ListView<>(observableList);
        vBox.getChildren().add(listView);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);

        Scene scene = new Scene(borderPane, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    public void setBuyerAgent(BuyerAgent buyerAgent) {
        this.buyerAgent = buyerAgent;
    }

    public void logMessage(ACLMessage aclMessage) {
        Platform.runLater(() -> {
            observableList.add(aclMessage.getContent() + " from " + aclMessage.getSender().getName());
        });
    }
}

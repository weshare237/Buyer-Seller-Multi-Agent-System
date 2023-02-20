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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SellerGui extends Application {

    protected SellerAgent sellerAgent;
    protected ObservableList<String> observableList;
    AgentContainer container;

    public static void main(String[] args) throws ControllerException {
        launch(args);
    }

    public void startContainer() throws ControllerException {
        Runtime runtime = Runtime.instance();
        ProfileImpl profile = new ProfileImpl();
        profile.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        container = runtime.createAgentContainer(profile);
        container.start();
    }

    @Override
    public void start(Stage stage) throws Exception {
        startContainer();
        stage.setTitle("Seller");

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10));

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(10));
        hBox.setSpacing(10);
        Label label = new Label("Seller Agent Name");
        TextField textField = new TextField();
        Button button = new Button("Deploy");
        hBox.getChildren().addAll(label, textField, button);

        observableList = FXCollections.observableArrayList();

        ListView<String> listView = new ListView<>(observableList);
        vBox.getChildren().add(listView);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        borderPane.setTop(hBox);

        Scene scene = new Scene(borderPane, 400, 300);
        stage.setScene(scene);
        stage.show();

        button.setOnAction(event -> {
            try {
                String name = textField.getText();
                AgentController agentController = container.createNewAgent(
                        name,
                        "agents.SellerAgent",
                        new Object[] {this}
                );
                agentController.start();
            } catch (StaleProxyException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void logMessage(ACLMessage aclMessage) {
        Platform.runLater(() -> {
            observableList.add(aclMessage.getContent() + " from " + aclMessage.getSender().getName());
        });
    }

    public void setSellerAgent(SellerAgent sellerAgent) {
        this.sellerAgent = sellerAgent;
    }
}

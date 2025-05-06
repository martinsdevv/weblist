package com.weblist;

import com.weblist.controller.TaskListController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Classe principal da aplicação de Gerenciamento de Tarefas.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/weblist/view/TaskList.fxml"));
        Parent root = loader.load();

        TaskListController controller = loader.getController();

        primaryStage.setTitle("Gerenciador de Tarefas");
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);

        primaryStage.setMinWidth(1024); 
        primaryStage.setMinHeight(768);

        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdown();
            }
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

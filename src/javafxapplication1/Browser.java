package JavaFXApplication1;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Browser extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Light Browser");
            stage.getIcons().add(new Image("file:Resources/isil.png"));
            stage.show();
        } catch (IOException | NullPointerException ex) {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}

package gui.managing;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
 
public class GuiLauncher  extends Application {
    
  
    @Override
    public void start(Stage primaryStage) {
        try {
            // Read file fxml and draw interface.
            Parent root = FXMLLoader.load(getClass()
                    .getResource("/gui/managing/MainGui.fxml"));
            
            primaryStage.setTitle("Money managing 2");
            Scene scene = new Scene(root);
            
            JMetro jMetro = new JMetro(Style.DARK);
            jMetro.setScene(scene);

            scene.getStylesheets().add("gui/managing/application.css");  
            primaryStage.setResizable(false);
            primaryStage.setScene(scene);
            
            primaryStage.setMinHeight(900);
            primaryStage.setMinWidth(880);
            primaryStage.setHeight(900);
            primaryStage.setWidth(880);
            primaryStage.setMaxHeight(900);
            primaryStage.setMaxHeight(880);
            //primaryStage.centerOnScreen();
            
            primaryStage.show();
         
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
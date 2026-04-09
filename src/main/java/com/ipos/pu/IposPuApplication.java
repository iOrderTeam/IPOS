package com.ipos.pu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.ipos.pu.ui.SceneManager;

@SpringBootApplication
public class IposPuApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(IposPuApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("IPOS-PU");
        javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/com/ipos/pu/ui/icon.png"));
        stage.getIcons().add(icon);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ipos/pu/ui/login.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        SceneManager.init(stage, springContext);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }
}

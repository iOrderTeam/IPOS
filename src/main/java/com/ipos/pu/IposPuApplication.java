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
    private Parent rootNode;
    private Stage stage;

    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(IposPuApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ipos/pu/ui/main.fxml"));
        loader.setControllerFactory(springContext::getBean);
        rootNode = loader.load();
        Scene scene = new Scene(rootNode, 800, 600);
        stage.setScene(scene);
        stage.setTitle("IPOS-PU");
        SceneManager.init(stage, springContext);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }
}

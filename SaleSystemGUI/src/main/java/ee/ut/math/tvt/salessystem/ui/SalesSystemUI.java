package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.logic.History;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Team;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import ee.ut.math.tvt.salessystem.ui.controllers.HistoryController;
import ee.ut.math.tvt.salessystem.ui.controllers.PurchaseController;
import ee.ut.math.tvt.salessystem.ui.controllers.StockController;
import ee.ut.math.tvt.salessystem.ui.controllers.TeamController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Graphical user interface of the sales system.
 */
public class SalesSystemUI extends Application {

    private static final Logger log = LogManager.getLogger(SalesSystemUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart shoppingCart;
    private final Warehouse warehouse;
    private final History history;
    private final Team team;

    public SalesSystemUI() {
        dao = new HibernateSalesSystemDAO();
        shoppingCart = new ShoppingCart(dao);
        warehouse = new Warehouse(dao);
        history = new History(dao);
        team = new Team();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("javaFX version: " + System.getProperty("javafx.runtime.version"));
        Tab purchaseTab = new Tab();
        purchaseTab.setText("Point-of-sale");
        purchaseTab.setClosable(false);
        purchaseTab.setContent(loadControls("PurchaseTab.fxml", new PurchaseController(dao, shoppingCart)));
        log.info("Purchase Tab is loaded");

        Tab stockTab = new Tab();
        stockTab.setText("Warehouse");
        stockTab.setClosable(false);
        StockController stockController = new StockController(dao, warehouse);
        stockTab.setContent(loadControls("StockTab.fxml", stockController));
        log.info("Stock Tab is loaded");

        Tab historyTab = new Tab();
        historyTab.setText("History");
        historyTab.setClosable(false);
        historyTab.setContent(loadControls("HistoryTab.fxml", new HistoryController(history)));
        log.info("History Tab is loaded");

        Tab teamTab = new Tab();
        teamTab.setText("Team");
        teamTab.setClosable(false);
        teamTab.setContent(loadControls("TeamTab.fxml", new TeamController(team)));
        log.info("Team Tab is loaded");

        Group root = new Group();
        Scene scene = new Scene(root, 600, 500, Color.WHITE);
        scene.getStylesheets().add(getClass().getResource("DefaultTheme.css").toExternalForm());

        BorderPane borderPane = new BorderPane();
        borderPane.prefHeightProperty().bind(scene.heightProperty());
        borderPane.prefWidthProperty().bind(scene.widthProperty());
        TabPane tabPane = new TabPane(purchaseTab, stockTab, historyTab, teamTab);
        borderPane.setCenter(tabPane);
        ((TabPane) borderPane.getCenter())
                .getSelectionModel()
                .selectedIndexProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    if (newValue.equals(1)){
                      stockController.onTabOpen();
                    }
                }));

        root.getChildren().add(borderPane);
        primaryStage.setTitle("Sales system");
        primaryStage.setScene(scene);
        primaryStage.show();

        log.info("Salesystem GUI started");
    }

    private Node loadControls(String fxml, Initializable controller) throws IOException {
        URL resource = getClass().getResource(fxml);
        if (resource == null) {
            throw new IllegalArgumentException(fxml + " not found");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setController(controller);
        return fxmlLoader.load();
    }
}



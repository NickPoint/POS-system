package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

//Todo check sold-out
//TODO Clean-up
public class StockController implements Initializable {

    private static final Logger log = LogManager.getLogger(StockController.class);

    private final SalesSystemDAO dao;
    private final Warehouse warehouse;
    //Holds information whether form was filled by barcode
    private boolean isFilledByBarcode = false;
    private boolean soldOutIsShown = true;

    @FXML
    private Button addItem;
    @FXML
    private TableView<StockItem> warehouseTableView;
    @FXML
    private Button addItemButton;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;

    public StockController(SalesSystemDAO dao, Warehouse warehouse) {
        this.dao = dao;
        this.warehouse = warehouse;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(this);
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        barCodeField.focusedProperty().addListener(($0, $1, newPropertyValue) -> {
            if (!newPropertyValue) {
                isFilledByBarcode = fillInputsBySelectedStockItem();
            }
        });
    }

    /**
     * Event handler for the {@code add Product} event
     */
    @FXML
    public void addProductButtonClicked() {
        //Probably distinguish between CLI and GUI
        log.info("Adding a product");
        try {
            log.debug("Contents of warehouse " + dao.findStockItems());
            Long idx = Long.parseLong(barCodeField.getText());
            int amount = Integer.parseInt(quantityField.getText());
            if (isFilledByBarcode) {
                warehouse.addByIdx(idx, amount);
            } else {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                warehouse.addNewItem(new StockItem(idx, name, "", price, amount));
            }
            emptyForm();
        } catch (SalesSystemException | NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            log.error(e.getMessage(), e);
            if (e instanceof NumberFormatException) {
                alert.setHeaderText("Your numeric input is wrongly formatted!");
            } else {
                alert.setHeaderText(e.getMessage());
            }
            alert.showAndWait();
        }
    }

    /**
     * Event handler for the {@code refresh} event
     */
    @FXML
    public void refreshButtonClicked() {
        warehouseTableView.refresh();
    }

    /**
     * Method that is invoked from {@link ee.ut.math.tvt.salessystem.ui.SalesSystemUI SalesSystemUI}
     * Used to show pop-up about sold-out items
     */
    public void onTabOpen() {
        if (soldOutIsShown) {
            Stream<StockItem> soldOuts = warehouse.getSoldOuts();
            String message = String.join(
                    "\n",
                    soldOuts.map(
                                    so -> String.format("%s (id: %d, amount: %d)", so.getName(), so.getId(), so.getQuantity())
                            ).toArray(String[]::new)
            );
            if (!message.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.getButtonTypes().add(new ButtonType("Do not show this message again", ButtonBar.ButtonData.NO));
                alert.setHeaderText("Some products are sold-out or soon will be!");
                alert.setTitle("Attention!");
                alert.setContentText(message);
                alert.showAndWait();
                if (alert.getResult().getButtonData() == ButtonBar.ButtonData.NO) {
                    soldOutIsShown = false;
                }
            }
        }
    }

    private boolean fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameField.setText(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
            return true; //Return true if item was successfully found by id
        }
        return false;
    }

    /**
     * Search the warehouse for a <code>StockItem<code/> with the bar code entered
     * to the <code>barCode</code> textfield.
     */
    private StockItem getStockItemByBarcode() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItem(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Reset form fields.
     */
    private void emptyForm() {
        barCodeField.setText("");
        quantityField.setText("1");
        nameField.setText("");
        priceField.setText("");
    }
}

package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.ProductValidationException;
import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

//Todo check sold-out
//TODO Clean-up
public class StockController implements Initializable {

    private static final Logger log = LogManager.getLogger(StockController.class);

    private final SalesSystemDAO dao;
    private final Warehouse warehouse;
    //Holds information whether form was filled by barcode
    private boolean isFilledByBarcode = false;

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
        warehouseTableView.setItems(FXCollections.observableList(dao.findStockItems()));
        barCodeField.focusedProperty().addListener(($0, $1, newPropertyValue) -> {
            if (!newPropertyValue) {
                isFilledByBarcode = fillInputsBySelectedStockItem();
            }
        });
        //TODO try same for name suggestions
    }

    /**
     * Event handler for the <code>add Product<code/> event
     */
    @FXML
    public void addProductButtonClicked() {
        log.info("Adding a product");
        try {
            log.debug("Contents of warehouse " + dao.findStockItems());
            Long idx = Long.parseLong(barCodeField.getText());
            int amount = Integer.parseInt(quantityField.getText());
            if (isFilledByBarcode) {
                warehouse.addByIdx(idx, amount);
            } else {
                String name = nameField.getText();
                if (name.isBlank()) {
                    throw new ProductValidationException("Product name cannot be blank!");
                }
                int price = Integer.parseInt(priceField.getText());
                warehouse.addNewItem(new StockItem(idx, name, "", price, amount));
            }
            emptyForm();
        } catch (NumberFormatException | SalesSystemException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            log.error(e.getMessage(), e);
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Event handler for the <code>refresh</code> event
     */
    @FXML
    public void refreshButtonClicked() {
        warehouseTableView.refresh();
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

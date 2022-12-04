package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.RollbackException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates everything that has to do with the warehouse tab (the tab
 * labelled "Warehouse" in the menu). Consists of the current item dialog and warehouse table.
 */
public class StockController implements Initializable {

    private static final Logger log = LogManager.getLogger(StockController.class);

    private final SalesSystemDAO dao;
    private final Warehouse warehouse;
    //Holds information whether form was filled by barcode
    private boolean isFilledByBarcode = false;
    //Holds information whether to show information window about sol-outs
    private boolean soldOutIsShown = true;

    private boolean editMode = false;

    private Long selectedItemId = -1L;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button addItem;
    @FXML
    private TableView<StockItem> warehouseTableView;
    @FXML
    private Button addItemButton;
    //    @FXML
//    private ComboBox dropDown;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;


    @FXML
    private Button deleteItemButton;

    @FXML
    private TableColumn<StockItem, String> itemPrice;

    public StockController(SalesSystemDAO dao, Warehouse warehouse) {
        this.dao = dao;
        this.warehouse = warehouse;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        warehouseTableView.setItems(FXCollections.observableList(new ArrayList<>(dao.findStockItems())));

        itemPrice.setCellValueFactory(p -> new ReadOnlyObjectWrapper(String.format("%.2f", p.getValue().getPrice())));
        setButtons(false);
        barCodeField.focusedProperty().addListener(($0, $1, newPropertyValue) -> {
            if (!newPropertyValue) {
                isFilledByBarcode = fillInputsBySelectedStockItem();
            }
        });
        warehouseTableView.getSelectionModel().selectedItemProperty().addListener(($0, $1, selected) -> {
            if (selected != null) {
                selectedItemId = selected.getBarCode();
                setButtons(true);
            } else {
                setButtons(false);
            }
        });
    }

    /**
     * Event handler for the {@code add Product} event
     */
    @FXML
    public void addProductButtonClicked() {
        log.info("Adding a product: ");
        log.debug("Contents of warehouse " + dao.findStockItems());
        log.debug(String.format("Form content: barcode: %s, name: %s, amount: %s, price: %s",
                barCodeField.getText(), nameField.getText(),
                quantityField.getText(), priceField.getText()));
        try {
            Long idx = Long.parseLong(barCodeField.getText());
            int amount = Integer.parseInt(quantityField.getText());
            if (isFilledByBarcode) {
                log.debug("Form was autofilled");
                warehouse.addByIdx(idx, amount);
            } else {
                log.debug("Form was filled automatically");
                String name = nameField.getText(); //dropdown.getText();
                double price = Double.parseDouble(priceField.getText());
                warehouse.addNewItem(new StockItem(idx, name,
//                        "",
                        price, amount));
            }
            emptyForm();
            refresh();
            log.info("Product is added to the warehouse");
            log.debug("Contents of warehouse " + dao.findStockItems());
        } catch (SalesSystemException | NumberFormatException e) {
            //TODO candidate for extraction and refactoring
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
        refresh();
    }

    /**
     * Method that is invoked from {@link ee.ut.math.tvt.salessystem.ui.SalesSystemUI SalesSystemUI}
     * Used to show pop-up about sold-out items
     */
    public void onTabOpen() {
        //Somewhat unrelated functionality,
        //yet quite handy to put it here for a slightly better UX
        refresh();
        if (soldOutIsShown) {
            log.info("Pulling sold-outs");
            log.debug("Warehouse state: " + dao.findStockItems());
            System.out.println(dao.findStockItems());
            Stream<StockItem> soldOuts = warehouse.getSoldOuts();
            String message = String.join(
                    "\n",
                    soldOuts.map(
                            so -> String.format("%s (id: %d, amount: %d)", so.getName(), so.getBarCode(), so.getQuantity())
                    ).toArray(String[]::new)
            );
            log.debug("Following sold-outs were detected " + soldOuts);
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

    @FXML
    public void deleteItemButtonClicked() {
        log.info("Deleting product from warehouse");
        log.debug("Product id: " + selectedItemId);
        refresh();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(warehouse.deleteFromStock(selectedItemId));
        alert.showAndWait();
    }

    @FXML
    public void editItemButtonClicked() {
        editMode = true;
        StockItem stockItem = dao.findStockItem(selectedItemId);
        if (stockItem != null) {
            barCodeField.setText(String.valueOf(stockItem.getBarCode()));
            nameField.setText(stockItem.getName());
            quantityField.setText(String.valueOf(stockItem.getQuantity()));
            priceField.setText(String.valueOf(stockItem.getPrice()));

        }
    }

    /**
     * TODO: Candidate for refactoring
     *
     * @return
     */
    private boolean fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameField.setText(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
            //Return true if item was successfully found by id
            return true;
        }
        return false;
    }

    /**
     * Search the warehouse for a {@code StockItem} with the bar code entered
     * to the {@code barCode} textfield.
     *
     * @return {@code StockItem} instance with the given barcode,
     * if there is no such object in DAO, {@code null} is returned instead
     */
    private StockItem getStockItemByBarcode() {
        try {
            long code = Long.parseLong(barCodeField.getText());
            return dao.findStockItem(code);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setButtons(boolean flag) {
        deleteItemButton.setVisible(flag);
        deleteItemButton.setDisable(!flag);
    }

    /**
     * Reset form fields.
     */
    private void emptyForm() {
        barCodeField.setText("");
        quantityField.setText("");
        nameField.setText("");
        priceField.setText("");
    }

    private void refresh() {
        warehouseTableView.getItems().setAll(dao.findStockItems());
    }
}

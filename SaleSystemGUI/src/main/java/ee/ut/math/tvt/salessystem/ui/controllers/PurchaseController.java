package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "Point-of-sale" in the menu). Consists of the purchase menu,
 * current purchase dialog and shopping cart table.
 */
public class PurchaseController implements Initializable {

    private static final Logger log = LogManager.getLogger(PurchaseController.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart shoppingCart;

    @FXML
    private Button newPurchase;
    @FXML
    private Button submitPurchase;
    @FXML
    private Button cancelPurchase;
    @FXML
    private TextField barCodeField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private Button addItemButton;
    @FXML
    private Button deleteButton;
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableView<SoldItem> purchaseTableView;
    @FXML
    private TableColumn<SoldItem, String> itemPrice;

    private SoldItem selected;

    @FXML
    private TableColumn<SoldItem, String> sumPrice;

    public PurchaseController(SalesSystemDAO dao, ShoppingCart shoppingCart) {
        this.dao = dao;
        this.shoppingCart = shoppingCart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        purchaseTableView.setItems(FXCollections.observableList(shoppingCart.getAll()));
        itemPrice.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getPrice())));
        deleteButton.setVisible(false);
        EventHandler<ActionEvent> clearSelection = e -> purchaseTableView.getSelectionModel().clearSelection();
        barCodeField.setOnAction(clearSelection::handle);
        nameField.setOnAction(clearSelection::handle);
        priceField.setOnAction(clearSelection::handle);
        quantityField.setOnAction(clearSelection::handle);


        itemPrice.setCellValueFactory(
                p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getPrice()))
        );
        sumPrice.setCellValueFactory(
                p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getSum()))
        );
        disableProductField(true);
        this.barCodeField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    fillInputsBySelectedStockItem();
                }
            }
        });
        purchaseTableView.getSelectionModel().selectedItemProperty().addListener(
                ($0, $1, selected) -> {
                    if (selected != null) {
                        deleteButton.setVisible(true);
                        this.selected = selected;
                    }
                }
        );
    }

    /**
     * Event handler for the {@code new purchase} event.
     */
    @FXML
    protected void newPurchaseButtonClicked() {
        log.info("New sale process started");
        try {
            enableInputs();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the {@code cancel purchase} event.
     */
    @FXML
    protected void cancelPurchaseButtonClicked() {
        log.info("Sale cancelled");
        try {
            shoppingCart.cancelCurrentPurchase();
            disableInputs();
            purchaseTableView.refresh();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Event handler for the {@code submit purchase} event.
     */
    @FXML
    protected void submitPurchaseButtonClicked() {
        log.info("Sale complete");
        try {
            log.debug("Contents of the current basket:\n" + shoppingCart.getAll());
            shoppingCart.submitCurrentPurchase();
            disableInputs();
            purchaseTableView.refresh();
        } catch (SalesSystemException e) {
            log.error(e.getMessage(), e);
        }
    }

    // switch UI to the state that allows to proceed with the purchase

    private void enableInputs() {
        resetProductField();
        disableProductField(false);
        cancelPurchase.setDisable(false);
        submitPurchase.setDisable(false);
        newPurchase.setDisable(true);
    }
    // switch UI to the state that allows to initiate new purchase

    private void disableInputs() {
        resetProductField();
        cancelPurchase.setDisable(true);
        submitPurchase.setDisable(true);
        newPurchase.setDisable(false);
        disableProductField(true);
    }

    private void fillInputsBySelectedStockItem() {
        StockItem stockItem = getStockItemByBarcode();
        if (stockItem != null) {
            nameField.setText(stockItem.getName());
            priceField.setText(String.valueOf(stockItem.getPrice()));
        } else {
            resetProductField();
        }
    }

    /**
     * Search the warehouse for a StockItem with the bar code entered
     * to the barCode textfield.
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
     * Add new item to the cart.
     */
    @FXML
    public void addItemEventHandler() {
        // add chosen item to the shopping cart.
        //print cart content
        StockItem stockItem = getStockItemByBarcode();
        log.info("Adding item to the cart");
        log.debug("Item: stockItem");
        if (stockItem != null) {
            int quantity;
            try {
                quantity = Integer.parseInt(quantityField.getText());
                shoppingCart.addItem(new SoldItem(stockItem, quantity));
                purchaseTableView.refresh();
            } catch (NumberFormatException | SalesSystemException e) {
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
    }

    @FXML
    public void deleteButtonClicked() {
        shoppingCart.deleteFromShoppingCart(selected.getBarcode());
        purchaseTableView.refresh();
    }


    /**
     * Sets whether the product component is enabled.
     */
    private void disableProductField(boolean disable) {
        this.addItemButton.setDisable(disable);
        this.barCodeField.setDisable(disable);
        this.quantityField.setDisable(disable);
        this.nameField.setDisable(disable);
        this.priceField.setDisable(disable);
    }

    /**
     * Reset dialog fields.
     */
    private void resetProductField() {
        barCodeField.setText("");
        quantityField.setText("");
        nameField.setText("");
        priceField.setText("");
    }
}

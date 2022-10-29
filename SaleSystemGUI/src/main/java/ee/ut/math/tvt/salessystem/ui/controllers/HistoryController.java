package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.logic.History;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {

    private static final Logger log = LogManager.getLogger(HistoryController.class);

    private final History history;

    @FXML
    private Button showBetweenDatesButton;
    @FXML
    private DatePicker startDate;
    @FXML
    private DatePicker endDate;
    @FXML
    private Button showLastTenButton;
    @FXML
    private Button showAllButton;
    @FXML
    private TableView<Purchase> historyTableView;
    @FXML
    private TableView<SoldItem> purchaseDetailsTableView;
    @FXML
    private TableColumn<Purchase, String> purchaseSum;
    @FXML
    private TableColumn<SoldItem, String> itemSum;

    @FXML
    private TableColumn<SoldItem, String> itemPrice;

    public HistoryController(History history) {
        this.history = history;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyTableView.getSelectionModel().selectedItemProperty().addListener(($0, $1, selected) -> {
            if (selected != null) {
                purchaseDetailsTableView.setItems(FXCollections.observableList(selected.getBoughtItems()));
                purchaseDetailsTableView.refresh();
            }
        });
        purchaseSum.setCellValueFactory(p -> new ReadOnlyObjectWrapper(String.format("%.2f", p.getValue().getSum())));
        itemSum.setCellValueFactory(p -> new ReadOnlyObjectWrapper(String.format("%.2f", p.getValue().getSum())));
        itemPrice.setCellValueFactory(p-> new ReadOnlyObjectWrapper(String.format("%.2f", p.getValue().getPrice())));
    }

    @FXML
    public void showLastTenButtonClicked() {
        log.info("Loading purchases");
        historyTableView.setItems(FXCollections.observableList(history.getLastTenPurchases()));
        purchaseDetailsTableView.setItems(null);
        purchaseDetailsTableView.refresh();
    }

    @FXML
    public void showBetweenDatesButtonClicked() {
        log.info("Loading purchases");
        LocalDate startDate = this.startDate.getValue();
        LocalDate endDate = this.endDate.getValue();
        log.debug("Start date is: " + startDate + "; " + "End date is: " + endDate);
        if (startDate != null && endDate != null) {
            historyTableView.setItems(FXCollections.observableList(history.getBetweenDates(startDate, endDate)));
            purchaseDetailsTableView.setItems(null);
            purchaseDetailsTableView.refresh();
        }
        else{
            log.error("Dates are invalid");
        }

    }

    @FXML
    public void showAllButtonClicked() {
        log.info("Loading purchases");
        historyTableView.setItems(FXCollections.observableList(history.getLastYear()));
        purchaseDetailsTableView.setItems(null);
        purchaseDetailsTableView.refresh();
    }
}

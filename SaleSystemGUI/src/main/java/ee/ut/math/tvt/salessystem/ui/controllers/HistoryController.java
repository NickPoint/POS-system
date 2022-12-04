package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {

    private static final Logger log = LogManager.getLogger(HistoryController.class);

    SalesSystemDAO dao;

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
    private TableColumn<Purchase, String> timeColumn;
    @FXML
    private TableColumn<Purchase, String> purchaseSum;
    @FXML
    private TableColumn<SoldItem, String> itemSum;

    @FXML
    private TableColumn<SoldItem, String> itemPrice;

    public HistoryController(SalesSystemDAO dao) {
        this.dao = dao;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyTableView.getSelectionModel().selectedItemProperty().addListener(($0, $1, selected) -> {
            if (selected != null) {
                purchaseDetailsTableView.setItems(FXCollections.observableList(selected.getBoughtItems()));
                purchaseDetailsTableView.refresh();
            }
        });
        startDate.setEditable(false);
        endDate.setEditable(false);
        timeColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(String.format("%1$tH:%1$tM", p.getValue().getTime())));
        purchaseSum.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getSum())));
        itemSum.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getSum())));
        itemPrice.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(String.format("%.2f", p.getValue().getPrice())));
    }

    @FXML
    public void showLastTenButtonClicked() {
        log.info("Loading last 10 purchases");
        historyTableView.setItems(FXCollections.observableList(dao.getLastTenPurchases()));
        purchaseDetailsTableView.setItems(null);
        purchaseDetailsTableView.refresh();
    }

    @FXML
    public void showBetweenDatesButtonClicked() {
        log.info("Loading purchases for a given date interval");
        LocalDate startDate = this.startDate.getValue();
        System.out.println(startDate);
        LocalDate endDate = this.endDate.getValue();
        log.debug("Start date is: " + startDate + "; " + "End date is: " + endDate);
        try {
            if (startDate == null && endDate == null) {
                throw new SalesSystemException("Dates are invalid");
            }
            historyTableView.setItems(FXCollections.observableList(dao.getBetweenDates(startDate, endDate)));
            purchaseDetailsTableView.setItems(null);
            purchaseDetailsTableView.refresh();
        } catch (SalesSystemException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            log.error(e.getMessage(), e);
            alert.setTitle("Error");
            alert.setHeaderText(e.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    public void showAllButtonClicked() {
        log.info("Loading purchases made in a year");
        historyTableView.setItems(FXCollections.observableList(dao.getLastYear()));
        purchaseDetailsTableView.setItems(null);
        purchaseDetailsTableView.refresh();
    }
}

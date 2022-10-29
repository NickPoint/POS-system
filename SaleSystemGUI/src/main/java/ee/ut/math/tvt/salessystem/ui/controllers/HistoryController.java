package ee.ut.math.tvt.salessystem.ui.controllers;

import ee.ut.math.tvt.salessystem.logic.History;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Encapsulates everything that has to do with the purchase tab (the tab
 * labelled "History" in the menu).
 */
public class HistoryController implements Initializable {

    private final History history;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: implement
    }

    public HistoryController(History history) {
        this.history = history;
    }
}

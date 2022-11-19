package ee.math.tvt.salessystem.logic;

import org.junit.Test;

public class ShoppingCartSubmitCurrentPurchaseTest {

    //  check that
    //submitting the current purchase decreases the quantity of all StockItems
    @Test
    public void testSubmittingCurrentPurchaseDecreasesStockItemQuantity() {
    }

    //    - check that
    //    submitting the current purchase decreases the quantity of all StockItems
    //    submitting the current purchase calls beginTransaction and endTransaction, exactly once and in that order

    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() {
    }

    // check that a new HistoryItem is saved and that it contains the correct SoldItems
    @Test
    public void testSubmittingCurrentOrderCreatesHistoryItem() {
    }


    //    check that  the timestamp on the created HistoryItem is set correctly (for example has only a small  difference to the current time)
    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime() {
    }

    //    check that canceling an order(with some items) and
    //    then submitting a new order (with some different items) only saves the items
    //    from the new order (with canceled items are discarded)
    @Test
    public void testCancellingOrder() {
    }

    //    check that after canceling an order the quantities of the related StockItems are not changed
    @Test
    public void testCancellingOrderQuanititesUnchanged() {
    }

}

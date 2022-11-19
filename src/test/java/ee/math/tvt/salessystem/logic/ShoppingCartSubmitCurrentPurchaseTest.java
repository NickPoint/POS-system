package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ShoppingCartSubmitCurrentPurchaseTest {
    private ShoppingCart shoppingCart;
    private Warehouse warehouse;
    private InMemorySalesSystemDAO dao;
    private static boolean invokedFirst;
    private static boolean invokedSecond;
    private static int counter = 0;

    @Before
    public void setUp() {
        dao = new MockDAO();
        shoppingCart = new ShoppingCart(dao);
        warehouse = new Warehouse(dao);
        dao.findStockItems().clear();
        dao.getPurchases().clear();
    }

    class MockDAO extends InMemorySalesSystemDAO {
        /**
         * Only legit variant goes through, so if commit and begin transactions are invoked two times, it works
         * if = invokedFirst; is = invokedSecond
         * first second     if = true, is = true
         * <p>
         * second first     if = true, is = false
         * first            if = true, is = false
         * first first      if = false, is = false
         * second           if = false, is = false
         * second second    if = false, is = false
         * -                if, is both false
         */
        @Override
        public void beginTransaction() {
            counter++;
            invokedFirst = !invokedSecond;
            super.beginTransaction();
        }

        @Override
        public void commitTransaction() {
            counter++;
            invokedSecond = invokedFirst;
            super.commitTransaction();
        }
    }

    //  check that
    //submitting the current purchase decreases the quantity of all StockItems
    @Test
    public void testSubmittingCurrentPurchaseDecreasesStockItemQuantity() {
        int initialQuantity = 3;
        StockItem stockItem1 = new StockItem(1L, "Test1", 1.0, initialQuantity);
        StockItem stockItem2 = new StockItem(2L, "Test2", 1.0, initialQuantity);
        warehouse.addNewItem(stockItem1);
        warehouse.addNewItem(stockItem2);
        int quantityInShoppingCart = 1;
        SoldItem soldItem1 = new SoldItem(stockItem1, quantityInShoppingCart);
        SoldItem soldItem2 = new SoldItem(stockItem2, quantityInShoppingCart);
        soldItem1.setId(1L);
        soldItem1.setId(2L);
        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);
        shoppingCart.submitCurrentPurchase();
        int expectedQuantity = initialQuantity - quantityInShoppingCart;
        assertEquals(expectedQuantity, dao.findStockItem(1L).getQuantity());
        assertEquals(expectedQuantity, dao.findStockItem(2L).getQuantity());
    }

    //Check that submitting the current purchase decreases the quantity of all StockItems submitting the current purchase calls beginTransaction and endTransaction, exactly once and in that order
    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() {
        StockItem stockItem = new StockItem(1l, "Test", 1.0, 1);
        warehouse.addNewItem(stockItem);
        counter = 0;
        invokedFirst = invokedSecond = false;
        SoldItem soldItem = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem);
        shoppingCart.submitCurrentPurchase();
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
    }

    //Check that a new HistoryItem is saved and that it contains the correct SoldItems
    @Test
    public void testSubmittingCurrentOrderCreatesHistoryItem() {
        StockItem stockItem1 = new StockItem(1l, "Test1", 1.0, 3);
        StockItem stockItem2 = new StockItem(2l, "Test2", 1.0, 3);
        warehouse.addNewItem(stockItem1);
        warehouse.addNewItem(stockItem2);
        SoldItem soldItem1 = new SoldItem(stockItem1, 1);
        soldItem1.setId(1L);
        SoldItem soldItem2 = new SoldItem(stockItem2, 2);
        soldItem2.setId(2L);
        List<SoldItem> items = new ArrayList<>();
        items.add(soldItem1);
        items.add(soldItem2);
        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);
        shoppingCart.submitCurrentPurchase();
        Purchase purchase;
        assertNotNull(purchase = dao.getPurchases().get(0));
        List<SoldItem> boughtItems = purchase.getBoughtItems();
        assertEquals(items.size(), boughtItems.size());
        items.forEach(item -> assertTrue(boughtItems.contains(item)));
    }

    //Check that  the timestamp on the created HistoryItem is set correctly (for example has only a small  difference to the current time)

    @Test
    public void testSubmittingCurrentOrderSavesCorrectTime() {
        StockItem stockItem = new StockItem(1l, "Test", 1.0, 1);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        soldItem.setId(1L);
        shoppingCart.addItem(soldItem);
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        shoppingCart.submitCurrentPurchase();
        Purchase purchase;
        assertNotNull(purchase = dao.getPurchases().get(0));
        LocalDate localDatePurchase = purchase.getDate();
        LocalTime localTimePurchase = purchase.getTime();
//        assertEquals(localDatePurchase.getYear(), currentDate.getYear());
//        assertEquals(localDatePurchase.getMonthValue(), currentDate.getMonthValue());
//        assertEquals(localDatePurchase.getDayOfMonth(), currentDate.getDayOfMonth());
//
//        assertEquals(localTimePurchase.getHour(), currentDate.getYear());
//        assertEquals(localTimePurchase.getMonthValue(), currentDate.getMonthValue());
//        assertEquals(localTimePurchase.getDayOfMonth(), currentDate.getDayOfMonth());
    }

    //Check that canceling an order(with some items) and then submitting a new order (with some different items) only saves the items from the new order (with canceled items are discarded)

    @Test
    public void testCancellingOrder() {
    }

    //Check that after canceling an order the quantities of the related StockItems are not changed
    @Test
    public void testCancellingOrderQuantitiesUnchanged() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 5);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem);
        shoppingCart.cancelCurrentPurchase();
        assertEquals(5, stockItem.getQuantity());
    }


}

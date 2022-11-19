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
import java.time.temporal.ChronoUnit;
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
        StockItem stockItem1 = new StockItem(1L, "Test1", 1.0, 3);
        StockItem stockItem2 = new StockItem(2L, "Test2", 1.0, 5);
        warehouse.addNewItem(stockItem1);
        warehouse.addNewItem(stockItem2);
        SoldItem soldItem1 = new SoldItem(stockItem1, 1);
        SoldItem soldItem2 = new SoldItem(stockItem2, 2);
        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);
        shoppingCart.submitCurrentPurchase();
        assertEquals(2, dao.findStockItem(1L).getQuantity());
        assertEquals(3, dao.findStockItem(2L).getQuantity());
    }

    //Check that submitting the current purchase decreases the quantity of all StockItems submitting the current purchase calls beginTransaction and endTransaction, exactly once and in that order
    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 1);
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
        StockItem stockItem1 = new StockItem(1L, "Test1", 1.0, 3);
        StockItem stockItem2 = new StockItem(2L, "Test2", 1.0, 3);
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
        shoppingCart.addItem(soldItem);
        LocalDateTime currentDateTime = LocalDateTime.now();
        shoppingCart.submitCurrentPurchase();
        Purchase purchase;
        assertNotNull(purchase = dao.getPurchases().get(0));
        LocalDate localDatePurchase = purchase.getDate();
        LocalTime localTimePurchase = purchase.getTime();
        LocalDateTime localDateTimePurchase = LocalDateTime.of(localDatePurchase, localTimePurchase);
        long diff = ChronoUnit.SECONDS.between(currentDateTime, localDateTimePurchase);
        assertTrue(diff <= 1);
    }

    //Check that canceling an order(with some items) and then submitting a new order (with some different items) only saves the items from the new order (with canceled items are discarded)
    @Test
    public void testCancellingOrder() {
        StockItem stockItem1 = new StockItem(1L, "Test1", 1, 5);
        StockItem stockItem2 = new StockItem(2L, "Test2", 1, 5);
        StockItem stockItem3 = new StockItem(3L, "Test3", 1, 5);
        warehouse.addNewItem(stockItem1);
        warehouse.addNewItem(stockItem2);
        warehouse.addNewItem(stockItem3);
        SoldItem soldItem1 = new SoldItem(stockItem1, 1);
        soldItem1.setId(1L);
        SoldItem soldItem2 = new SoldItem(stockItem2, 2);
        soldItem2.setId(2L);
        shoppingCart.addItem(soldItem1);
        shoppingCart.addItem(soldItem2);
        shoppingCart.cancelCurrentPurchase();
        //Items are absent from the cart
        assertEquals(0, shoppingCart.getAll().size());
        //There is no purchase saved in dao
        assertEquals(0, dao.getPurchases().size());

        SoldItem soldItem3 = new SoldItem(stockItem1, 2);
        soldItem3.setId(3L);
        SoldItem soldItem4 = new SoldItem(stockItem2, 2);
        soldItem4.setId(4L);
        SoldItem soldItem5 = new SoldItem(stockItem3, 3);
        soldItem5.setId(5L);
        shoppingCart.addItem(soldItem3);
        shoppingCart.addItem(soldItem4);
        shoppingCart.addItem(soldItem5);
        shoppingCart.submitCurrentPurchase();

        assertEquals(1, dao.getPurchases().size());
        List<SoldItem> boughtItems = dao.getPurchases().get(0).getBoughtItems();
        assertFalse(boughtItems.contains(soldItem1));
        assertFalse(boughtItems.contains(soldItem2));
        assertTrue(boughtItems.contains(soldItem3));
        assertTrue(boughtItems.contains(soldItem4));
        assertTrue(boughtItems.contains(soldItem5));
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

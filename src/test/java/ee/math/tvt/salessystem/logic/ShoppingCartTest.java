package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ShoppingCartTest {

    private ShoppingCart shoppingCart;
    private Warehouse warehouse;
    private InMemorySalesSystemDAO dao;
    private static boolean invokedFirst;
    private static boolean invokedSecond;
    private static int counter = 0;
    private static boolean called = false;

    @Before
    public void setUp(){
        dao = new MockDAO();
        shoppingCart = new ShoppingCart(dao);
        warehouse = new Warehouse(dao);
        dao.findStockItems().clear();
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

        @Override
        public void savePurchase(Purchase purchase) {
            called = true;
            super.savePurchase(purchase);
        }
    }
    @Test
    public void testAddingItemWithNegativeQuantity() {
        StockItem stockItem = new StockItem(30l, "Test",
//                "",
                1.0, 1);
        SoldItem soldItem = new SoldItem(stockItem, -10);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem));
    }
    @Test
    public void testSubmittingCurrentPurchaseBeginsAndCommitsTransaction(){
        counter = 0;
        invokedFirst = invokedSecond = false;
        StockItem stockItem = new StockItem(30l, "Test",
//                "",
                1.0, 1);
        warehouse.addNewItem(stockItem);
        invokedFirst = invokedSecond = false;
        counter = 0;
        SoldItem soldItem = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem);
        shoppingCart.submitCurrentPurchase();
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
    }
}

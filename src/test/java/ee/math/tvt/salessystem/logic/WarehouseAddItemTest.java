package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
//TODO: Investigate additional test suites we may need
public class WarehouseAddItemTest {
    private InMemorySalesSystemDAO dao;
    private Warehouse warehouse;
    private static boolean invokedFirst;
    private static boolean invokedSecond;
    private static int counter = 0;
    private static boolean called = false;

    @Before
    public void setUp(){
        dao = new MockDAO();
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
        public void saveStockItem(StockItem stockItem) {
            called = true;
            super.saveStockItem(stockItem);
        }
    }

    // - check that methods beginTransaction and commitTransaction are both called exactly once and
    @Test
    public void testAddingItemBeginsAndCommitsTransaction() {
        counter = 0;
        invokedFirst = invokedSecond = false;
        Long id = 1L;
        warehouse.addNewItem(new StockItem(id, "Placeholder", 2, 5));
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
        invokedFirst = invokedSecond = false;
        counter = 0;
        warehouse.addByIdx(id, 2);
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
    }

    // - check that a new item is saved through the DAO

    @Test
    public void testAddingNewItem() {
        StockItem stockItem = new StockItem(1L, "Coca-Cola", 20.5, 3);
        warehouse.addNewItem(stockItem);
        StockItem testStockItem;
        Assert.assertNotNull(testStockItem = dao.findStockItem(stockItem.getBarCode()));
        Assert.assertEquals(testStockItem.getBarCode(), stockItem.getBarCode());
        Assert.assertEquals(testStockItem.getName(), stockItem.getName());
        Assert.assertEquals(testStockItem.getPrice(), stockItem.getPrice(), 0.001);
        Assert.assertEquals(testStockItem.getQuantity(), stockItem.getQuantity());
    }

    // - check that adding a new item increases the quantity and
    // the saveStockItem method of the DAO is not called
    @Test
    public void testAddingExistingItem() {
        StockItem stockItem1 = new StockItem(30l, "SI", 1.0, 1);
        warehouse.addNewItem(stockItem1);
        called = false;
        int expected = dao.findStockItem(stockItem1.getBarCode()).getQuantity() + 1;
        warehouse.addNewItem(stockItem1);
        assertEquals(expected, dao.findStockItem(stockItem1.getBarCode()).getQuantity());
        assertFalse("Method was called", called);
    }

    //check that adding an item with negative quantity results in an exception
    @Test
    public void testAddingItemWithNegativeQuantity() {
        StockItem stockItem = new StockItem(30l, "SI",
//                "",
                1.0, -10);
        assertThrows(SalesSystemException.class, () -> warehouse.addNewItem(stockItem));
    }
}

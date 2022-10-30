package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WarehouseTest {
    private InMemorySalesSystemDAO dao;
    private Warehouse warehouse;
    private static boolean invokedFirst;
    private static boolean invokedSecond;
    private static int counter = 0;

    @Before
    public void setUp() {
        dao = new MockDAO();
        warehouse = new Warehouse(dao);
    }

    //TODO: refactor InMemorySSDAO to be mock DB when real DB connection is added after LAB 5
    class MockDAO extends InMemorySalesSystemDAO {
        /**
         * Only legit variant goes through, so if commit and begin transactions are invoked two times, it works
         * first second   if = true -> is = true
         * <p>
         * second first   is = true -> if = false
         * first          is = true -> if = false
         * second         is = false -> if = true
         * -             if == is -> both false
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

        {
            super.findStockItems().clear();
        }
    }

    // - check that methods beginTransaction and commitTransaction are both called exactly once and
    @Test
    public void testAddingItemBeginsAndCommitsTransaction() {
        invokedFirst = invokedSecond = false;
        counter = 0;
        warehouse.addByIdx(1l, 2);
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
        counter = 0;
        invokedFirst = invokedSecond = false;
        warehouse.addNewItem(dao.findStockItem(1l));
        assertTrue(counter == 2 && invokedFirst && invokedSecond);
    }

    // - check that a new item is saved through the DAO
    @Test
    public void testAddingNewItem() {
        long id = 1L;
        String name = "Coca-Cola";
        double price = 20.5;
        int quantity = 3;
        StockItem stockItem = new StockItem(id, name, price, quantity);
        warehouse.addNewItem(stockItem);
        StockItem testStockItem;
        Assert.assertNotNull(testStockItem = dao.findStockItem(id));
        Assert.assertEquals(testStockItem.getId(), stockItem.getId());
        Assert.assertEquals(testStockItem.getName(), stockItem.getName());
        Assert.assertEquals(testStockItem.getPrice(), stockItem.getPrice(), 0.001);
        Assert.assertEquals(testStockItem.getQuantity(), stockItem.getQuantity());
    }

    // - check that adding a new item increases the quantit and the saveStockItem method of the DAO is not called
    @Test
    public void testAddingExistingItem() {

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

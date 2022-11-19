package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ShoppingCartAddItemTest {
    private ShoppingCart shoppingCart;
    private Warehouse warehouse;
    private InMemorySalesSystemDAO dao;

    @Before
    public void setUp() {
        dao = new InMemorySalesSystemDAO();
        shoppingCart = new ShoppingCart(dao);
        warehouse = new Warehouse(dao);
        dao.findStockItems().clear();
    }

    //Check that shopping cart is initially empty
    @Test
    public void testShoppingCartInitiallyEmpty() {
        assertEquals("Items in the cart: ", 0, shoppingCart.getAll().size());
    }


    //Check that adding an existing item increases the quantity
    @Test
    public void testAddingExistingItem() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 5);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem);
        assertEquals("Quantity of the item in the cart ", 1, shoppingCart.getAll().get(0).getQuantity());
        SoldItem soldItem2 = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem2);
        assertEquals("Number of items in the cart ", 1, shoppingCart.getAll().size());
        assertEquals("Quantity of the item in the cart ", 2, shoppingCart.getAll().get(0).getQuantity());
    }

    //Check that the new item is added to the shopping cart
    @Test
    public void testAddingNewItem() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 1);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        warehouse.addNewItem(stockItem);
        shoppingCart.addItem(soldItem);
        assertEquals("Items in the cart: ", 1, shoppingCart.getAll().size());
        SoldItem soldItemInSC;
        soldItemInSC = shoppingCart.getAll().get(0);
        assertEquals("Added item and item in the shopping cart are equal:", soldItem, soldItemInSC);
    }


    //Check that an exception is thrown if trying to add an item with a negative quantity
    @Test
    public void testAddingItemWithNegativeQuantity() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 1);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, -1);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem));
    }

    //Check that an exception is thrown if the quantity of the added item is larger than the quantity in the warehouse
    @Test
    public void testAddingItemWithQuantityTooLarge() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 1);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, 2);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem));
    }


    //Check that an exception is thrown if the sum of the quantity of the added item and the quantity already in the shopping cart is larger than the quantity in the warehouse
    @Test
    public void testAddingItemWithQuantitySumTooLarge() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 3);
        warehouse.addNewItem(stockItem);
        SoldItem soldItem = new SoldItem(stockItem, 1);
        shoppingCart.addItem(soldItem);
        SoldItem soldItem1 = new SoldItem(stockItem, 3);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem1));
    }
}

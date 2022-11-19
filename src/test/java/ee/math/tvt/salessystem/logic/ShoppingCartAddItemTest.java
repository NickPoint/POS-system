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

public class ShoppingCartAddItemTest {
    private ShoppingCart shoppingCart;
    private Warehouse warehouse;
    private InMemorySalesSystemDAO dao;
    @Before
    public void setUp(){
        dao = new InMemorySalesSystemDAO();
        shoppingCart = new ShoppingCart(dao);
        warehouse = new Warehouse(dao);
        dao.findStockItems().clear();
    }

    //    check that
//    adding an
//    existing item
//    increases the
//    quantity
    @Test
    public void testAddingExistingItem() {

    }


    //    check that
//    the new
//    item is
//    added to
//    the shopping
//    cart
    @Test
    public void testAddingNewItem() {
    }


    //Check that an exception is thrown if trying to add an item with a negative quantity
   @Test
    public void testAddingItemWithNegativeQuantity() {
        StockItem stockItem = new StockItem(1L, "Test", 1.0, 1);
        SoldItem soldItem = new SoldItem(stockItem, -10);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem));
    }
    //    check that
//    an exception
//    is thrown if
//    the quantity
//    of the
//    added item
//    is larger
//    than the
//    quantity in
//    the warehouse
    @Test
    public void testAddingItemWithQuantityTooLarge() {
    }


    //    check that
//    an exception
//    is
//    thrown if
//    the sum
//    of the
//    quantity of
//    the added
//    item and
//    the quanti
    @Test
    public void testAddingItemWithQuantitySumTooLarge() {
    }




}

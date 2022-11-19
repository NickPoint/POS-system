package ee.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class ShoppingCartTest {

    private ShoppingCart shoppingCart;
    private InMemorySalesSystemDAO dao;

    @Before
    public void setUp(){
        dao = new InMemorySalesSystemDAO();
        shoppingCart = new ShoppingCart(dao);
    }
    @Test
    public void testAddingItemWithNegativeQuantity() {
        StockItem stockItem = new StockItem(30l, "SI",
//                "",
                1.0, -10);
        SoldItem soldItem = new SoldItem(stockItem, -10);
        assertThrows(SalesSystemException.class, () -> shoppingCart.addItem(soldItem));
    }
}

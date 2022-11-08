package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShoppingCart {

    private final SalesSystemDAO dao;
    private final List<SoldItem> items = new ArrayList<>();
    private static final Logger log = LogManager.getLogger(ShoppingCart.class);

    public ShoppingCart(SalesSystemDAO dao) {
        this.dao = dao;
    }

    /**
     * Add new {@code SoldItem} to table.
     */
    public void addItem(SoldItem item) {
        // TODO verify that warehouse items' quantity remains at least zero or throw an exception
        try {
            if (item.getQuantity() < 0) {
                throw new SalesSystemException("Product quantity cannot be negative!");
            }
            StockItem stockItem = dao.findStockItem(item.getBarcode());
            //The product we add is not yet in shopping cart, we add it now, but first we need to check the quantity
            if (item.getQuantity() > stockItem.getQuantity()) {
                throw new SalesSystemException("You cannot add more items than in stock!");
            }
            //Defensive quantity reduction, so that manipulating items in the warehouse does not result in
            // the erroneous state in the shopping cart
            stockItem.setQuantity(stockItem.getQuantity() - item.getQuantity());
            //Look for item in the items already added to shopping cart
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getBarcode() == item.getBarcode()) {
                    //Update quantity of the item
                    items.get(i).setQuantity(items.get(i).getQuantity() + item.getQuantity());
                    log.debug("Updated stock of item" + item.getName() + "in the cart, new quantity: " + item.getQuantity());
                    return;
                }
            }
            items.add(item);
            log.debug("Added " + item.getName() + " quantity of " + item.getQuantity());
        } catch (SalesSystemException e) {
            log.error(e.getMessage());
            throw e;
        }
    }


    //Change
    public String deleteFromShoppingCart(long id) {
        Iterator<SoldItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            SoldItem soldItem = iterator.next();
            if (soldItem.getBarcode() == id) {
                int existing = 0;
                StockItem stockItem1 = dao.findStockItem(id);
                if (stockItem1 != null) {
                    stockItem1.setQuantity(
                            soldItem.getQuantity()+stockItem1.getQuantity()
                    );
                }//TODO what should we do about deleted products, forget about them?
//                StockItem stockItem = new StockItem(
//                        soldItem.getBarcode(), soldItem.getName(),
//                        soldItem.getQuantity(), soldItem.getQuantity() + existing
//                );
//                dao.saveStockItem(stockItem);
                iterator.remove();
                return "Successfully deleted an item with barcode " + id;
            }
        }
        return "The item with barcode " + id + " is not in the cart!";
    }

    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        items.forEach(soldItem -> {
            StockItem stockItem = dao.findStockItem(soldItem.getBarcode());
            if (stockItem != null) {
                stockItem.setQuantity(stockItem.getQuantity() + soldItem.getQuantity());
                dao.saveStockItem(stockItem);
            }//TODO what should we do about deleted products, forget about them?
        });
        items.clear();
    }

    public void submitCurrentPurchase() {
        dao.beginTransaction();
        try {
            Purchase purchase = new Purchase(new ArrayList<>(items), LocalTime.now(), LocalDate.now());
            items.forEach(item -> item.setPurchase(purchase));
            dao.savePurchase(purchase);

            dao.commitTransaction();
            items.clear();
            log.info("Purchase is saved");
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw e;
        }
    }
}

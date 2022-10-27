package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
            if (item.getQuantity() < 0){
                throw new SalesSystemException("Product quantity cannot be negative!");
            }
            StockItem stockItem = dao.findStockItem(item.getId());
            //Look for item in the items already added to shopping cart
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getId() == item.getId()) {
                    int newQuantity = items.get(i).getQuantity() + item.getQuantity();
                    if (newQuantity > stockItem.getQuantity()){
                        throw new SalesSystemException("Max quantity exceeded!");
                    }
                    items.get(i).setQuantity(newQuantity);
                    log.debug("Updated stock of item" + item.getName() + "in the cart, new quantity: " + item.getQuantity());
                    return;
                }
            }
            //The product we add is not yet in shopping cart, we add it now, but first we need to check the quantity
            if (item.getQuantity() > stockItem.getQuantity()){
                throw new SalesSystemException("Max quantity exceeded!");
            }
            items.add(item);
            log.debug("Added " + item.getName() + " quantity of " + item.getQuantity());
        } catch (SalesSystemException e){
           log.error(e.getMessage());
            throw e;
        }
    }

    public List<SoldItem> getAll() {
        //TODO: Defensive copying???
        return items;
    }

    public void cancelCurrentPurchase() {
        items.clear();
    }

    public void submitCurrentPurchase() {
        dao.beginTransaction();
        try {
            items.forEach(dao::saveSoldItem);
            dao.commitTransaction();
            items.clear();
            log.info("Purchase is saved");
        } catch (Exception e) {
            dao.rollbackTransaction();
            throw e;
        }
    }
}

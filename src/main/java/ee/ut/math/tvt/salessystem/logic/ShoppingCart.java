package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
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
        for (int i = 0; i < items.size(); i++) {
            if(items.get(i).getId()==item.getId()){
                items.get(i).setQuantity(items.get(i).getQuantity()+item.getQuantity());
                log.debug("Updated stock of item" +item.getName()+"in the cart, new quantity: "+item.getQuantity());
                return;
            }
        }
        items.add(item);
        log.debug("Added " + item.getName() + " quantity of " + item.getQuantity());
    }

    public List<SoldItem> getAll() {
        return items;
    }

    public void cancelCurrentPurchase() {
        items.clear();
    }

    public void submitCurrentPurchase() {
        dao.beginTransaction();
        try {
            //TODO: Should we use low-level for i approach or high level declarative approach is preferred
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

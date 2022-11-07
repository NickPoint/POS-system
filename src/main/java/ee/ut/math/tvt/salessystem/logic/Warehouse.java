package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.ProductValidationException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.RollbackException;
import java.util.stream.Stream;

public class Warehouse {
    private final SalesSystemDAO dao;

    private static final Logger log = LogManager.getLogger(Warehouse.class);

    //Determines when a product is considered nearly sold-out
    private final int SOLD_OUT_MEASURE = 7;

    public Warehouse(SalesSystemDAO dao) {
        log.debug("Current sold-out quantifier: " + SOLD_OUT_MEASURE);
        this.dao = dao;
    }

    /**
     * Resupply existing {@code StockItem} by barcode
     *
     * @param idx      barcode of the product
     * @param quantity quantity of the product to be added
     * @throws ProductValidationException if {@param quantity} is negative or transaction fails
     */
    public void addByIdx(long idx, int quantity) throws ProductValidationException {
        log.debug("Adding by index: " + idx + ", quantity: " + quantity);
        try {
            dao.beginTransaction();
            if (quantity < 0) {
                throw new ProductValidationException("The quantity of the item cannot be negative!");
            }
            StockItem stockItem = dao.findStockItem(idx);
            log.debug("Quantity in the database: " + stockItem.getQuantity());
            stockItem.setQuantity(stockItem.getQuantity() + quantity);
            log.debug("New quantity: " + stockItem.getQuantity());
            dao.commitTransaction();
            log.info("The product is resupplied in the warehouse");
        } catch (ProductValidationException | RollbackException e) {
            dao.rollbackTransaction();
            log.error(e.getMessage());
            throw new ProductValidationException(e.getMessage());
        }
    }

    /**
     * Add new {@code StockItem} to the warehouse .
     */
    public void addNewItem(StockItem item) throws ProductValidationException {
        log.debug("Adding a product " + item);
        try {
            dao.beginTransaction();
            if (item.getQuantity() < 0) {
                throw new ProductValidationException("The quantity of the item cannot be negative!");
            }
            if (item.getPrice() < 0) {
                throw new ProductValidationException("The price of the item cannot be negative!");
            }
            if (item.getName().isBlank()) {
                throw new ProductValidationException("Product name cannot be blank!");
            }
            log.debug("Validation is passed, checking for item with teh same barcode in database");
            StockItem stockItem = dao.findStockItem(item.getBarCode());
            //TODO probably separate
            if (stockItem != null) {
                log.debug("The item with the same barcode is found: " + stockItem);
                if (!(stockItem.getName().equals(item.getName()) && stockItem.getPrice() == item.getPrice())) {
                    log.error("Fields of item in database and item added by the same barcode do not match!");
                    throw new ProductValidationException(
                            "Product with given ID already exists in the system, yet other fields do not match!"
                    );
                }
                log.info("The product already existed in the database! Updating quantity:");
                log.debug("Quantity in the database: " + stockItem.getQuantity());
                stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
                log.debug("New quantity: " + stockItem.getQuantity());
                log.info("Product is resupplied in the warehouse");
            } else {
                dao.saveStockItem(item);
                log.info("New product is added to the database");
            }
            dao.commitTransaction();
        } catch (ProductValidationException | RollbackException e) {
            dao.rollbackTransaction();
            log.error(e.getMessage());
            throw new ProductValidationException(e.getMessage());
        }
    }


    public String deleteFromStock(long id) {
        dao.beginTransaction();
        try {
            boolean b = dao.deleteItem(id);
            dao.commitTransaction();
            return "Item with index " + id + (b ? " is removed from the warehouse." : " is not in the warehouse!");
        } catch (RollbackException e) {
            log.error("Failed to remove item, transaction failed");
            log.error(e.getMessage());
            dao.rollbackTransaction();
        }
        return "Failed to remove an item from the warehouse!";
    }

    //TODO: Migrate to JPQL and cache the result
    public Stream<StockItem> getSoldOuts() {
        return dao.findStockItems()
                .stream()
                .filter(i -> i.getQuantity() < SOLD_OUT_MEASURE);
    }
}





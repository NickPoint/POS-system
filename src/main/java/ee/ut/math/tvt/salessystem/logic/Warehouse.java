package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.ProductValidationException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Warehouse {
    private final SalesSystemDAO dao;

    //Determines when a product is considered nearly sold-out
    private final int SOLD_OUT_MEASURE = 7;

    public Warehouse(SalesSystemDAO dao) {
        this.dao = dao;
    }


    /**
     * Resupply existing <code>StockItem</code> by index
     */
    public void addByIdx(long idx, int quantity) throws ProductValidationException {
        if (quantity < 0) {
            throw new ProductValidationException("The quantity of the item cannot be negative!");
        }
        StockItem stockItem = dao.findStockItem(idx);
        stockItem.setQuantity(stockItem.getQuantity() + quantity);
        dao.saveStockItem(stockItem);
    }

    /**
     * Add new <code>StockItem</code> to the warehouse .
     */
    public void addNewItem(StockItem item) throws ProductValidationException {
        //log.debug("Adding a product "+item);
        if (item.getQuantity() < 0) {
            throw new ProductValidationException("The quantity of the item cannot be negative!");
        }
        if (item.getPrice() < 0) {
            throw new ProductValidationException("The price of the item cannot be negative!");
        }
        if (item.getName().isBlank()) {
            throw new ProductValidationException("Product name cannot be blank!");
        }
        StockItem stockItem = dao.findStockItem(item.getId());
        //TODO probably separate
        if (stockItem != null) {
            if (!stockItem.getName().equals(item.getName()) ||
                    stockItem.getPrice() != item.getPrice()
//                    || !stockItem.getDescription().equals(item.getDescription())
            ) {
                throw new ProductValidationException("Product with given ID already exists in the system, yet other fields do not match!");
            }
            stockItem.setQuantity(stockItem.getQuantity() + item.getQuantity());
            dao.saveStockItem(stockItem);
        } else {
            dao.saveStockItem(item);
        }
        //log.info("Added product "+item);
    }

    //TODO Return Stream instead of List and change processing accordingly
    public List<StockItem> getSoldOuts() {
        return dao
                .findStockItems()
                .stream()
                .filter(i -> i.getQuantity() < SOLD_OUT_MEASURE)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}





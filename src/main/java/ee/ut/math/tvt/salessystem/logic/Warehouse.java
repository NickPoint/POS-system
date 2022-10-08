package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

public class Warehouse {
    private final SalesSystemDAO dao;

    public Warehouse(SalesSystemDAO dao) {
        this.dao = dao;
    }


    //Deal with exception in CLI and GUI controller
    public void addByIdx(long idx, int quantity) throws ProductValidationException {
        if (quantity < 0) {
            throw new ProductValidationException("The quantity of the item cannot be negative!");
        }
        StockItem stockItem = dao.findStockItem(idx);
        stockItem.setQuantity(stockItem.getQuantity() + quantity);
        dao.saveStockItem(stockItem);
    }

    /**
     * Add new StockItem to the Warehouse .
     */
    public void addNewItem(StockItem item) throws ProductValidationException {
        if (item.getQuantity() < 0) {
            throw new ProductValidationException("The quantity of the item cannot be negative!");
        }
        if (item.getPrice() < 0) {
            throw new ProductValidationException("The price of the item cannot be negative!");
        }
        StockItem stockItem = dao.findStockItem(item.getId());
        if (stockItem != null) {
            if (!stockItem.getName().equals(item.getName()) ||
                    stockItem.getPrice() != item.getPrice() ||
                    !stockItem.getDescription().equals(item.getDescription())) {
                throw new ProductValidationException("Product with given ID already exists in the system, yet other fields do not match!");
            }else {
                stockItem.setQuantity(stockItem.getQuantity()+item.getQuantity());
                dao.saveStockItem(stockItem);
            }
        } else {
            dao.saveStockItem(item);
        }
    }

    //query sold-outs
    //Track sold-outs

}
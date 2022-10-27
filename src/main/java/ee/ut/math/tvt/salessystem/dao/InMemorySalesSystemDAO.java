package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private final List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;
    private final List<Purchase> purchaseList;

    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<>();
        items.add(new StockItem(1L, "Lays chips",
//                "Potato chips",
                11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups",
//                "Sweets",
                8.0, 8));
        items.add(new StockItem(3L, "Frankfurters",
//                "Beer sauseges",
                15.0, 12));
        items.add(new StockItem(4L, "Free Beer",
//                "Student's delight",
                0.0, 100));
        this.stockItemList = items;
        this.soldItemList = new ArrayList<>();
        this.purchaseList = new ArrayList<>();
    }

    @Override
    public List<StockItem> findStockItems() {
        return stockItemList;
    }

    @Override
    public StockItem findStockItem(long id) {
        for (StockItem item : stockItemList) {
            if (item.getId() == id)
                return item;
        }
        return null;
    }

    @Override
    public void saveSoldItem(SoldItem item) {
        for (int i = 0; i < stockItemList.size(); i++) {
            if(stockItemList.get(i).getId().equals(item.getId())){
                stockItemList.get(i).setQuantity(stockItemList.get(i).getQuantity()-item.getQuantity());
                break;
            }
        }
        soldItemList.add(item);
    }

    public void finalisePurchase(){
        purchaseList.add(new Purchase(soldItemList, LocalTime.now(), LocalDate.now()));
        soldItemList.clear();
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        for (int i = 0; i < stockItemList.size(); i++) {
            if(stockItemList.get(i).getId().equals(stockItem.getId())){
                stockItemList.set(i, stockItem);
                return;
            }
        }
        stockItemList.add(stockItem);
    }

    @Override
    public List<Purchase> getPurchases() {
        return purchaseList;
    }

    @Override
    public void beginTransaction() {
    }

    @Override
    public void rollbackTransaction() {
        soldItemList.clear();
    }

    @Override
    public void commitTransaction() {
    }

    @Override
    public boolean deleteItem(Long id) {
        return stockItemList.removeIf(stockItem -> stockItem.getId().equals(id));
    }
}

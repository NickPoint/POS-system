package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private final List<StockItem> stockItemList;
    private final List<SoldItem> soldItemList;
    private final List<Purchase> purchaseList;

    public InMemorySalesSystemDAO() {
        List<StockItem> items = new ArrayList<>();
//        items.add(new StockItem(1L, "Lays chips",
////                "Potato chips",
//                11.0, 5));
//        items.add(new StockItem(2L, "Chupa-chups",
////                "Sweets",
//                8.0, 8));
//        items.add(new StockItem(3L, "Frankfurters",
////                "Beer sauseges",
//                15.0, 12));
//        items.add(new StockItem(4L, "Free Beer",
////                "Student's delight",
//                0.0, 100));
        this.stockItemList = items;
        this.soldItemList = new ArrayList<>();
        this.purchaseList = new ArrayList<>();
        items.add(new StockItem(1L, "Lays chips", 11.0, 5));
        items.add(new StockItem(2L, "Chupa-chups", 8.0, 8));
        items.add(new StockItem(3L, "Frankfurters", 15.0, 12));
        items.add(new StockItem(4L, "Free Beer", 0.0, 100));
        items.add(new StockItem(5L, "Sussy baka", 3.0, 91));
        items.add(new StockItem(6L, "Los pollos hermanos", 80, 1));
        items.add(new StockItem(7L, "Aboltus", 7.0, 1));
        items.add(new StockItem(8L, "我们喜欢茶", 0.0, 8));
        items.add(new StockItem(9L, "Eboldas", 0.0, 1));
        items.add(new StockItem(10L, "Kebab", 6.90, 3));
        items.add(new StockItem(11L, "Noodles", 5.50, 2));
        items.add(new StockItem(12L, "Sirka dish", 11.0, 3));
        items.add(new StockItem(13L, "Lasagne", 5.80, 4));
        items.add(new StockItem(14L, "Mao pao chicken", 6.50, 1));
        items.add(new StockItem(15L, "我们喜欢咖啡", 44, 44));
        generateHistory();
    }


    private void generateHistory() {
        int currentYear = LocalDate.now().getYear();
        //Majority is more than a year old
        generateHistory(currentYear, 3, 1, 12);
        //Majority is up to a year old
        generateHistory(currentYear, 1, 0, 7);
    }

    /**
     * Generate dummy purchase history
     *
     * @param year   a year around which we build an interval
     * @param deltaS defines start of the date interval
     * @param deltaE defines end of the date interval
     * @param n      number of purchases to add
     */
    private void generateHistory(int year, int deltaS, int deltaE, int n) {
        var current = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            var date = LocalDate.of(
                    current.nextInt(year - deltaS, year - deltaE + 1),
                    current.nextInt(1, 13),
                    current.nextInt(1, 29)
            );
            var time = LocalTime.of(
                    current.nextInt(0, 24),
                    current.nextInt(0, 60)
            );
            Map<Long, SoldItem> purchase = new HashMap<>();
            int numberOfProducts = current.nextInt(1, 15);
            for (int j = 0; j < numberOfProducts; j++) {
                int choice = current.nextInt(0, stockItemList.size());
                Long id = stockItemList.get(choice).getId();
                int quantity = current.nextInt(1, 33);
                purchase.putIfAbsent(id, new SoldItem(stockItemList.get(choice), quantity));
                purchase.computeIfPresent(id, (key, oldItem) -> {
                    oldItem.setQuantity(oldItem.getQuantity() + quantity);
                    return oldItem;
                });
            }
            purchaseList.add(new Purchase(new ArrayList<>(purchase.values()), time, date));
        }
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
//        for (int i = 0; i < stockItemList.size(); i++) {
//            if(stockItemList.get(i).getId().equals(item.getId())){
//                stockItemList.get(i).setQuantity(stockItemList.get(i).getQuantity()-item.getQuantity());
//                break;
//            }
//        }
        soldItemList.add(item);
    }


    public void finalisePurchase() {
        purchaseList.add(new Purchase(soldItemList, LocalTime.now(), LocalDate.now()));
        soldItemList.clear();
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        for (int i = 0; i < stockItemList.size(); i++) {
            if (stockItemList.get(i).getId().equals(stockItem.getId())) {
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

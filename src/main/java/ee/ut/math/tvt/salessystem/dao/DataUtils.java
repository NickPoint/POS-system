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

public class Utils {

    /**
     *
     * @param dao
     */
    public static void populateDAO(SalesSystemDAO dao){
        populateWarehouse(dao);
        populateHistory(dao);
    }

    /**
     *
     * @param dao
     */
    private static void populateWarehouse(SalesSystemDAO dao){
        dao.saveStockItem(new StockItem(1L, "Lays chips", 11.0, 5));
        dao.saveStockItem(new StockItem(2L, "Chupa-chups", 8.0, 8));
        dao.saveStockItem(new StockItem(3L, "Frankfurters", 15.0, 12));
        dao.saveStockItem(new StockItem(4L, "Free Beer", 0.0, 100));
        dao.saveStockItem(new StockItem(5L, "Sussy baka", 3.0, 91));
        dao.saveStockItem(new StockItem(6L, "Los pollos hermanos", 80, 1));
        dao.saveStockItem(new StockItem(7L, "Aboltus", 7.0, 1));
        dao.saveStockItem(new StockItem(8L, "我们喜欢茶", 0.0, 8));
        dao.saveStockItem(new StockItem(9L, "Eboldas", 0.0, 1));
        dao.saveStockItem(new StockItem(10L, "Kebab", 6.90, 3));
        dao.saveStockItem(new StockItem(11L, "Noodles", 5.50, 2));
        dao.saveStockItem(new StockItem(12L, "Sirka dish", 11.0, 3));
        dao.saveStockItem(new StockItem(13L, "Lasagne", 5.80, 4));
        dao.saveStockItem(new StockItem(14L, "Mao pao chicken", 6.50, 1));
        dao.saveStockItem(new StockItem(15L, "我们喜欢咖啡", 44, 44));
    }

    /**
     *
     * @param dao
     */
    private static void populateHistory(SalesSystemDAO dao){
        int currentYear = LocalDate.now().getYear();
        //Majority is more than a year old
        generateHistory(dao, currentYear, 3, 1, 12);
        //Majority is up to a year old
        generateHistory(dao, currentYear, 1, 0, 7);
    }

    /**
     * Generate dummy purchase history
     *
     * @param year   a year around which we build an interval
     * @param deltaS defines start of the date interval
     * @param deltaE defines end of the date interval
     * @param n      number of purchases to add
     */
    private static void generateHistory(SalesSystemDAO dao, int year, int deltaS, int deltaE, int n) {
        List<StockItem> stockItemList = dao.findStockItems();
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
            dao.savePurchase(new Purchase(new ArrayList<>(purchase.values()), time, date));
        }
    }

}

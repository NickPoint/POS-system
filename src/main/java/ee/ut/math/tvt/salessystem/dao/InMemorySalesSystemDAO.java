package ee.ut.math.tvt.salessystem.dao;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InMemorySalesSystemDAO implements SalesSystemDAO {

    private final List<StockItem> stockItemList;
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
        this.purchaseList = new ArrayList<>();
        DataUtils.populateDAO(this);
    }


    @Override
    public void savePurchase(Purchase purchase) {
        purchaseList.add(purchase);
    }

    @Override
    public List<StockItem> findStockItems() {
        return stockItemList;
    }

    @Override
    public StockItem findStockItem(long id) {
        for (StockItem item : stockItemList) {
            if (item.getBarCode() == id)
                return item;
        }
        return null;
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
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
    public void rollbackTransaction() {}

    @Override
    public void commitTransaction() {
    }

    @Override
    public boolean deleteItem(long id) {
        return stockItemList.removeIf(stockItem -> stockItem.getBarCode() == id);
    }

    /**
     * @return List of the last 10 purchases made
     */
    @Override
    public List<Purchase> getLastTenPurchases() {
        List<Purchase> purchases = this.getPurchases();
        //Temporary measure, using SQL filtering and querying should yield better performance
        purchases.sort(Comparator.comparing((Purchase purchase) -> LocalDateTime.of(purchase.getDate(), purchase.getTime())));
        return purchases.subList(Math.max(purchases.size() - 10, 0), purchases.size());
    }


    /**
     * @param start date after which we
     * @param end   date before which
     * @return list of purchases between two given dates
     * @throws SalesSystemException exception is thrown if end date comes before start date
     */
    @Override
    public List<Purchase> getBetweenDates(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new SalesSystemException("Start date is Before the end date!");
        }
        Predicate<LocalDate> isBetween = date -> date.isAfter(start) && date.isBefore(end);
        return getWithPredicate(purchase -> isBetween.test(purchase.getDate()));
    }

    /**
     * @return list of purchases up to one year old
     */
    @Override
    public List<Purchase> getLastYear() {
        LocalDate aYearAgo = LocalDate.now().minusYears(1L);
        return getWithPredicate(purchase -> purchase.getDate().isAfter(aYearAgo));
    }

    /**
     * @param p predicate based on which Purchases are filtered
     * @return list of products matching the predicate
     */

    private List<Purchase> getWithPredicate(Predicate<Purchase> p) {
        return this.getPurchases()
                .stream()
                .filter(p::test)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}

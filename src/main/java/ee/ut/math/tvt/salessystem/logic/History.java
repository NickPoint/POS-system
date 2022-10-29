package ee.ut.math.tvt.salessystem.logic;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class History {
    private final SalesSystemDAO dao;

    private static final Logger log = LogManager.getLogger(History.class);

    public History(SalesSystemDAO dao) {
        this.dao = dao;
    }

    /**
     * @return List of the last 10 purchases made
     */
    public List<Purchase> getLastTenPurchases(){
        List<Purchase> purchases = dao.getPurchases();
        return purchases.subList(purchases.size() - 10, purchases.size());
    }

    /**
     * @param start date after which we
     * @param end date before which
     * @throws SalesSystemException exception is thrown if end date comes before start date
     * @return list of purchases between two given dates
     */
    public List<Purchase> getBetweenDates(LocalDate start, LocalDate end){
        if(start.isAfter(end)){
            throw new SalesSystemException("Start date is Before the end date!");
        }
        Predicate<LocalDate> isBetween = date -> date.isAfter(start) && date.isBefore(end);
        return getWithPredicate(purchase -> isBetween.test(purchase.getDate()));
    }

    /**
     * @return list of purchases up to one year old
     */
    public List<Purchase> getLastYear(){
        LocalDate aYearAgo = LocalDate.now().minusYears(1L);
        return getWithPredicate(purchase -> purchase.getDate().isAfter(aYearAgo));
    }

    /**
     * @param p predicate based on which Purchases are filtered
     * @return list of products matching the predicate
     */
    private List<Purchase> getWithPredicate(Predicate<Purchase> p){
        return dao.getPurchases()
                .stream()
                .filter(p::test)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}

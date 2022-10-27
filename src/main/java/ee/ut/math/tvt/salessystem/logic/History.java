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
     *
     * @return
     */
    public List<Purchase> getLastTenPurchases(){
        List<Purchase> purchases = dao.getPurchases();
        return purchases.subList(purchases.size()  - 10, purchases.size());
    }



    /**
     *
     * @param p
     * @return
     */
    private List<Purchase> getWithPredicate(Predicate<Purchase> p){
        return dao.getPurchases()
                .stream()
                .filter(p::test)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}

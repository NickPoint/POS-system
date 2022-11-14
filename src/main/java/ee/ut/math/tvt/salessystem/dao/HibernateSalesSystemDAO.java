package ee.ut.math.tvt.salessystem.dao;


import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class HibernateSalesSystemDAO implements SalesSystemDAO {
    private final EntityManagerFactory emf;
    private final EntityManager em;

    public HibernateSalesSystemDAO() {
        emf = Persistence.createEntityManagerFactory("pos");
        em = emf.createEntityManager();
        DataUtils.populateDAO(this);
    }

    // TODO implement missing methods
    public void close() {
        em.close();
        emf.close();
    }

    @Override
    public void beginTransaction() {
        em.getTransaction().begin();
    }

    @Override
    public void rollbackTransaction() {
        em.getTransaction().rollback();
    }

    @Override
    public void commitTransaction() {
        em.getTransaction().commit();
    }

    @Override
    public List<StockItem> findStockItems() {
        return em.createQuery("FROM StockItem", StockItem.class).getResultList();
    }

    @Override
    public List<Purchase> getPurchases() {
        return em.createQuery("FROM Purchase", Purchase.class).getResultList();
    }

    @Override
    public StockItem findStockItem(long id) {
        return em.find(StockItem.class, id);
    }

    @Override
    public void saveStockItem(StockItem stockItem) {
        em.persist(stockItem);
    }


    @Override
    public void savePurchase(Purchase purchase) {
        em.persist(purchase);
    }

    @Override
    public boolean deleteItem(long id) {
        StockItem stockItem = em.find(StockItem.class, id);
        if (stockItem == null) {
            return false;
        }
        em.remove(stockItem);
        return true;
    }


    @Override
    public List<Purchase> getBetweenDates(LocalDate startDate, LocalDate endDate) {
        return em.createQuery("FROM Purchase p where p.date BETWEEN :startDate AND :endDate", Purchase.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    @Override
    public List<Purchase> getLastYear() {
        LocalDate lastYearPurchases = LocalDate.now().minusYears(1);
        return em.createQuery("FROM Purchase p where p.date BETWEEN LocalDate.now() AND lastYearPurchases:", Purchase.class).getResultList();
    }

    @Override
    public List<Purchase> getLastTenPurchases() {
        return em.createNativeQuery("SELECT * FROM Purchase ORDER BY date_of_purchase DESC, time_of_purchase DESC LIMIT 10", Purchase.class).getResultList();
    }


}

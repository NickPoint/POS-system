package ee.ut.math.tvt.salessystem.dataobjects;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
public class Purchase {
    @Id
    @GeneratedValue
//    @GenericGenerator(name="autoincrement" , strategy="increment")
//    @GeneratedValue(generator="autoincrement")
    private long id;

//    @OneToMany(cascade=CascadeType.ALL)
//    @JoinColumn(name = "ID")
    @OneToMany(
            targetEntity=SoldItem.class, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true
    )
    private List<SoldItem> boughtItems;

    @Column(name="TIME_OF_PURCHASE")
    private LocalTime time;

    @Column(name="DATE_OF_PURCHASE")
    private LocalDate date;

//    @Transient
//    @Formula("SELECT SUM(price) FROM SOLDITEM")
//    @Formula(value = "(select sum(b.sum) from STOCK_ITEM b where b.purchase.id = id)")
    @Transient
    //SELECT a, b FROM Author a JOIN a.books b
    private double sum;
    //Potentially use formula

    public Purchase() {

    }


    public Purchase(List<SoldItem> boughtItems, LocalTime time, LocalDate date) {
        this.boughtItems = boughtItems;
        this.time = time;
        this.date = date;
        this.sum = boughtItems.stream().mapToDouble(SoldItem::getSum).sum();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getSum() {
        return sum;
    }

    public List<SoldItem> getBoughtItems() {
        return boughtItems;
    }

    public void setBoughtItems(List<SoldItem> boughtItems) {
        this.boughtItems = boughtItems;
        this.sum = boughtItems.stream().mapToDouble(SoldItem::getSum).sum();
    }

    public LocalTime getTime() {
        return time;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Purchase, time=" + time +
                ", date=" + date +
                ", sum=" + sum;
    }
}

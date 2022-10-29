package ee.ut.math.tvt.salessystem.dataobjects;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Purchase {
    private List<SoldItem> boughtItems;
    private LocalTime time;
    private LocalDate date;
    private double sum;

    public Purchase(List<SoldItem> boughtItems, LocalTime time, LocalDate date) {
        this.boughtItems = boughtItems;
        this.time = time;
        this.date = date;
        this.sum = boughtItems.stream().mapToDouble(SoldItem::getSum).sum();
    }

    public double getSum() {
        return sum;
    }
    public List<SoldItem> getBoughtItems() {
        return boughtItems;
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

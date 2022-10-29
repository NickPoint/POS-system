package ee.ut.math.tvt.salessystem.dataobjects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class Purchase {
    private List<SoldItem> boughtItems;
    private LocalTime time;
    private LocalDate date;

    public Purchase(List<SoldItem> boughtItems, LocalTime time, LocalDate date) {
        this.boughtItems = boughtItems;
        this.time = time;
        this.date = date;
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
}
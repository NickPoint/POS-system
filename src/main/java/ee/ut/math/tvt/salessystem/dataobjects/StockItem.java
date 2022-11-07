package ee.ut.math.tvt.salessystem.dataobjects;

import javax.persistence.*;

//TODO change toString method
/**
 * Stock item.
 */
@Entity
@Table(name = "STOCK_ITEM")
public class StockItem {
    @Id
    private long barCode;
    private String name;
    private double price;
    private int quantity;

    public StockItem() {

    }

    public StockItem(long barCode, String name, double price, int quantity) {
        this.barCode = barCode;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getBarCode() {
        return barCode;
    }

    public void setBarCode(long barCode) {
        this.barCode = barCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("StockItem{barCode=%d, name='%s'}", barCode, name);
    }
}

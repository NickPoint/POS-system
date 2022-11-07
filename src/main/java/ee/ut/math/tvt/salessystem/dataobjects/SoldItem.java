package ee.ut.math.tvt.salessystem.dataobjects;


import javax.persistence.*;

/**
 * Already bought StockItem. SoldItem duplicates name and price for preserving history.
 */
@Entity
@Table(name = "SOLD_ITEM")
public class SoldItem {

    @Id
    @GeneratedValue
    private long id;

    private long barcode;
//    @OneToOne
//    private StockItem stockItem;
    private String name;
    private int quantity;


    //TODO:
    // Delete bidirectional mapping
    @ManyToOne
    @JoinColumn(name = "Purchase_ID", nullable = false)
    private Purchase purchase;

    @Transient
    private double price;

    public SoldItem() {
    }

    public SoldItem(StockItem stockItem, int quantity) {
        this.barcode = stockItem.getBarCode();
//        this.stockItem = stockItem;
        this.name = stockItem.getName();
        this.price = stockItem.getPrice();
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBarcode() {
        return barcode;
    }

    public void setBarcode(long barcode) {
        this.barcode = barcode;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSum() {
        return price * quantity;
    }

//    public StockItem getStockItem() {
//        return stockItem;
//    }
//
//    public void setStockItem(StockItem stockItem) {
//        this.stockItem = stockItem;
//    }


    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    @Override
    public String toString() {
        return String.format("SoldItem{barcode=%d, name='%s'}", barcode, name);
    }
}

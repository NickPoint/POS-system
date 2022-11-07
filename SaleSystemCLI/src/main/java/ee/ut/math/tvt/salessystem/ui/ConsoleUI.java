package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.HibernateSalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.Purchase;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Team;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A simple CLI (limited functionality).
 */
/*
* TODO:
* cosmetic changes:
*  - add -- to all commands
*  - extend == in CLI welcome window
* minute changes:
*  - refactor some logic into separate methods
*/
public class ConsoleUI {
    private static final Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;
    private final Warehouse warehouse;
    private final Team team;
    private List<Purchase> lastWatchedPurchasesList = new ArrayList<>();

    public ConsoleUI(SalesSystemDAO dao) {
        this.dao = dao;
        this.cart = new ShoppingCart(dao);
        this.warehouse = new Warehouse(dao);
        this.team = new Team();
    }

    public static void main(String[] args) throws Exception {
        log.info("Starting up the sales system CLI");
        SalesSystemDAO dao = new HibernateSalesSystemDAO();
        ConsoleUI console = new ConsoleUI(dao);
        console.run();
    }

    /**
     * Run the sales system CLI.
     */
    public void run() throws IOException {
        System.out.println("===========================");
        System.out.println("=       Sales System      =");
        System.out.println("===========================");
        printUsage();
        showSoldOutItems();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            processCommand(in.readLine().trim().toLowerCase());
            System.out.println("Done. ");
        }
    }

    private void showStock() {
        List<StockItem> stockItems = dao.findStockItems();
        System.out.println("-------------------------");
        for (StockItem si : stockItems) {
            System.out.println(si.getBarCode() + " " + si.getName() + " " + si.getPrice() + "Euro (" + si.getQuantity() + " items)");
        }
        if (stockItems.size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showCart() {
        System.out.println("-------------------------");
        for (SoldItem si : cart.getAll()) {
            System.out.println(si.getName() + " " + si.getPrice() + "Euro (" + si.getQuantity() + " items)");
        }
        if (cart.getAll().size() == 0) {
            System.out.println("\tNothing");
        }
        System.out.println("-------------------------");
    }

    private void showTeam() {
        System.out.println("-------------------------");
        System.out.println("Team name   " + team.getTeamName());
        System.out.println("Team leader   " + team.getTeamLeader());
        System.out.println("Team leader email   " + team.getTeamLeaderEmail());
        System.out.println("Team members   " + String.join(", ", team.getTeamMembers()));
        System.out.println("-------------------------");
    }

    private void addByBarcode(String[] c) {
        log.info("Received tokens for adding item by barcode");
        try {
            log.debug("Received following tokens: " + Arrays.toString(c));
            long idx = Long.parseLong(c[1]);
            int amount = Integer.parseInt(c[2]);
            StockItem item = dao.findStockItem(idx);
            if (item != null) {
                warehouse.addByIdx(idx, amount);
            } else {
                System.out.println("no stock item with id " + idx);
            }
        } catch (SalesSystemException | NoSuchElementException e) {
            log.error(e.getMessage());
        }
    }

    private void showSoldOutItems() {
        Stream<StockItem> soldOuts = warehouse.getSoldOuts();
        String message = String.join(
                "\n",
                soldOuts
                        .map(so -> String.format("%s (id: %d, amount: %d)", so.getName(), so.getBarCode(), so.getQuantity()))
                        .toArray(String[]::new));
        if (!message.isBlank()) {
            System.out.println("Nearly sold out items are:");
            System.out.println(message);
        }
    }

    private void resupplyNewItem(String[] info) {
        log.info("Received info for item to resupply");
        try {
            log.debug("Received following tokens: " + Arrays.toString(info));
            long idx = Long.parseLong(info[1]);
            String name = info[2];
            double price = Double.parseDouble(info[3]);
            int amount = Integer.parseInt(info[4]);
            StockItem newItem = new StockItem(idx, name,
//                    "",
                    price, amount);
            warehouse.addNewItem(newItem);
        } catch (NumberFormatException | SalesSystemException e) {
            log.error(e.getMessage());
            if (e instanceof NumberFormatException) {
                System.out.println("Your numeric input is wrongly formatted!");
            } else {
                System.out.println(e.getMessage());
            }
        }
    }

    private void showHistoryBetweenDates(LocalDate firstDate, LocalDate secondDate) {
        this.lastWatchedPurchasesList = dao.getBetweenDates(firstDate, secondDate);
        printPurchaseTable();
    }

    private void showLastTenPurchases() {
//        this.lastWatchedPurchasesList = dao.getLastTenPurchases();
        //TODO: Delete, used it for setting up the database
        this.lastWatchedPurchasesList = dao.getLastTenPurchases();
        printPurchaseTable();
    }

    private void showAllUpToOneYear() {
        this.lastWatchedPurchasesList = dao.getLastYear();
        printPurchaseTable();
    }

    private void printPurchaseTable() {
        int i = 0;
        System.out.println("IDX   Date:       Time:  Total:");
        for (Purchase purchase : lastWatchedPurchasesList) {
            System.out.printf("%-5d %2$tY-%2$tm-%2$td  %3$tH:%3$tM  %4$.2f\n", i++, purchase.getDate(), purchase.getTime(), purchase.getSum());
        }
    }

    private int numberOfDigits(double number) {
        return (int) Math.floor(Math.log10(number)) + 1;
    }

    private void showDetails(int idx) {
        if (idx < 0 || idx >= lastWatchedPurchasesList.size()) {
            System.out.println("Invalid index!");
        }
        Purchase purchase = lastWatchedPurchasesList.get(idx);
        List<SoldItem> boughtItems = purchase.getBoughtItems();
        int idMaxLen = 7;
        int nameMaxLen = 4;
        int priceMaxLen = 3;
        int quantityMaxLen = 6;
        for (SoldItem item : boughtItems) {
            idMaxLen = Math.max(idMaxLen, numberOfDigits(item.getBarcode()));
            nameMaxLen = Math.max(nameMaxLen, item.getName().length());
            priceMaxLen = Math.max(priceMaxLen, numberOfDigits(item.getPrice()));
            quantityMaxLen = Math.max(quantityMaxLen, numberOfDigits(item.getQuantity()));
        }
        priceMaxLen += 3;
        String fHeader = "%-" + idMaxLen + "s %-" + nameMaxLen + "s %-" + priceMaxLen + "s %-" + quantityMaxLen + "s %s\n";
        String fRow = "%-" + idMaxLen + "d %-" + nameMaxLen + "s %-" + priceMaxLen + ".2f %-" + quantityMaxLen + "d %.2f\n";
        System.out.printf(fHeader, "Barcode", "Name", "Price", "Amount", "Sum");
        for (SoldItem item : boughtItems) {
            System.out.printf(fRow,
                    item.getBarcode(), item.getName(), item.getPrice(),
                    item.getQuantity(), item.getSum()
            );
        }
    }

    private void deleteFromShoppingCart(Long idx) {
        log.debug("Received following index: " + idx);
        System.out.println(cart.deleteFromShoppingCart(idx));
    }

    private void deleteFromWarehouse(Long idx) {
        log.debug("Received following index: " + idx);
        System.out.println(warehouse.deleteFromStock(idx));
    }

    private void printUsage() {
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println("h\t\tShow this help");
        System.out.println("q\t\tExit the CLI");
        System.out.println("w\t\tShow warehouse contents");
        System.out.println("c\t\tShow cart contents");
        System.out.println("a IDX NR \tAdd NR of stock item with index IDX to the cart");
        System.out.println("p\t\tPurchase the shopping cart");
        System.out.println("r\t\tReset the shopping cart");
        System.out.println("t\t\tPrint team info");
        System.out.println("l\t\tShow last ten purchases");
        System.out.println("y\t\tShow all purchases that are up to one year");
        System.out.println("i IDX\tShow details of a purchase with index IDX from the last shown purchases list");
        System.out.println("     \tLast shown purchases exists if commands 'Show last ten purchases' or 'Show all purchases that are up to one year' or 'Show the history of purchases between FIRSTDATE and SECONDDATE' was called");
        System.out.println("e IDX\t\tDelete an item with index IDX from the shopping cart");
        System.out.println("ew IDX\t\tDelete an item with index IDX from the warehouse");
        System.out.println("b IDX NR \tResupply NR of stock item with index IDX to the warehouse");
        System.out.println("n IDX NAME PRICE NR\tAdd an amount (NR) of a new product with index IDX, name (NAME) and price (PRICE) to the warehouse");
        System.out.println("                   \tTo add product with name consisting of more than one word, enclose it in ''");
        System.out.println("d FIRSTDATE SECONDDATE\tShow the history of purchases between FIRSTDATE and SECONDDATE");
        System.out.println("                      \tDates format is yyyy-MM-dd");
        System.out.println("-------------------------");
    }

    private void processCommand(String command) {
        log.info("User output received");
        String[] c = command.split(" ");
        log.debug("Received tokens from user: " + Arrays.toString(c));
        if (c[0].equals("h"))
            printUsage();
        else if (c[0].equals("q"))
            System.exit(0);
        else if (c[0].equals("w")) {
            showStock();
            showSoldOutItems();
        } else if (c[0].equals("d") && c.length == 3) {
            log.info("Showing the history of purchases between FIRSTDATE and SECONDDATE");
            try {
                LocalDate firstDate = LocalDate.parse(c[1]);
                LocalDate secondDate = LocalDate.parse(c[2]);
                showHistoryBetweenDates(firstDate, secondDate);
            } catch (SalesSystemException | NumberFormatException e) {
                log.error(e.getMessage());
            }
        } else if (c[0].equals("i") && c.length == 2) {
            log.info("Showing details of a purchase with index " + c[1] + " from the last shown purchases list");
            try {
                int idx = Integer.parseInt(c[1]);
                showDetails(idx);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
        } else if (c[0].equals("c"))
            showCart();
        else if (c[0].equals("p"))
            cart.submitCurrentPurchase();
        else if (c[0].equals("r"))
            cart.cancelCurrentPurchase();
        else if (c[0].equals("t"))
            showTeam();
        else if (c[0].equals("l")) {
            showLastTenPurchases();
        } else if (c[0].equals("y")) {
            showAllUpToOneYear();
        } else if (c[0].equals("a") && c.length == 3) {
            log.info("Adding NR of stock item with index IDX to the cart");
            try {
                long idx = Long.parseLong(c[1]);
                int amount = Integer.parseInt(c[2]);
                StockItem item = dao.findStockItem(idx);
                if (item != null) {
                    cart.addItem(new SoldItem(item, Math.min(amount, item.getQuantity())));
                } else {
                    System.out.println("no stock item with id " + idx);
                }
            } catch (SalesSystemException | NoSuchElementException e) {
                log.error(e.getMessage());
            }
        } else if (c[0].equals("e") && c.length == 2) {
            log.info("Deleting item with index IDX from the shopping cart");
            try {
                long idx = Long.parseLong(c[1]);
                deleteFromShoppingCart(idx);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
        } else if (c[0].equals("ew") && c.length == 2) {
            log.info("Deleting item with index IDX from the warehouse");
            try {
                long idx = Long.parseLong(c[1]);
                deleteFromWarehouse(idx);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
        } else if (c[0].equals("b") && c.length == 3) {
            addByBarcode(c);
        } else if (c[0].equals("n") && c.length >= 5) {
            if (c.length == 5) {
                resupplyNewItem(c);
            } else {
                String[] tokens = command.split("'");
                log.debug("Received following tokens: " + Arrays.toString(tokens));
                if (tokens.length == 3) {
                    String[] nc = Stream.concat(
                            Stream.concat(
                                    Arrays.stream(tokens[0].split(" ")),
                                    Stream.of(tokens[1])
                            ),
                            Arrays.stream(tokens[2].split(" "))
                                    .filter(Predicate.not(String::isBlank))
                    ).toArray(String[]::new);
                    log.debug("Received following tokens: " + Arrays.toString(nc));
                    resupplyNewItem(nc);
                    //Low-level fallback option
//                if (c[2].charAt(0) == '\'' && c[c.length - 3].endsWith("'")) {
//                    String[] nc = new String[5];
//                    nc[0] = c[0];
//                    nc[1] = c[1];
//                    nc[3] = c[c.length - 2];
//                    nc[4] = c[c.length - 1];
//                    StringBuilder sb = new StringBuilder(c[2]);
//                    sb.deleteCharAt(0);
//                    for (int i = 3; i < c.length - 2; i++) {
//                        sb.append(' ');
//                        sb.append(c[i]);
//                    }
//                    sb.deleteCharAt(sb.length() - 1);
//                    nc[2] = sb.toString();
//                    System.out.println(Arrays.toString(nc));
                    /** Function call (nc)*/
                } else {
                    System.out.println("unknown command");
                }
            }
        } else {
            System.out.println("unknown command");
        }
    }

}

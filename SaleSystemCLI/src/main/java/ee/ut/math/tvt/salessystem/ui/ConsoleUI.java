package ee.ut.math.tvt.salessystem.ui;

import ee.ut.math.tvt.salessystem.SalesSystemException;
import ee.ut.math.tvt.salessystem.dao.InMemorySalesSystemDAO;
import ee.ut.math.tvt.salessystem.dao.SalesSystemDAO;
import ee.ut.math.tvt.salessystem.dataobjects.SoldItem;
import ee.ut.math.tvt.salessystem.dataobjects.StockItem;
import ee.ut.math.tvt.salessystem.logic.History;
import ee.ut.math.tvt.salessystem.logic.ShoppingCart;
import ee.ut.math.tvt.salessystem.logic.Team;
import ee.ut.math.tvt.salessystem.logic.Warehouse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A simple CLI (limited functionality).
 */
public class ConsoleUI {
    private static final Logger log = LogManager.getLogger(ConsoleUI.class);

    private final SalesSystemDAO dao;
    private final ShoppingCart cart;
    private final Warehouse warehouse;
    private final History history;
    private final Team team;

    public ConsoleUI(SalesSystemDAO dao) {
        this.dao = dao;
        this.cart = new ShoppingCart(dao);
        this.warehouse = new Warehouse(dao);
        this.history = new History(dao);
        this.team = new Team();
    }

    public static void main(String[] args) throws Exception {
        SalesSystemDAO dao = new InMemorySalesSystemDAO();
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
            System.out.println(si.getId() + " " + si.getName() + " " + si.getPrice() + "Euro (" + si.getQuantity() + " items)");
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
        System.out.println("Team members   " + String.join(" ", team.getTeamMembers()));
        System.out.println("-------------------------");
    }

    private void addByBarcode(String[] c) {
        try {
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
                        .map(so -> String.format("%s (id: %d, amount: %d)", so.getName(), so.getId(), so.getQuantity()))
                        .toArray(String[]::new));
        if(!message.isBlank()) {
            System.out.println("Sold out items are:");
            System.out.println(message);
        }
    }

    private void resupplyNewItem(String[] info) {
        try {
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

    private void printUsage() {
        System.out.println("-------------------------");
        System.out.println("Usage:");
        System.out.println("h\t\tShow this help");
        System.out.println("w\t\tShow warehouse contents");
        System.out.println("c\t\tShow cart contents");
        System.out.println("a IDX NR \tAdd NR of stock item with index IDX to the cart");
        System.out.println("p\t\tPurchase the shopping cart");
        System.out.println("r\t\tReset the shopping cart");
        System.out.println("t\t\tPrint team info");
        System.out.println("b IDX NR \tResupply NR of stock item with index IDX to the warehouse");
        System.out.println("n IDX NAME PRICE NR\tAdd an amount (NR) of a new product with index IDX, name (NAME) and price (PRICE) to the warehouse");
        System.out.println("                   \tTo add product with name consisting of more than one word, enclose it in ''");
        System.out.println("-------------------------");
    }

    private void processCommand(String command) {
        String[] c = command.split(" ");

        if (c[0].equals("h"))
            printUsage();
        else if (c[0].equals("q"))
            System.exit(0);
        else if (c[0].equals("w")) {
            showStock();
            showSoldOutItems();
        }
        else if (c[0].equals("c"))
            showCart();
        else if (c[0].equals("p"))
            cart.submitCurrentPurchase();
        else if (c[0].equals("r"))
            cart.cancelCurrentPurchase();
        else if (c[0].equals("t"))
            showTeam();
        else if (c[0].equals("a") && c.length == 3) {
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
        } else if (c[0].equals("b") && c.length == 3) {
            addByBarcode(c);
        } else if (c[0].equals("n") && c.length >= 5) {
            if (c.length == 5) {
                resupplyNewItem(c);
            } else {
                String[] tokens = command.split("'");
                if (tokens.length == 3) {
                    String[] nc = Stream.concat(
                            Stream.concat(
                                    Arrays.stream(tokens[0].split(" ")),
                                    Stream.of(tokens[1])
                            ),
                            Arrays.stream(tokens[2].split(" "))
                                    .filter(Predicate.not(String::isBlank))
                    ).toArray(String[]::new);
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

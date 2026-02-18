import java.util.Scanner;

/**
 * @author Jordon Youngblood
 * @version 1.0
 * * Main entry class for the Global Supply Chain System (GSSO).
 * This class handles the simulation of warehouse management, product tracking, 
 * and shipment processing between facilities via a command-line interface.
 */
public class GSSO{
    /**
     * Main execution loop for the supply chain simulation.
     * Processes input to initialize warehouses and products, then executes commands
     * for restocking, decommissioning, reporting, and creating shipments.
     * * @param args Command line arguments (not used).
     * * @call-relationships
     * CALLS: {@link Warehouse#getId()}, {@link Warehouse#restock(Product, int)}, 
     * {@link Warehouse#printSummary()}, {@link Warehouse#hasStock(int, int)}, 
     * {@link Warehouse#canFit(Product, int)}, {@link Warehouse#removeStock(Product, int)},
     * {@link Product#getId()}
     */
    public static void main(String[] args){
    // Scanner for reading standard input
    Scanner input = new Scanner(System.in);
    // Total count of warehouses to be initialized
    int numWarehouses = input.nextInt();
    // Total count of products to be initialized
    int numProducts = input.nextInt();
    // Fixed-size array to store warehouse instances
    Warehouse[] warehouses = new Warehouse[1000];
    // Fixed-size array to store product definitions
    Product[] product = new Product[1000];
/* Loop to initialize warehouse objects from input data */
for (int i = 0; i < numWarehouses; i++) {
            // Temporary storage for warehouse ID
            int wId = input.nextInt();
            // Temporary storage for warehouse location string
            String wLoc = input.next();
            // Temporary storage for warehouse maximum weight capacity
            double wCap = input.nextDouble();
            warehouses[i] = new Warehouse(wId, wLoc, wCap, 0.0);
}
/* Loop to initialize product objects from input data */
for (int i = 0; i < numProducts; i++) {
    // Temporary storage for product ID
    int pId = input.nextInt();
    // Temporary storage for product name
    String pName = input.next();
    // Temporary storage for weight per unit
    double pWeight = input.nextDouble();
    // Temporary storage for value per unit
    double pValue = input.nextDouble();
    product[i] = new Product(pId, pName, pWeight, pValue);
}
// Number of commands to process
int M = input.nextInt();
        /* Main command processing loop */
        for (int i = 0; i < M; i++) {
            // Command keyword (RESTOCK, DECOMMISSION, REPORT, CREATE_SHIPMENT)
            String command = input.next();
            /* Logic for adding stock to a specific warehouse */
            if (command.equals("RESTOCK")) {
                // Target warehouse ID
                int whId = input.nextInt();
                // Target product ID
                int pId = input.nextInt();
                // Quantity to add
                int qty = input.nextInt();
                // Reference to the found warehouse object
                Warehouse foundWh = null;
                for (Warehouse w : warehouses) {
                    if (w != null && w.getId() == whId) { foundWh = w; break; }
                }
                // Reference to the found product object
                Product foundProd = null;
                for (Product p : product) {
                    if (p != null && p.getId() == pId) { foundProd = p; break; }
                }
                if (foundWh != null && foundProd != null && foundWh.restock(foundProd, qty)) {
                    System.out.println("SUCCESS");
                } else {
                    System.out.println("FAILURE");
                }
            /* Logic for removing a warehouse from the system */
            } else if (command.equals("DECOMMISSION")) {
                // ID of warehouse to remove
                int whId = input.nextInt();
                // Flag tracking if deletion was successful
                boolean found = false;
                for (int j = 0; j < warehouses.length; j++) {
                    if (warehouses[j] != null && warehouses[j].getId() == whId) {
                        warehouses[j] = null;
                        found = true;
                        break;
                    }
                }
                System.out.println(found ? "SUCCESS" : "FAILURE");
            /* Logic for printing current status of all active warehouses */
            } else if (command.equals("REPORT")) {
                System.out.println("--- WAREHOUSE REPORT ---");
                for (Warehouse w : warehouses) {
                    if (w != null) w.printSummary();
                }
            /* Logic for moving stock from one warehouse to another */
            } else if (command.equals("CREATE_SHIPMENT")) {
                // UNKNOWN: fill in here - shipment identifier provided but not stored in a persistent shipment log
                int shipId = input.nextInt(); 
                // Source warehouse ID
                int srcId = input.nextInt();
                // Destination warehouse ID
                int dstId = input.nextInt();
                // Product ID to move
                int pId = input.nextInt();
                // Quantity to transfer
                int qty = input.nextInt();
                // Reference to source warehouse
                Warehouse srcWh = null;
                // Reference to destination warehouse
                Warehouse dstWh = null;
                // Reference to product to be shipped
                Product shipProd = null;
                /* Search for required objects in arrays */
                for (Warehouse w : warehouses) {
                    if (w != null) {
                        if (w.getId() == srcId) srcWh = w;
                        if (w.getId() == dstId) dstWh = w;
                    }
                }
                for (Product p : product) {
                    if (p != null && p.getId() == pId) { shipProd = p; break; }
                }
                /* Validate availability and capacity before executing transfer */
                if (srcWh != null && dstWh != null && shipProd != null) {
                    if (srcWh.hasStock(pId, qty) && dstWh.canFit(shipProd, qty)) {
                        srcWh.removeStock(shipProd, qty);
                        dstWh.restock(shipProd, qty);
                        System.out.println("SUCCESS");
                    } else {
                        System.out.println("FAILURE");
                    }
                } else {
                    System.out.println("FAILURE");
                }
            }
        }
    }
}

/**
 * @author Jordon Youngblood
 * @version 1.0
 * * Represents a physical storage facility that tracks its capacity, current load,
 * and the specific quantities of products held in inventory.
 */
class Warehouse{
    // Unique identifier for the warehouse
    private int id;
    // Physical location or name of the facility
    private String location;
    // Maximum weight capacity the warehouse can hold
    private double maxCapacity;
    // Current total weight of all stored products
    private double currentWeight;
    // Array to store product IDs corresponding to productQuantities index
    private int[] productIds = new int[1000];
    // Array to store stock quantities of specific products
    private int[] productQuantities = new int[1000];
    /**
     * Checks if the warehouse contains a sufficient quantity of a specific product.
     * * @param pId The unique ID of the product to check.
     * @param qty The required minimum quantity.
     * @return True if stock is available, false otherwise.
     * * @call-relationships
     * CALLED BY: {@link GSSO#main(String[])}
     */
    public boolean hasStock(int pId, int qty) {
    for (int i = 0; i < productIds.length; i++) {
        if (productIds[i] == pId) {
            return productQuantities[i] >= qty;
        }
    }
    return false;
}
    /**
     * Determines if a quantity of a product can be added without exceeding max capacity.
     * * @param p The product object to evaluate.
     * @param qty The amount of units to test.
     * @return True if the total weight remains within limits, false otherwise.
     * * @call-relationships
     * CALLS: {@link Product#getWeight()}
     * CALLED BY: {@link GSSO#main(String[])}
     */
public boolean canFit(Product p, int qty) {
    return (currentWeight + (qty * p.getWeight())) <= maxCapacity;
}
    /**
     * Decreases stock levels and updates warehouse weight after a shipment removal.
     * * @param p The product to remove.
     * @param qty The quantity to subtract.
     * * @call-relationships
     * CALLS: {@link Product#getId()}, {@link Product#getWeight()}
     * CALLED BY: {@link GSSO#main(String[])}
     */
public void removeStock(Product p, int qty) {
    for (int i = 0; i < productIds.length; i++) {
        if (productIds[i] == p.getId()) {
            productQuantities[i] -= qty;
            currentWeight -= (qty * p.getWeight());
            break;
        }
    }
}
    /**
     * Adds stock to the warehouse and updates total weight. 
     * Handles both existing inventory and adding new products to empty slots.
     * * @param p The product to add.
     * @param qty The quantity to add.
     * @return True if restock was successful, false if capacity exceeded.
     * * @call-relationships
     * CALLS: {@link Product#getWeight()}, {@link Product#getId()}
     * CALLED BY: {@link GSSO#main(String[])}
     */
    public boolean restock(Product p, int qty) {
    // Calculated total weight to be added
    double additionalWeight = qty * p.getWeight();
    if (currentWeight + additionalWeight <= maxCapacity) {
        currentWeight += additionalWeight;
        for (int i = 0; i < productIds.length; i++) {
            /* Case: Product already exists in inventory */
            if (productIds[i] == p.getId()) {
                productQuantities[i] += qty;
                return true;
            /* Case: New product slot found (identified by 0) */
            } else if (productIds[i] == 0) {
                productIds[i] = p.getId();
                productQuantities[i] = qty;
                return true;
            }
        }
    }
    return false;
}
    /**
     * Getter for the warehouse unique ID.
     * * @return The warehouse ID.
     */
    public int getId() { 
        return id; 
    }
    /**
     * Constructs a new Warehouse with specific parameters.
     * * @param id Warehouse identifier.
     * @param location Facility location.
     * @param maxCapacity Total weight limit.
     * @param currentWeight Initial weight load.
     */
    public Warehouse(int id, String location, double maxCapacity, double currentWeight){
        this.id = id;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.currentWeight = currentWeight;
    }
    /**
     * Prints a formatted summary of the warehouse's current state.
     * * @call-relationships
     * CALLED BY: {@link GSSO#main(String[])}
     */
    public void printSummary() {
    System.out.println("ID: " + id + " | Location: " + location + 
                       " | Load: " + currentWeight + "/" + maxCapacity);
}
}
/**
 * @author Jordon Youngblood
 * @version 1.0
 * * Defines a product entity within the supply chain, including physical 
 * attributes like weight and financial attributes like value.
 */
class Product{
    // Unique identifier for the product
    private int id;
    // Human-readable name of the product
    private String name;
    // Weight per individual unit of product
    private double weightPerUnit; 
    // Market value per individual unit of product
    private double valuePerUnit;
    /**
     * Getter for the product unique ID.
     * * @return The product ID.
     */
    public int getId() { 
        return id; 
    }
    /**
     * Getter for the unit weight.
     * * @return The weight per unit.
     */
    public double getWeight() { 
    return weightPerUnit; 
}
    /**
     * Constructs a new Product definition.
     * * @param id Product identifier.
     * @param name Name of product.
     * @param weightPerUnit Unit weight.
     * @param valuePerUnit Unit value.
     */
    public Product(int id, String name, double weightPerUnit, double valuePerUnit){
    this.id = id;
    this.name = name;
    this.weightPerUnit = weightPerUnit;
    this.valuePerUnit =  valuePerUnit;
    }
    /**
     * Getter for the unit price/value.
     * * @return The value per unit.
     */
    public double getPrice(){
        return valuePerUnit;
    }
    /**
     * Getter for the product name.
     * * @return The name string.
     */
    public String getProduct()
    {
        return name; 
    }
}
/**
 * @author Jordon Youngblood
 * @version 1.0
 * * Data class representing a shipment transaction.
 * Note: Currently instantiated as a data holder, but logic for managing 
 * these objects is external to the class definition.
 */
class Shipment{
    // Unique identifier for the shipment
    private int id;
    // ID of the source warehouse
    private int sourceId;
    // ID of the destination warehouse
    private int destId;
    // ID of the product being moved
    private int productId;
    // Amount of product moved
    private int quantity;
    // Completion status flag
    private boolean isCompleted;
    /**
     * Constructs a shipment record.
     * * @param id Shipment identifier.
     * @param sourceId Origin facility ID.
     * @param destId Target facility ID.
     * @param productId Item ID.
     * @param quantity Number of units.
     * @param isCompleted Status of the shipment.
     */
    public Shipment(int id, int sourceId, int destId, int productId, int quantity, boolean isCompleted){
        this.destId = destId;
        this.id = id;
        this.sourceId = sourceId;
        this.productId = productId;
        this.quantity = quantity;
        this.isCompleted = isCompleted;
        }
    /**
     * Getter for the shipment ID.
     * * @return The shipment identifier.
     */
        public int getShipmentId(){
            return id;
        }
}
/*USE Get-Content GSSOInput.txt | java GSSO TO RUN! */
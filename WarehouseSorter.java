import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Visualizer provides a Graphical User Interface (GUI) for the Warehouse system.
 * It manages user inputs, displays bin statuses, and handles the application's lifecycle.
 * * @author Jordon Youngblood
 * @version 1.0
 * @see Warehouse
 */
class Visualizer extends JFrame {
    // The core warehouse logic engine
    private Warehouse myWarehouse = new Warehouse();
    // The main text display for system logs and status
    private JTextArea displayArea = new JTextArea();
    // Input field for user commands
    private JTextField inputField = new JTextField(20);
    // Button to trigger command processing
    private JButton addButton = new JButton("Add Item");

    /**
     * Constructs the Visualizer frame, initializes UI components, and loads data.
     * * @caller WarehouseSorter.main
     * @callee Warehouse.setupDefaultWarehouse, Warehouse.loadFromFile, Visualizer.updateUI
     */
    public Visualizer() {
        // Window Setup
        setTitle("Automated Warehouse System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add components
        add(new JScrollPane(displayArea), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(inputField);
        bottomPanel.add(addButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listener for command processing
        addButton.addActionListener(e -> {
            String input = inputField.getText().trim();
            String[] parts = input.split(" ");
            
            // Logic for processing specific string-based commands
            if (input.startsWith("ADD_ITEM") && parts.length == 5) {
                // ADD_ITEM [name] [weight] [tag] [binId]
                myWarehouse.processAddItem(parts[1], Double.parseDouble(parts[2]), parts[3], Integer.parseInt(parts[4]));
            } 
            else if (input.startsWith("MOVE_ITEM") && parts.length == 3) {
                // MOVE_ITEM [itemName] [targetBinId]
                myWarehouse.moveItem(parts[1], Integer.parseInt(parts[2]));
            } 
            else if (input.startsWith("DECOMMISSION_BIN") && parts.length == 2) {
                // DECOMMISSION_BIN [binId]
                myWarehouse.decommissionBin(Integer.parseInt(parts[1]));
            }
            else if (input.startsWith("REPLACE_BIN") && parts.length == 3) {
                // REPLACE_BIN [newId] [capacity]
                int newId = Integer.parseInt(parts[1]);
                double cap = Double.parseDouble(parts[2]);
                boolean replaced = myWarehouse.addReplacementBin(newId, cap);
                
                if (replaced) {
                    displayArea.append("System: New Bin " + newId + " installed successfully.\n");
                } else {
                    displayArea.append("System Error: No decommissioned slots available.\n");
                }
            }
            else if (input.startsWith("RECOVER_ITEM") && parts.length == 3) {
                // RECOVER_ITEM [itemName] [targetBinId]
                boolean recovered = myWarehouse.recoverItem(parts[1], Integer.parseInt(parts[2]));
                if (!recovered) {
                    displayArea.append("System Error: Could not recover item. Check compatibility/weight.\n");
                }
            }
            inputField.setText("");
            updateUI(); 
        });

        // Serialization logic to restore previous session data
        Warehouse loaded = Warehouse.loadFromFile();
        if (loaded != null) {
            this.myWarehouse = loaded;
        } else {
            myWarehouse.setupDefaultWarehouse();
        }

        // Save automatically when the window closes
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myWarehouse.saveToFile();
            }
        });
        
        updateUI(); // Initial display

        // UI Hint label setup
        JLabel hintLabel = new JLabel("<html><b>Commands:</b><br>" +
            "ADD_ITEM [name] [weight] [tag] [binId]<br>" +
            "MOVE_ITEM [itemName] [targetBinId]<br>" +
            "DECOMMISSION_BIN [binId]<br>"+"REPLACE_BIN [binId][weight]"+"</html>");

        hintLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        hintLabel.setForeground(Color.BLUE);

        add(hintLabel, BorderLayout.NORTH);

        setVisible(true);
    }

    /**
     * Refreshes the JTextArea with the current state of all bins and unsorted items.
     * * @caller Visualizer constructor, ActionListeners
     * @callee Warehouse.getBins, StorageBin.calculateTotalWeight, Warehouse.getUnsortedItems
     */
    public void updateUI() {
        displayArea.setText("--- WAREHOUSE STATUS ---\n");
        StorageBin[] bins = myWarehouse.getBins();
        double totalWeight = 0;
        int totalItems = 0;

        // Iterate through all bins and append their contents to the display
        for (StorageBin bin : bins) {
            if (bin != null) {
                displayArea.append("Bin " + bin.getBinId() + " (Weight: " + bin.calculateTotalWeight() + "/" + bin.getMaxWeight() + "):\n");
                for (Item item : bin.getItems()) {
                    if (item != null) {
                        displayArea.append("  - " + item.getName() + " [" + item.getTag() + "]\n");
                        totalItems++;
                    }
                }
                totalWeight += bin.calculateTotalWeight();
            } else {
                displayArea.append("[Empty Slot - Use REPLACE_BIN]\n");
            }
        }
        
        // Append the list of items currently in the unsorted holding area
        displayArea.append("\n--- UNSORTED HOLDING AREA ---\n");
        boolean hasUnsorted = false;
        for (Item item : myWarehouse.getUnsortedItems()) { 
            if (item != null) {
                displayArea.append(" [!] " + item.getName() + " (" + item.getTag() + ")\n");
                hasUnsorted = true;
                totalItems++; 
            }
        }
        if (!hasUnsorted) displayArea.append(" No unsorted items.\n");
        
        // Final summary statistics
        displayArea.append("\n==============================");
        displayArea.append("\nTotal Warehouse Weight: " + totalWeight);
        displayArea.append("\nTotal Items: " + totalItems);
    }
}

/**
 * Entry point for the Warehouse application.
 * * @author Jordon Youngblood
 * @version 1.0
 */
public class WarehouseSorter {
    /**
     * Main method to launch the Visualizer GUI.
     * * @param args Command line arguments (not used)
     * @callee Visualizer constructor
     */
    public static void main(String[] args){
        new Visualizer();
    }
}

/**
 * Represents a physical item within the warehouse.
 * * @author Jordon Youngblood
 */
class Item implements java.io.Serializable {
    // Unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    // The descriptive name of the item
    private String name;
    // The physical weight of the item
    private double weight;
    // A category tag used for compatibility checks
    private String tag;

    /**
     * Constructs a new Item.
     * * @param name Descriptive name
     * @param weight Physical weight
     * @param tag Compatibility category
     */
    public Item(String name, double weight, String tag){
        this.name = name;
        this.weight = weight;
        this.tag = tag;
    }

    public double getWeight() { return weight; }
    public String getName() { return name; }
    public String getTag() { return tag; }
}

/**
 * Defines whether two different tags are allowed to be stored together.
 * * @author Jordon Youngblood
 */
class CompatibilityRule implements java.io.Serializable {
    // Unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    // The first tag in the pair
    private String tagA;
    // The second tag in the pair
    private String tagB;
    // Flag indicating if the combination is permitted
    private boolean isAllowed;

    /**
     * Constructs a compatibility rule between two tags.
     * * @param tagA First category
     * @param tagB Second category
     * @param isAllowed Permission status
     */
    public CompatibilityRule(String tagA, String tagB, boolean isAllowed) {
        this.tagA = tagA;
        this.tagB = tagB;
        this.isAllowed = isAllowed;
    }

    public String getTagA() { return tagA; }
    public String getTagB() { return tagB; }
    public boolean getIsAllowed() { return isAllowed; }
}

/**
 * Represents a storage container that holds items.
 * * @author Jordon Youngblood
 */
class StorageBin implements java.io.Serializable {
    // Unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    // The unique identifier for this bin
    private int binId;
    // Maximum weight capacity of the bin
    private double maxWeight;
    // Array of items stored in this bin
    Item[] items = new Item[10];

    /**
     * Constructs a StorageBin with a specific ID and weight limit.
     * * @param binId ID of the bin
     * @param maxWeight Weight limit
     */
    public StorageBin(int binId, double maxWeight){
        this.binId = binId;
        this.maxWeight = maxWeight;
    }

    public int getBinId() { return binId; }
    public Item[] getItems() { return items; }

    /**
     * Calculates the current sum of weights of all items in the bin.
     * * @return Total double weight
     */
    public double calculateTotalWeight() {
        double sum = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) { 
                sum += items[i].getWeight();
            }
        }
        return sum;
    }

    /**
     * Attempts to add an item to the bin if weight and space allow.
     * * @param newItem The Item object to add
     * @return true if added, false if bin is full or over weight
     * @callee StorageBin.calculateTotalWeight, StorageBin.findFirstEmptySlot
     */
    public boolean addItem(Item newItem) {
        if (this.calculateTotalWeight() + newItem.getWeight() > this.maxWeight) {
            return false; 
        }
        int slot = this.findFirstEmptySlot();
        if (slot == -1) {
            return false;
        }
        items[slot] = newItem;
        return true;
    }

    /**
     * Finds the first null index in the items array.
     * * @return index of empty slot, or -1 if full
     */
    public int findFirstEmptySlot() {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) { 
                return i;           
            }
        }
        return -1; 
    }   

    /**
     * Removes an item by name and returns it.
     * * @param itemName Name of item to remove
     * @return The Item removed, or null if not found
     */
    public Item removeItem(String itemName) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getName().equals(itemName)) {
                Item temp = items[i]; 
                items[i] = null;
                return temp;
            }
        }
        return null; 
    }

    public double getMaxWeight() { return maxWeight; }
}

/**
 * Manages the collection of bins and enforces global warehouse rules.
 * * @author Jordon Youngblood
 */
class Warehouse implements java.io.Serializable {
    // Unique ID for serialization compatibility
    private static final long serialVersionUID = 1L;
    // Fixed-size array of storage bins
    private StorageBin[] bins = new StorageBin[10];
    // Global rules for item compatibility
    private CompatibilityRule[] rules = new CompatibilityRule[5];
    // Holding area for items without a bin
    private Item[] unsortedItems = new Item[50];

    /**
     * Searches for a bin by its unique ID.
     * * @param id The ID to search for
     * @return StorageBin if found, null otherwise
     */
    public StorageBin findBin(int id) {
        for (int i = 0; i < bins.length; i++) {
            if (bins[i] != null && bins[i].getBinId() == id) {
                return bins[i];
            }
        }
        return null;
    }

    /**
     * Validates and adds a new item to a specific bin.
     * * @return true if successful
     * @callee Warehouse.findBin, Warehouse.isCompatible, StorageBin.addItem
     */
    public boolean processAddItem(String name, double weight, String tag, int binId) {
        StorageBin target = findBin(binId);
        if (target == null) return false; 

        if (!isCompatible(tag, target)) {
            System.out.println("Error: Incompatible tags found in Bin " + binId);
            return false;
        }
        
        Item newItem = new Item(name, weight, tag);
        return target.addItem(newItem);
    }

    /**
     * Checks if a new tag conflicts with any items already inside a specific bin.
     * * @param newTag Tag of the item to be added
     * @param bin Target bin for the item
     * @return true if allowed, false if a restrictive rule is triggered
     */
    public boolean isCompatible(String newTag, StorageBin bin) {
        Item[] existingItems = bin.getItems();
        
        // Loop through items already in the bin
        for (int i = 0; i < existingItems.length; i++) {
            if (existingItems[i] != null) {
                String existingTag = existingItems[i].getTag();
                
                // Check every rule in the warehouse
                for (int j = 0; j < rules.length; j++) {
                    if (rules[j] != null) {
                        boolean match1 = (rules[j].getTagA().equals(newTag) && rules[j].getTagB().equals(existingTag));
                        boolean match2 = (rules[j].getTagA().equals(existingTag) && rules[j].getTagB().equals(newTag));
                        
                        if ((match1 || match2) && !rules[j].getIsAllowed()) {
                            return false; 
                        }
                    }
                }
            }
        }
        return true; 
    }

    /**
     * Transfers an item from one bin to another after validation.
     * * @param itemName Name of item to move
     * @param targetBinId ID of destination bin
     * @return true if successfully moved
     * @callee Warehouse.findBin, Warehouse.isCompatible, StorageBin.removeItem, StorageBin.addItem
     */
    public boolean moveItem(String itemName, int targetBinId) {
        StorageBin sourceBin = null;
        Item itemToMove = null;

        // 1. Find the item and its current location
        for (int i = 0; i < bins.length; i++) {
            if (bins[i] != null) {
                Item[] currentItems = bins[i].getItems();
                for (int j = 0; j < currentItems.length; j++) {
                    if (currentItems[j] != null && currentItems[j].getName().equals(itemName)) {
                        sourceBin = bins[i];
                        itemToMove = currentItems[j];
                        break;
                    }
                }
            }
        }

        StorageBin destBin = findBin(targetBinId);
        if (sourceBin == null || destBin == null) return false;
        if (sourceBin.getBinId() == targetBinId) return false;

        // Validation block
        if (!isCompatible(itemToMove.getTag(), destBin)) return false;
        if (destBin.calculateTotalWeight() + itemToMove.getWeight() > destBin.getMaxWeight()) {
            return false;
        }

        // Execution block
        sourceBin.removeItem(itemName); 
        boolean success = destBin.addItem(itemToMove);
        
        return success;
    }

    public StorageBin[] getBins() { 
        return bins; 
    }

    /**
     * Initializes the warehouse with standard bins and safety rules.
     */
    public void setupDefaultWarehouse() {
        bins[0] = new StorageBin(1, 100.0);
        bins[1] = new StorageBin(2, 50.0);
        rules[0] = new CompatibilityRule("Flammable", "Electronic", false);
    }

    /**
     * Removes a bin from service and moves all contained items to the unsorted area.
     * * @param binId ID of bin to remove
     * @callee Warehouse.moveToUnsorted
     */
    public void decommissionBin(int binId) {
        for (int i = 0; i < bins.length; i++) {
            if (bins[i] != null && bins[i].getBinId() == binId) {
                Item[] itemsToSave = bins[i].getItems();
                for (Item item : itemsToSave) {
                    if (item != null) {
                        moveToUnsorted(item);
                    }
                }
                bins[i] = null; 
                System.out.println("Bin " + binId + " decommissioned. Items moved to Unsorted.");
                return;
            }
        }
    }

    /**
     * Places an item into the first available slot in the unsortedHolding array.
     * * @param item The Item to store
     */
    private void moveToUnsorted(Item item) {
        for (int i = 0; i < unsortedItems.length; i++) {
            if (unsortedItems[i] == null) {
                unsortedItems[i] = item;
                return;
            }
        }
    }

    /**
     * Serializes the current Warehouse object to a file.
     */
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("warehouse_data.ser"))) {
            oos.writeObject(this);
            System.out.println("Warehouse saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deserializes the Warehouse object from a file.
     * * @return Loaded Warehouse or null if file not found
     */
    public static Warehouse loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("warehouse_data.ser"))) {
            return (Warehouse) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No save file found, starting fresh.");
            return null; 
        }
    }

    /**
     * Adds a new bin to a slot where a previous bin was decommissioned.
     * * @param newId New Bin ID
     * @param capacity Max weight capacity
     * @return true if slot found and bin added
     */
    public boolean addReplacementBin(int newId, double capacity) {
        for (int i = 0; i < bins.length; i++) {
            if (bins[i] == null) { 
                bins[i] = new StorageBin(newId, capacity);
                return true;
            }
        }
        return false; 
    }

    /**
     * Attempts to move an item from the unsorted area into a specific bin.
     * * @param itemName Name of the item
     * @param targetBinId ID of the bin to move to
     * @return true if recovered and stored, false otherwise
     * @callee Warehouse.findBin, Warehouse.isCompatible, StorageBin.addItem
     */
    public boolean recoverItem(String itemName, int targetBinId) {
        StorageBin dest = findBin(targetBinId);
        if (dest == null) return false;

        for (int i = 0; i < unsortedItems.length; i++) {
            if (unsortedItems[i] != null && unsortedItems[i].getName().equals(itemName)) {
                if (isCompatible(unsortedItems[i].getTag(), dest) && 
                    (dest.calculateTotalWeight() + unsortedItems[i].getWeight() <= dest.getMaxWeight())) {
                    
                    if (dest.addItem(unsortedItems[i])) {
                        unsortedItems[i] = null; 
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Provides access to the unsorted items array, ensuring it is initialized.
     * * @return Array of Items
     */
    public Item[] getUnsortedItems() {
        if (unsortedItems == null) {
            unsortedItems = new Item[50];
        }
        return unsortedItems;
    }
}
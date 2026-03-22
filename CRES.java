import java.util.Scanner;

/**
 * The Player class represents a user in the game world.
 * Each player has a name, level, a reference to a Guild, and an inventory of Relics.
 * * @author Jordon Youngblood
 * @version 2025-2026
 */
class Player {
    // The unique identifier for the player's character
    private String name;
    // The progression level of the player, influencing carrying capacity
    private int level;
    // Reference to the Guild object the player is currently affiliated with
    private Guild currentGuild;
    // Fixed-size array representing the player's item slots
    Relic[] inventory = new Relic[5];

    /**
     * Constructs a new Player with a name and level.
     * Initial guild state is set to null (Freelancer).
     * * @param name The name of the player.
     * @param level The starting level of the player.
     * CALLER: CRES.main()
     * CALLEE: None
     */
    public Player(String name, int level) {
        this.name = name;
        this.level = level;
        this.currentGuild = null; // Players start as Freelancers
    }

    /**
     * Prints the current state of the player, including guild affiliation 
     * and the detailed contents of their inventory.
     * * CALLER: CRES.main() (STATUS command)
     * CALLEE: Guild.getGName(), Relic.getItemName(), Relic.getWeight(), Relic.getPowerValue()
     */
    public void displayStatus() {
        // Ternary operator used to handle null guilds gracefully
        String guildName = (currentGuild != null) ? currentGuild.getGName() : "Freelancer";
        System.out.println("Player: " + name + " | Level: " + level + " | Guild: " + guildName);
        System.out.print("   Inventory: ");
        // Local flag to determine if the inventory array contains any non-null objects
        boolean empty = true;
        // Iterating through the inventory array to find non-null relics
        for (Relic r : inventory) {
            if (r != null) {
                // Displays all 3 constructor values: Name, Weight, and Power Value
                System.out.print("[" + r.getItemName() + " (W:" + r.getWeight() + ", P:" + r.getPowerValue() + ")] ");
                empty = false;
            }
        }
        if (empty) {
            System.out.print("Empty");
        }  
        System.out.println();
    }

    /**
     * Updates the player's current guild affiliation.
     * * @param g The Guild object to associate with the player.
     * CALLER: CRES.main() (JOIN command), Guild.disband()
     * CALLEE: None
     */
    public void setGuild(Guild g) {
        this.currentGuild = g;
    }

    /**
     * Retrieves the current level of the player.
     * * @return The integer level of the player.
     * CALLER: CRES.main() (JOIN command)
     * CALLEE: None
     */
    public int getLevel() {
        return level;
    }

    /**
     * Updates the player's level with boundary validation.
     * * @param level The new level to set.
     * CALLER: UNKNOWN: fill in here
     * CALLEE: None
     */
    public void setLevel(int level) {
        // Validation logic to ensure game balance
        if (level >= 0 && level <= 100) {
            this.level = level;
        } else {
            System.out.println("Levels out of bounds and implausible!");
        }
    }

    /**
     * Retrieves the player's name.
     * * @return The string name of the player.
     * CALLER: CRES.findPlayer(), Guild.addMemberToGuild(), Guild.removeMemberFromGuild(), CRES.main()
     * CALLEE: None
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the player's current Guild reference.
     * * @return The Guild object or null if Freelancer.
     * CALLER: CRES.main() (JOIN, LEAVE commands)
     * CALLEE: None
     */
    public Guild getGuild() {
        return currentGuild;
    }

    /**
     * Adds a relic to the first available null slot in the inventory array.
     * Also establishes the back-reference from the relic to the player.
     * * @param newRelic The Relic object to be added.
     * CALLER: CRES.main() (PICKUP command)
     * CALLEE: None (Sets Relic.owner)
     */
    public void addRelicToPlayer(Relic newRelic) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = newRelic;
                newRelic.owner = this; // Relic now knows its owner
                return;
            }
        }
    }

    /**
     * Cascading Weight Rule: If total weight > (Level * 10), drop the heaviest item.
     * Uses a while(true) loop to ensure the player is under the limit after multiple drops.
     * * CALLER: CRES.main() (After PICKUP)
     * CALLEE: Relic.getWeight(), Relic.getItemName()
     */
    public void weightCalculation() {
        while (true) {
            // Local accumulator for the weight of all items in inventory
            int totalWeight = 0;
            // Tracking variable for the highest weight value found in current iteration
            int maxWeight = -1;
            // Index of the heaviest relic to be removed if capacity is exceeded
            int heaviestIndex = -1;
            // 1. Calculate sum AND find the heaviest at the same time
            for (int i = 0; i < inventory.length; i++) {
                if (inventory[i] != null) {
                    totalWeight += inventory[i].getWeight();
                    if (inventory[i].getWeight() > maxWeight) {
                        maxWeight = inventory[i].getWeight();
                        heaviestIndex = i;
                    }
                }
            }
            // Check if capacity is exceeded
            if (totalWeight > (level * 10) && heaviestIndex != -1) {
                System.out.println("Relic dropped! " + inventory[heaviestIndex].getItemName() + " exceeds total weight!");
                inventory[heaviestIndex].owner = null; // Unclaim the relic
                inventory[heaviestIndex] = null;       // Delete from inventory (tombstone)
                // Loop continues to re-verify total weight
            } else {
                break; // Stop loop if under limit or inventory is empty
            }
        }
    }

    /**
     * Manually removes a specific relic by name from the player's inventory.
     * * @param relicName The string name of the relic to drop.
     * CALLER: CRES.main() (DROP command)
     * CALLEE: Relic.getItemName()
     */
    public void dropRelic(String relicName) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getItemName().equals(relicName)) {
                System.out.println(name + " dropped " + inventory[i].getItemName());
                inventory[i].owner = null; // Relic is now unclaimed
                inventory[i] = null;       // Slot is now empty
                return;
            }
        }
        System.out.println("Relic not found in inventory.");
    }

    /**
     * Handles cascading cleanup when a player is removed from the world.
     * Leaves the guild and unclaims all held relics.
     * * CALLER: CRES.main() (DELETE command)
     * CALLEE: Guild.removeMemberFromGuild(), Relic.getItemName()
     */
    public void prepareForDeletion() {
        // 1. Leave Guild
        if (currentGuild != null) {
            currentGuild.removeMemberFromGuild(this);
            currentGuild = null;
        }
        // 2. Release all relics back to the world
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                System.out.println("Relic " + inventory[i].getItemName() + " is now unclaimed.");
                inventory[i].owner = null;
                inventory[i] = null;
            }
        }
    }
}

/**
 * The Guild class manages an array of Player references and enforces 
 * membership and level requirements.
 * * @author Jordon Youngblood
 * @version 2025-2026
 */
class Guild {
    // The internal ranking/difficulty level of the guild
    private int guildLevel;
    // The display name of the guild
    private String guildName;
    // Fixed-capacity array of Player references currently in the guild
    private Player[] members = new Player[10]; // Fixed capacity of 10 members

    /**
     * Constructs a Guild with a name and level.
     * * @param guildName Name of the guild.
     * @param guildLevel Level of the guild.
     * CALLER: CRES.main()
     * CALLEE: None
     */
    public Guild(String guildName, int guildLevel) {
        this.guildName = guildName;
        this.guildLevel = guildLevel;
    }

    /**
     * Adds a player to the guild list if a slot is available.
     * * @param p The Player object joining the guild.
     * CALLER: CRES.main() (JOIN command)
     * CALLEE: Player.getName()
     */
    public void addMemberToGuild(Player p) {
        for (int i = 0; i < members.length; i++) {
            if (members[i] == null) {
                members[i] = p;
                System.out.println(p.getName() + " has joined " + this.guildName);
                return;
            }
        }
        System.out.println("Guild is full!");
    }

    /**
     * Removes a player from the guild array (tombstone deletion).
     * * @param p The Player object leaving the guild.
     * CALLER: Player.prepareForDeletion(), CRES.main() (LEAVE command)
     * CALLEE: Player.getName()
     */
    public void removeMemberFromGuild(Player p) {
        for (int i = 0; i < members.length; i++) {
            if (members[i] != null && members[i] == p) {
                members[i] = null; // Vacates the slot
                System.out.println(p.getName() + " has left " + this.guildName);
                return;
            }
        }
    }

    /**
     * Retrieves the guild's level.
     * * @return The integer level of the guild.
     * CALLER: CRES.main() (JOIN command)
     * CALLEE: None
     */
    public int getGLevel() {
        return guildLevel;
    }

    /**
     * Retrieves the guild's name.
     * * @return The string name of the guild.
     * CALLER: Player.displayStatus(), CRES.findGuild(), CRES.main()
     * CALLEE: None
     */
    public String getGName() {
        return guildName;
    }

    /**
     * Removes members if the guild level drops below a certain threshold.
     * * CALLER: UNKNOWN: fill in here
     * CALLEE: None
     */
    public void cleanupMembers() {
        if (this.guildLevel < 2) {
            for (int i = 0; i < members.length; i++) {
                if (this.members[i] != null) {
                    this.members[i] = null;
                    System.out.println("Removed a member because guild level is too low.");
                }
            }
        }
    }

    /**
     * Breaks the guild apart, making all current members freelancers.
     * * CALLER: CRES.main() (DISBAND command)
     * CALLEE: Player.setGuild()
     */
    public void disband() {
        for (int i = 0; i < members.length; i++) {
            if (members[i] != null) {
                members[i].setGuild(null); // Player becomes Freelancer
                members[i] = null;         // Remove reference in guild list
            }
        }
        System.out.println(this.guildName + " has been disbanded.");
    }
}

/**
 * The Relic class represents a physical item in the world.
 * It maintains a bidirectional relationship with its current owner.
 * * @author Jordon Youngblood
 * @version 2025-2026
 */
class Relic {
    // The name of the relic
    private String itemName;
    // The weight value used for capacity calculations
    private int weight;
    // Reference to the Player object currently holding the relic
    Player owner; // Bidirectional relationship with Player
    // The numerical power rating of the relic
    private int powerValue;

    /**
     * Constructs a Relic with specific stats.
     * * @param name The name of the relic.
     * @param weight The weight of the relic.
     * @param powerValue The power rating of the relic.
     * CALLER: CRES.main()
     * CALLEE: None
     */
    public Relic(String name, int weight, int powerValue) {
        this.itemName = name;
        this.weight = weight;
        this.owner = null; // Unclaimed by default
        this.powerValue = powerValue;
    }

    /**
     * Retrieves the name of the relic.
     * * @return The string name.
     * CALLER: Player.displayStatus(), Player.weightCalculation(), Player.dropRelic(), 
     * Player.prepareForDeletion(), CRES.findRelic(), CRES.main()
     * CALLEE: None
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Retrieves the weight of the relic.
     * * @return The integer weight.
     * CALLER: Player.displayStatus(), Player.weightCalculation(), CRES.main()
     * CALLEE: None
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Retrieves the power value of the relic.
     * * @return The integer power value.
     * CALLER: Player.displayStatus(), CRES.main()
     * CALLEE: None
     */
    public int getPowerValue() {
        return powerValue;
    }
}

/**
 * Main System Class: Chrono-Relic Expedition System (CRES)
 * Orchestrates the creation and interaction of world entities via a command-line loop.
 * * @author Jordon Youngblood
 * @version 2025-2026
 */
public class CRES {
    /**
     * Search method for finding a Player object by name in the world array.
     * * @param allPlayers The array of players to search through.
     * @param nameToFind The name to search for.
     * @return The Player object or null if not found.
     * CALLER: CRES.main()
     * CALLEE: Player.getName()
     */
    public static Player findPlayer(Player[] allPlayers, String nameToFind) {
        for (int i = 0; i < allPlayers.length; i++) {
            if (allPlayers[i] != null && allPlayers[i].getName().equals(nameToFind)) {
                return allPlayers[i];
            }
        }
        return null;
    }

    /**
     * Search method for finding a Relic object by name in the world array.
     * * @param allRelics The array of relics to search through.
     * @param relicNameToFind The name to search for.
     * @return The Relic object or null if not found.
     * CALLER: CRES.main()
     * CALLEE: Relic.getItemName()
     */
    public static Relic findRelic(Relic[] allRelics, String relicNameToFind) {
        for (int i = 0; i < allRelics.length; i++) {
            if (allRelics[i] != null && allRelics[i].getItemName().equals(relicNameToFind)) {
                return allRelics[i];
            }
        }
        return null;
    }

    /**
     * Search method for finding a Guild object by name in the world array.
     * * @param allGuilds The array of guilds to search through.
     * @param guildNameToFind The name to search for.
     * @return The Guild object or null if not found.
     * CALLER: CRES.main()
     * CALLEE: Guild.getGName()
     */
    public static Guild findGuild(Guild[] allGuilds, String guildNameToFind) {
        for (int i = 0; i < allGuilds.length; i++) {
            if (allGuilds[i] != null && allGuilds[i].getGName().equals(guildNameToFind)) {
                return allGuilds[i];
            }
        }
        return null;
    }

    /**
     * The main entry point for the CRES system.
     * Initializes world state and processes user commands autonomously.
     * * @param args Command line arguments.
     * CALLER: JVM
     * CALLEE: Player constructor, Guild constructor, Relic constructor, search methods, 
     * and various class methods based on command input.
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        // Global arrays to hold world state
        Player[] worldPlayers = new Player[100];
        Guild[] worldGuilds = new Guild[20];
        Relic[] worldRelics = new Relic[200];

        // --- Step 1: Initialize Players ---
        System.out.println("Enter number of players: ");
        int numPlayers = input.nextInt();
        for (int i = 0; i < numPlayers; i++) {
            System.out.print("Enter name for player: " + (i + 1) + ": ");
            String name = input.next();
            System.out.print("Enter level for player " + (i + 1) + ": ");
            int level = input.nextInt();
            worldPlayers[i] = new Player(name, level);
        }

        // --- Step 2: Initialize Guilds ---
        System.out.println("Enter number of guilds in the world: ");
        int numGuilds = input.nextInt();
        for (int i = 0; i < numGuilds; i++) {
            System.out.print("Enter the guild name: ");
            String guildName = input.next();
            // Local variable used for automated level assignment logic
            int autoLevel = i + 1; 
            System.out.println("Guild level: " + autoLevel);
            worldGuilds[i] = new Guild(guildName, autoLevel);
        }

        // --- Step 3: Initialize Relics (Automated Stats) ---
        System.out.println("How many relics are in the world?:");
        int numRelics = input.nextInt();
        for (int i = 0; i < numRelics; i++) {
            System.out.print("Enter the relic's name: ");
            String relicName = input.next();
            // System determines stats automatically based on creation order to ensure uniqueness
            int autoWeight = (i + 1) * 5;
            int autoPower = i + 2;
            worldRelics[i] = new Relic(relicName, autoWeight, autoPower);
            // Print verification of all 3 constructor values
            System.out.println("Relic Created: " + worldRelics[i].getItemName() +
                    " | Weight: " + worldRelics[i].getWeight() +
                    " | Power: " + worldRelics[i].getPowerValue());
        }

        // --- Step 4: Command Loop ---
        /*
         * Block: Command Processing
         * Continuously listens for user input to modify the state of the world entities.
         */
        while (true) {
            System.out.println("\nAvailable Commands: PICKUP, DROP, JOIN, STATUS, LEAVE, DISBAND, DELETE, EXIT");
            System.out.print("Enter command: ");
            String command = input.next();
            if (command.equalsIgnoreCase("EXIT")) {
                System.out.println("Exiting System...");
                break;
            }

            // Command to assign a relic to a player
            if (command.equalsIgnoreCase("PICKUP")) {
                System.out.print("Which player is picking up? ");
                String pName = input.next();
                System.out.print("Which relic are they taking? ");
                String rName = input.next();
                Player p = findPlayer(worldPlayers, pName);
                Relic r = findRelic(worldRelics, rName);
                if (p != null && r != null) {
                    if (r.owner == null) {
                        p.addRelicToPlayer(r);
                        p.weightCalculation(); // Triggers capacity check
                    } else {
                        System.out.println("Error: " + r.getItemName() + " is already owned by " + r.owner.getName());
                    }
                }
            }

            // Command for players to join guilds based on level requirements
            if (command.equalsIgnoreCase("JOIN")) {
                System.out.print("Player Name: ");
                String pName = input.next();
                System.out.print("Guild Name: ");
                String gName = input.next();
                Player p = findPlayer(worldPlayers, pName);
                Guild g = findGuild(worldGuilds, gName);
                if (p != null && g != null) {
                    // Sophisticated level-based requirement
                    if (p.getLevel() < (g.getGLevel() * 5)) {
                        System.out.println("Player level too low for this guild!");
                    } else if (p.getGuild() == null) {
                        p.setGuild(g);
                        g.addMemberToGuild(p);
                    } else {
                        System.out.println(p.getName() + " is already in a guild!");
                    }
                }
            }

            // Global status report
            if (command.equalsIgnoreCase("STATUS")) {
                System.out.println("--- World Status ---");
                for (Player p : worldPlayers) {
                    if (p != null) {
                        p.displayStatus();
                    }
                }
            }

            // Removes a player from a guild
            if (command.equalsIgnoreCase("LEAVE")) {
                System.out.print("Player Name: ");
                String pName = input.next();
                Player p = findPlayer(worldPlayers, pName);
                if (p != null && p.getGuild() != null) {
                    Guild current = p.getGuild();
                    current.removeMemberFromGuild(p);
                    p.setGuild(null);
                } else {
                    System.out.println("Player is either not found or not in a guild.");
                }
            }

            // Manually drop a relic
            if (command.equalsIgnoreCase("DROP")) {
                System.out.print("Player Name: ");
                String pName = input.next();
                System.out.print("Relic Name: ");
                String rName = input.next();
                Player p = findPlayer(worldPlayers, pName);
                if (p != null) {
                    p.dropRelic(rName);
                }
            }

            // Deletes a player and handles cascading state changes
            if (command.equalsIgnoreCase("DELETE")) {
                System.out.print("Enter name of player to delete: ");
                String pName = input.next();
                for (int i = 0; i < worldPlayers.length; i++) {
                    if (worldPlayers[i] != null && worldPlayers[i].getName().equals(pName)) {
                        worldPlayers[i].prepareForDeletion();
                        worldPlayers[i] = null; // Tombstone deletion
                        System.out.println("Player " + pName + " removed from world.");
                        break;
                    }
                }
            }

            // Disbands an entire guild
            if (command.equalsIgnoreCase("DISBAND")) {
                System.out.print("Enter name of guild to disband: ");
                String gName = input.next();
                for (int i = 0; i < worldGuilds.length; i++) {
                    if (worldGuilds[i] != null && worldGuilds[i].getGName().equals(gName)) {
                        worldGuilds[i].disband();
                        worldGuilds[i] = null; // Deletes guild from world array
                        break;
                    }
                }
            }
        } // End of while loop
    } // End of main
} // End of class CRES
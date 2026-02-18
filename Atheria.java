import java.util.Scanner;
public class Atheria{
    public static void main(String[] args){
        Scanner input = new Scanner(System.in);
        Player[] players = new Player[100];
        Raid myRaid = new Raid();
while(input.hasNext()){
    String name = input.next();
    String role = input.next();
    int level = input.nextInt();
    String guildName = input.next();
        Player myPlayer = new Player(name, role, guildName, level);
        for(int i=0; i < players.length; i++){
            if(players[i]== null){
                players[i] = myPlayer;
                break;
                                        }
                                    }
                       
                                }
        for(int i=0; i < players.length; i++){
        if (players[i] != null && players[i].getGuild() != null && players[i].getGuild().getGLevel() < 5){
                    players[i] = null;
                            }
                        }
        for(int i=0; i < players.length; i++){
            if(players[i] != null){
                
                    }
                }
        for(int i=0; i < players.length; i++){
                        if(players[i] != null){
                            myRaid.addPlayerToRaid(players[i]);
            }
        }
    }       
}
class Player{
    private String name;
    private String role;
    private int level;
    private Guild currentGuild;
    private String guildName;
    public Player(String name, String role, String guildName, int level){
        this.name = name;
        this.role = role;
        this.level = level;
        this.guildName = guildName;
        this.currentGuild = new Guild((int)(Math.random() * 10) + 1);
                }
    public int getLevel(){
        return level;
            }
    public void setLevel(int level){
        if(level >= 0 && level <= 100){
            this.level = level;
        }else{
            System.out.println("Levels out of bounds and implausible!");
    }
}
    public String getName(){
        return name;
            }
    public Guild getGuild(){
        return currentGuild;
        }
        public String getRole(){
        return role;
    }
}
class Guild{
    private int level;
    public int getGLevel() {
        return level;
    }
    public Guild(int level) 
    { 
        this.level = level; 
    }
    public void cleanupMembers(Player[] allPlayers) {
        if (this.level < 5) {
            for (int i = 0; i < allPlayers.length; i++) {
                if (allPlayers[i] != null && allPlayers[i].getGuild() == this) {
                    allPlayers[i] = null;
                    System.out.println("Removed a member because guild level is too low.");
                }
            }
        }
    }
}
class RaidSlot{
    private String requiredRole;
    private Player occupant;
    public RaidSlot(String role){
        this.requiredRole = role;
    }
    public void fillSlot(Player p){
        p.getRole();
        p.getLevel();
        p.getName();

        if(p.getRole().equals(this.requiredRole) && this.occupant == null){
            this.occupant = p;
            System.out.println(p.getName()+" joined the Raid as " + p.getRole());
        }
    }
    public Player getOccupant() 
    {
        return this.occupant; 
    }
}
class Raid {
    private RaidSlot[] slots = new RaidSlot[3];
    public Raid() {
        slots[0] = new RaidSlot("Tank");
        slots[1] = new RaidSlot("Healer");
        slots[2] = new RaidSlot("DPS");
    }
    public void addPlayerToRaid(Player p){
        for(int i = 0; i < slots.length; i++){
            slots[i].fillSlot(p);
            if(slots[i].getOccupant() == p){
                break;
            }
        }
    }
}

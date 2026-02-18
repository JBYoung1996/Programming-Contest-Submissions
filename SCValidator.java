import java.util.Scanner;
public class SCValidator{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String user = sc.next().trim();

        if(user.length() == 4){
            if (user.charAt(0) == user.charAt(3)){
                System.out.println("Secure");
            }else{
                System.out.println("Insecure");
            }
        }else{
            System.out.println("Invalid");
        }
    }
}
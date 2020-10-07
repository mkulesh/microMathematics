import java.util.Scanner;

public class Conversion {

    public static void main(String args[]) {
        Scanner ip = new Scanner(System.in);
        System.out.print("Enter an integer in decimal form (base 10): ");
        int num = ip.nextInt();
        int i,n = num,j=0;
        String binary="",str,octal="",hexa="";
        
        // for binary - base 2
        while(n>0){
            n = (int) (num/(Math.pow(2, j++)));
            i = n%2;
            str = Integer.toString(i);
            binary = str.concat(binary);
        }
        System.out.println("The binary representation of " + num + " is " + binary);
        
        // for Octal - base 8
        j=0;
        n=num;
        while(n>0){
            n = (int) (num/(Math.pow(8, j++)));
            i = n%8;
            str = Integer.toString(i);
            octal = str.concat(octal);
        }
        System.out.println("The Octal representation of " + num + " is " + octal);
        
        // for hexadecimal - base 16
        j=0;
        n=num;
        char ch;
        while(n>0){
            n = (int) (num/(Math.pow(16, j++)));
            i = n%16;
            if(i>9){
                i = i + 55;
                ch =(char)i;
                str = Character.toString(ch);
            }
            else{
                str = Integer.toString(i);
            }
            hexa = str.concat(hexa);
        }
        System.out.println("The Hexadecimal representation of " + num + " is " + hexa);
        
        // for base 12
        j=0;
        n=num;
        char ch1;
        String base12 = "";
        while(n>0){
            n = (int) (num/(Math.pow(12, j++)));
            i = n%12;
            if(i>9){
                i = i + 55;
                ch1 =(char)i;
                str = Character.toString(ch1);
            }
            else{
                str = Integer.toString(i);
            }
            base12 = str.concat(base12);
        }
        System.out.println("The Base 12 representation of " + num + " is " + base12);
        
        // for base 60
        j=0;
        n=num;
        String base60 = "";
        while(n>0){
            n = (int) (num/(Math.pow(60, j++)));
            i = n%60;
            if(i>9 && i<36){
                i = i + 55;
                ch =(char)i;
                str = Character.toString(ch);
            }
            else if(i>35) {
            	i = i + 61;
            	ch = (char)i;
            	str = Character.toString(ch);
            }
            else{
                str = Integer.toString(i);
            }
            base60 = str.concat(base60);
        }
        System.out.println("The Base 60 representation of " + num + " is " + base60);
        
        ip.close();
    }
}

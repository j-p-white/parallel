import java.util.Random; 
public class AlloyObj{

//random number must be between zero and one 
Random rand = new Random();

//percentageOf Metal
double m1P = .30;
double m2P = .35; 
double m3P = .35;

//makeing the metal 
metalObj m1 = new metalObj("Jamium",0.75);
metalObj m2 = new metalObj("CSium",1.0); 
metalObj m3 = new metalObj("Computerium",1.25);

//seting the alloy/region startTemp 
long temp = 0;
long plate = 0;

public AlloyObj(){
}
 }//end class

import java.util.Random; 
public class AlloyObj{

//random number must be between zero and one 
Random rand = new Random();

//percentageOf Metal
double m1P = 0;
double m2P = 0; 
double m3P = 0;

//makeing the metal 
metalObj m1 = new metalObj("Jamium",0.75);
metalObj m2 = new metalObj("CSium",1.0); 
metalObj m3 = new metalObj("Computerium",1.25);

//seting the alloy/region startTemp 
long temp = 0;

public AlloyObj(){
    makeAlloyPercentage(m1P,m2P);
}
  
  public void makeAlloyPercentage(double m1P, double m2P ){
           m1P = rand.nextDouble()+1;
           m2P = rand.nextDouble()+0; 

           m3P = m1P - m2P;
            //if m3P is negative 
           if(m3P < -0){
               m3P = m3P * -1;
           }
  }//end makeAlloyPercentage

//make getter for percent of metals 

 public double percentOfM1(){
    return m1P;
 }// end percent of M1

 public double percentOfM2(){
    return m2P;
 }// end percent of M2

 public double percentOfM3(){
    return m3P;
 }// end percent of M3

 public metalObj getM1(){
       return m1;
 }//end getM1 

 public metalObj getM2(){
       return m2;
 }//end getM2

 public metalObj getM3(){
       return m3;
 }//end getM3 

public void setRegionTemp(long temp){
    this.temp = temp;
}// end long

public long getRegionTemp(){
    return temp;
 }//end getRegionTemp

}//end class

public class metalObj{
 String metalName; //name
 double metalTC; //thermal constant
 
 public metalObj(String name, double metalConstant){
    
    metalName = name; 

    metalTC = metalConstant;
    
 }
    //empty constructor
 public metalObj(){
 }

 public double getMetalTC(){
        return metalTC;
 }

}//end class

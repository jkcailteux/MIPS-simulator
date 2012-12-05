import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Pipeline {

  ArrayList<Instruction> Instructions = new ArrayList<Instruction>();
  Map<String,Register> Registers = new HashMap<String,Register>();
  
  public Pipeline(){
  }
  public Pipeline(ArrayList<Instruction> Instructions1, Map<String, Integer> Registers1){
    Instructions=Instructions1;
    String temp="";
    Iterator<String> Registerkeys=Registers1.keySet().iterator();
    //Populate list of Registers
    while(Registerkeys.hasNext()){
      Register R= new Register();
      temp=Registerkeys.next();
      R.value=Registers1.get(temp);
      Registers.put(temp, R);
    }
    
    
  }

  public void IF1(Instruction i) {
    String temp=i.raw_text;
    int x=0;
    while(temp.charAt(x)!=' '){
      x++;
    }
    i.opcode=temp.substring(0, x);
    i.stage="IF2";
  }

  public void IF2(Instruction i) {
    
   i.stage="ID"; 
  }
  
  public void ID(Instruction i) {
    
    
    
   }
  public void EXE(Instruction i) {
    
    if(i.opcode.equals("DADD")){
      if(i.source1!=null && i.source2!=null){
        Registers.get(i.dest).value=Registers.get(i.source1).value+Registers.get(i.source2).value;
      } else if(i.source1!=null && i.imm!=null){
        Registers.get(i.dest).value=Registers.get(i.source1).value + i.imm;
      }
      
    }else if (i.opcode.equals("SUB")){
      
    }else if (i.opcode.equals("LD")){
      
    }else if (i.opcode.equals("SD")){
      
    }else if (i.opcode.equals("BNEZ")){
      
    }
    
    
  }

}

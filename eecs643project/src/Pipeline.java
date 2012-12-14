import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class Pipeline {

  public int cycle = 1;
  ArrayList<Instruction> Instructions = new ArrayList<Instruction>();
  Map<String, Integer> Registers = new HashMap<String, Integer>();
  Map<String, Integer> Memory = new HashMap<String, Integer>();
  int PC = 0;
  int pipeline_number = 1;// number of instructions in pipeline
  int finished = 0;
  String branchdest = "";
  boolean next_cycle_branch = false;
  boolean stalling = false;

  // Control Signals

  public Pipeline() {
  }

  public void initialize(Map<String, Integer> Registers1, Map<String, Integer> Memory1) {
    String temp = "";
    Memory = Memory1;

    Iterator<String> Registerkeys = Registers1.keySet().iterator();
    // Populate list of Registers
    while (Registerkeys.hasNext()) {
      temp = Registerkeys.next();
      Registers.put(temp, Registers1.get(temp));
    }
  }

  public void giveInst(Instruction i) {
    i.pipeline_number = pipeline_number;
    pipeline_number++;
    Instructions.add(i);
  }

  public void IF1(Instruction i) {
    String temp = i.raw_text;
    int x = 0;
    while (temp.charAt(x) != ' ') {
      x++;
    }
    i.opcode = temp.substring(0, x);
    PC = PC + 4;
    i.stage = "IF2";
  }

  public void IF2(Instruction i) {
    i.stage = "ID";
  }

  public void ID(Instruction i) {

    // DADD SUB
    if (i.opcode.equals("DADD") || i.opcode.equals("SUB")) {
      decode_parsingR(i);
      // LD and SD
    } else if (i.opcode.equals("LD") || i.opcode.equals("SD")) {
      decode_parsingRI(i);
      // BRANCH
    } else if (i.opcode.equals("BNEZ")) {
      decode_parsingBr(i);
    }
    i.stage = "EX";

  }

  public void EXE(Instruction i) {

    Instruction i2 = null;
    // check for stalling 1 up
    int z = getIndex(i);
    if (z > 0) {
      i2 = Instructions.get(z - 1);
      if (i2 != null && i2.opcode.equals("LD")
          && (i2.stage.equals("MEM1") || i2.stage.equals("MEM2") || i2.stage.equals("MEM3"))) {
        // check source1
        if (i.source1.equals(i2.dest)) {
          stalling = true;
          return;
        }
        // check source2
        if (i.re2 && i.source2.equals(i2.dest)) {
          stalling = true;
          return;
        }
      }
    }
    // check for stalling 2 up
    if (z > 1) {
      i2 = Instructions.get(z - 2);
      if (i2 != null && i2.opcode.equals("LD")
          && (i2.stage.equals("MEM1") || i2.stage.equals("MEM2") || i2.stage.equals("MEM3"))) {
        // check source1
        if (i.source1.equals(i2.dest)) {
          stalling = true;
          return;
        }
        // check source2
        if (i.re2 && i.source2.equals(i2.dest)) {
          stalling = true;
          return;
        }
      }
    }

    // forwarding
    if (z > 0) {
      i2 = Instructions.get(z - 1);
      if (i2.Regwrite && (i2.dest.equals(i.source1))) {
        if (i2.opcode.equals("LD")) {
          Registers.put(i.source1, i2.LMD);//forward load result
        } else {
          Registers.put(i.source1, i2.ALUoutput);//forward aluoutput
        }
      } else if (i2.Regwrite && i2.re2 && (i2.dest.equals(i.source2))) {
        if (i2.opcode.equals("LD")) {
          Registers.put(i.source2, i2.LMD);//forward load result
        } else {
          Registers.put(i.source2, i2.ALUoutput);//forward aluoutput
        }
      }
    }
    if (z > 1) {
      i2 = Instructions.get(z - 2);
      if (i2.Regwrite && (i2.dest.equals(i.source1))) {
        Registers.put(i.source1, i2.ALUoutput);
      } else if (i2.Regwrite && i2.re2 && (i2.dest.equals(i.source2))) {
        Registers.put(i.source2, i2.ALUoutput);
      }
    }

    // DADD
    if (i.opcode.equals("DADD")) {
      if (!i.source1.equals("") && !i.source2.equals("")) {
        i.ALUoutput = Registers.get(i.source1) + Registers.get(i.source2);
      } else if (!i.source1.equals("") && i.imm != null) {
        i.ALUoutput = Registers.get(i.source1) + i.imm;
      }
      // SUB
    } else if (i.opcode.equals("SUB")) {
      if (!i.source1.equals("") && !i.source2.equals("")) {
        i.ALUoutput = Registers.get(i.source1) - Registers.get(i.source2);
      } else if (!i.source1.equals("") && i.imm != null) {
        i.ALUoutput = Registers.get(i.source1) - i.imm;
      }
      // LD SD
    } else if (i.opcode.equals("LD") || i.opcode.equals("SD")) {
      i.ALUoutput = Registers.get(i.source1) + i.offset;
      // BRANCH
    } else if (i.opcode.equals("BNEZ")) {
      if (Registers.get(i.source1) != 0) {
        i.branch_taken = true;
        next_cycle_branch = true;// for pipeline to know to stop executing IF2,
                                 // ID
        branchdest = i.branch_word;
      }
    }

    i.stage = "MEM1";

  }

  public void MEM1(Instruction i) {
    i.stage = "MEM2";
  }

  public void MEM2(Instruction i) {

    if (i.opcode.equals("LD")) {
      i.LMD = Memory.get(Integer.toString(i.ALUoutput));
    } else if (i.opcode.equals("SD")) {
      Memory.put(Integer.toString(i.ALUoutput), Registers.get(i.dest));
    }

    i.stage = "MEM3";
  }

  public void MEM3(Instruction i) {
    i.stage = "WB";
  }

  public void WB(Instruction i) {

    if (i.opcode.equals("DADD") || i.opcode.equals("SUB")) {
      Registers.put(i.dest, i.ALUoutput);
      i.stage = "done";
    } else if (i.opcode.equals("LD")) {
      Registers.put(i.dest, i.LMD);
      i.stage = "done";
    } else if (i.opcode.equals("SD")) {
      i.stage = "done";
      // BRANCH
    } else if (i.opcode.equals("BNEZ")) {
      i.stage = "done";
    }
    finished++;
  }

  public void print_instr() {
    for (Instruction i : Instructions) {
      System.out.println(i.raw_text);
    }
  }

  public void print_Registers(PrintStream out) {

    String temp = "";
    out.println("REGISTERS");
    for (int z = 1; z != 32; z++) {
      temp = "R" + z;
      if (Registers.get(temp) != null) {
        out.println(temp + " " + Registers.get(temp));
      }
    }
  }

  public void print_Memory(PrintStream out) {
    String temp = "";
    out.println("MEMORY");
    for (int z = 0; z != 993; z++) {
      temp = z + "";
      if (Memory.get(temp) != null) {
        out.println(temp + " " + Memory.get(temp));
      }
    }
  }

  public void decode_parsingR(Instruction i) {
    // handles immediate and register addressing modes
    i.RegDst = true;
    i.Regwrite = true;

    if (i.opcode.equals("DADD")) {
      i.we_bypass = true;
      // pass opcode
      String temp = i.raw_text;
      int x = 0, y = 0;
      while (temp.charAt(x) != ' ') {
        x++;
      }
      x++;
      y = x;

      // rd
      while (temp.charAt(y) != ' ') {
        y++;
      }
      i.dest = temp.substring(x, (y - 1));
      i.ws = i.dest;
      // rs
      y++;
      x = y;
      while (temp.charAt(x) != ' ') {
        x++;
      }
      i.source1 = temp.substring(y, (x - 1));

      // either rt or imm
      x++;
      y = x;
      if (temp.charAt(x) == '#') {
        // imm
        while (x < temp.length() && temp.charAt(x) != ' ') {
          x++;
        }
        i.imm = Integer.parseInt(temp.substring(y + 1, x));

      } else if (temp.charAt(x) == 'R') {
        // rt
        while (x < temp.length() && temp.charAt(x) != ' ') {
          x++;
        }
        i.source2 = temp.substring(y, x);
        i.re2 = true;
      }
    } else if (i.opcode.equals("SUB")) {
      i.we_bypass = true;
      // pass opcode
      String temp = i.raw_text;
      int x = 0, y = 0;
      while (temp.charAt(x) != ' ') {
        x++;
      }
      x++;
      y = x;

      // rd
      while (temp.charAt(y) != ' ') {
        y++;
      }
      i.dest = temp.substring(x, (y - 1));
      i.ws = i.dest;
      // rs
      y++;
      x = y;
      while (temp.charAt(x) != ' ') {
        x++;
      }
      i.source1 = temp.substring(y, (x - 1));

      // either rt or imm
      x++;
      y = x;
      if (temp.charAt(x) == '#') {
        // imm
        while (temp.charAt(x) != ' ') {
          x++;
        }
        i.imm = Integer.parseInt(temp.substring(y + 1, x));

      } else if (temp.charAt(x) == 'R') {
        // rt
        while (x < temp.length() && temp.charAt(x) != ' ') {
          x++;
        }
        i.source2 = temp.substring(y, x);
        i.re2 = true;
      }
    }
  }

  public void decode_parsingRI(Instruction i) {
    // handles register indirect addressing mode

    // pass opcode
    String temp = i.raw_text;
    int x = 0, y = 0;
    while (temp.charAt(x) != ' ') {
      x++;
    }
    x++;
    y = x;

    // rd(is technically rt)
    while (temp.charAt(y) != ' ') {
      y++;
    }
    i.dest = temp.substring(x, (y - 1));
    if (i.opcode.equals("LD")) {
      i.Regwrite = true;
      i.ALUsrc = true;
      i.Memread = true;
      i.MemtoReg = true;
      i.ws = i.dest;
      i.we_stall = true;
    } else if (i.opcode.equals("SD")) {
      i.ALUsrc = true;
      i.MemWrite = true;
    }

    // offset
    y++;
    x = y;
    while (temp.charAt(x) != '(') {
      x++;
    }
    i.offset = Integer.parseInt(temp.substring(y, x));
    // rs
    y = x;
    x++;
    while (x < temp.length() && temp.charAt(x) != ')') {
      x++;
    }
    i.source1 = temp.substring(y + 1, x);

  }

  public void decode_parsingBr(Instruction i) {
    // handles branches
    i.Branch = true;
    // pass opcode
    String temp = i.raw_text;
    int x = 0, y = 0;
    while (temp.charAt(x) != ' ') {
      x++;
    }
    x++;
    y = x;

    // rs
    while (temp.charAt(x) != ' ') {
      x++;
    }
    i.source1 = temp.substring(y, (x - 1));

    x++;
    y = x;
    // branch word
    while (x < temp.length() && temp.charAt(x) != ' ') {
      x++;
    }
    i.branch_word = temp.substring(y, x);
  }

  public Instruction getInstructionAtStage(String stage) {
    for (Instruction i : Instructions) {
      if (i.stage.equals(stage))
        return i;
    }
    return null;
  }

  public int getIndex(Instruction i) {
    int x = 0;

    for (Instruction i2 : Instructions) {
      if (i2 == i) {
        return x;
      }
      x++;
    }

    return x;
  }

}

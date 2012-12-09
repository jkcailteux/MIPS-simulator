import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {

  static int instr_count = 0;
  static Map<String, Integer> Branches = new HashMap<String, Integer>();// holds
                                                                        // PC
                                                                        // values
                                                                        // for
                                                                        // branch
                                                                        // words
  static Pipeline pipeline = new Pipeline();
  static boolean stall_check = false;
  static PrintStream out = null;

  static boolean stop = false;

  public static void main(String args[]) {

    // setup variables
    ArrayList<Instruction> Instructions = new ArrayList<Instruction>();// holds
                                                                       // list
                                                                       // of
                                                                       // instructions,
                                                                       // untouched
    Map<String, Integer> Registers = new HashMap<String, Integer>();// temp hold
                                                                    // reg
                                                                    // values
    Map<String, Integer> Memory = new HashMap<String, Integer>();// temp holds
                                                                 // memory

    Registers.put("R0", 0);// R0 is always 0
    String output = "";

    // Read input files, fill registers, memory,and instructions
    try {
      Readfiles("input.txt", Registers, Memory, Instructions);
    } catch (IOException ioe) {
    }
    try {
      PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
    } catch (FileNotFoundException e) {
    }
    // enable when done
    // System.setOut(out);

    pipeline.initialize(Registers, Memory);

    // Begin Pipeline here
    // IF1, IF2, ID, EX, MEM1, MEM2, MEM3, WB
    // IF1-PC selection, initiate instruction cache access
    // IF2-complete instruction cache access
    // ID-decode, register fetch, hazard checking, instruction cache hit
    // detection
    // EX-address calculation, ALU operation, branch-target computation,
    // condition evaluation
    // MEM1-data fetch, first half of data cache access
    // MEM2-completion of data cache acccess
    // MEM3-Tag check, determine whether the data cache access hit
    // WB- Write-back for loads and register-register ops

    // give 1 instruction
    for (Instruction ins : Instructions) {
      if (ins.inst_num == pipeline.PC) {
        ins.stage = "IF1";
        Instruction i2 = new Instruction();
        i2.raw_text = ins.raw_text;
        i2.branch_word = ins.branch_word;
        i2.inst_num = ins.inst_num;
        i2.stage = ins.stage;// clone i
        pipeline.giveInst(i2);
      }

      while (pipeline.finished!=pipeline.Instructions.size()) {
        System.out.print("c#" + pipeline.cycle + " ");

        // go through all instructions given to pipeline
        // they are processed WB to IF1
        for (int x = 0; x < pipeline.Instructions.size(); x++) {
          Instruction itemp = pipeline.Instructions.get(x);

          if (itemp.stage.equals("WB")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-WB ");
              pipeline.WB(itemp);
            }
            // MEM3
          } else if (itemp.stage.equals("MEM3")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-MEM3 ");
              pipeline.MEM3(itemp);
            }
            // MEM2
          } else if (itemp.stage.equals("MEM2")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-MEM2 ");
              pipeline.MEM2(itemp);
            }
            // MEM1
          } else if (itemp.stage.equals("MEM1")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              // PC may change here, branch may be true
              if (itemp.branch_taken == true) {
                System.out.println(pipeline.PC);
                System.out.println(itemp.branch_word);
                System.out.println(Branches.get(itemp.branch_word));
                pipeline.PC = Branches.get(itemp.branch_word);
              }

              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-MEM1 ");
              pipeline.MEM1(itemp);
            }
            // EXE
          } else if (itemp.stage.equals("EX")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              pipeline.EXE(itemp);
              if(pipeline.stalling) {
                System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
              } else 
                System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-EX ");
            }
            // ID
          } else if (itemp.stage.equals("ID")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-ID ");
              pipeline.ID(itemp);
            }
            // IF2
          } else if (itemp.stage.equals("IF2")) {
            if (pipeline.stalling) {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-stall ");
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-IF2 ");
              pipeline.IF2(itemp);
            }
            // IF1
          } else if (itemp.stage.equals("IF1")) {
            if (pipeline.stalling) {
            } else {
              System.out.print("I" + ((int) (itemp.inst_num / 4) + 1) + "-IF1 ");
              pipeline.IF1(itemp);
            }
          }
        }

        // get new instruction based on PC if not stalling
        if (!pipeline.stalling) {
          for (Instruction i : Instructions) {
            if (i.inst_num == pipeline.PC) {
              i.stage = "IF1";
              Instruction i2 = new Instruction();
              i2.raw_text = i.raw_text;
              i2.branch_word = i.branch_word;
              i2.inst_num = i.inst_num;
              i2.stage = i.stage;// clone i
              pipeline.giveInst(i2);
            }
          }
        }

        // end of cycle
        System.out.println("");
        pipeline.stalling=false;
        pipeline.cycle++;
      }
    }

  }

  public static void Readfiles(String filename, Map<String, Integer> Registers,
      Map<String, Integer> Memory, ArrayList<Instruction> Instructions) throws IOException {

    FileInputStream fstream = null;
    String s = null;
    try {
      File file = new File(filename);
      fstream = new FileInputStream(file);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      s = br.readLine();
      while (s != null) {
        s.trim();

        // Fill Lists
        if (s.equals("REGISTERS")) {
          s = br.readLine();
          while (!s.equals("MEMORY") && !s.equals("CODE")) {
            s.trim();
            addRegister(s, Registers);
            s = br.readLine();
          }
        }
        if (s.equals("MEMORY")) {
          s = br.readLine();
          while (!s.equals("REGISTERS") && !s.equals("CODE")) {
            s.trim();
            addMemory(s, Memory);
            s = br.readLine();
          }
        }
        // assumes code is last piece
        if (s.equals("CODE")) {
          s = br.readLine();
          while (!s.equals("REGISTERS") && !s.equals("MEMORY")) {
            s.trim();
            addInstruction(s, Instructions);
            s = br.readLine();
            if (s == null) {
              break;
            }
          }
        }

      }

      // Done reading input
      br.close();
      return;

    } catch (FileNotFoundException ex) {
      Logger.getLogger("log").log(Level.SEVERE, "File not found", ex);
      System.out.println("File not found");
    }
  }

  static void addRegister(String s, Map<String, Integer> Registers) {
    int x = 0, value;
    String name;
    while (s.charAt(x) != ' ') {
      x++;
    }
    name = s.substring(0, x);
    value = Integer.parseInt(s.substring(x + 1));
    Registers.put(name, value);
    return;
  }

  static void addMemory(String s, Map<String, Integer> Memory) {
    int x = 0, value;
    String name;
    while (s.charAt(x) != ' ') {
      x++;
    }
    name = s.substring(0, x);
    value = Integer.parseInt(s.substring(x + 1));
    Memory.put(name, value);
    return;
  }

  static void addInstruction(String s, ArrayList<Instruction> Instructions) {
    Instruction instruc = new Instruction();
    int x = 0, y = 0;

    // Find branch word
    if (s.charAt(0) != ' ') {
      while (s.charAt(y) != ' ') {
        y++;
      }

      Branches.put(s.substring(0, (y - 1)), (instr_count * 4));
      x = y;
    }

    // Cut off whitespace
    while (s.charAt(x) == ' ') {
      x++;
    }
    y = x;
    instruc.raw_text = s.substring(x);
    instruc.inst_num = instr_count * 4;
    instr_count++;

    Instructions.add(instruc);
  }

}
import java.io.*;
import java.util.*;
import java.util.logging.*;

public class Main {

	public static void main(String args[]) {
		// setup variables
		ArrayList<Instruction> Instructions = new ArrayList<Instruction>();
		Map<String, Integer> Registers = new HashMap<String, Integer>();
		Map<String, Integer> Memory = new HashMap<String, Integer>();
		Registers.put("R0", 0);// R0 is always 0

		// Read input files, fill registers, memory,and instructions
		try {
			Readfiles("input.txt", Registers, Memory, Instructions);
		} catch (IOException ioe) {
		}

		
		//Begin Pipeline here
		//IF1, IF2, ID, EX, MEM1, MEM2, MEM3, WB
		//IF1-PC selection, initiate instruction cache access
		//IF2-complete instruction cache access
		//ID-decode, register fetch, hazard checking, instruction cache hit detection
		//EX-address calculation, ALU operation, branch-target computation, condition evaluation
		//MEM1-data fetch, first half of data cache access
		//MEM2-completion of data cache acccess
		//MEM3-Tag check, determine whether the data cache access hit
		//WB- Write-back for loads and register-register ops
		
		
		
		
		// print contents of registers, memory,
		printRegisters(Registers);
		printMemory(Memory);

	}

	public static void Readfiles(String filename, Map<String, Integer> Registers, Map<String, Integer> Memory, ArrayList<Instruction> Instructions) throws IOException {

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
			instruc.branch_word = s.substring(0, y);
			x = y;
		}

		
		// Cut off whitespace
		while (s.charAt(x) == ' ') {
			x++;
		}
		y = x;
		instruc.raw_text=s.substring(x);
		
		Instructions.add(instruc);
		/*
		while (s.charAt(y) != ' ') {
			y++;
		}
		instruc.opcode = s.substring(x, y);

		// how to set remaining variables based on instr
		if (s.substring(x, y).equals("BNEZ")) {
			System.out.println("BNEZ");
		} else if (s.substring(x, y).equals("LD")) {
			System.out.println("LD");
		} else if (s.substring(x, y).equals("DADD")) {
			System.out.println("DADD");
		} else if (s.substring(x, y).equals("SD")) {
			System.out.println("SD");
		} else if (s.substring(x, y).equals("SUB")) {
			System.out.println("SUB");
		}
		*/

	}

	static void printRegisters(Map<String, Integer> Registers) {
		String temp = "";
		Iterator<String> it = Registers.keySet().iterator();
		System.out.println("REGISTERS");
		while (it.hasNext()) {
			temp = (String) it.next();
			System.out.print(temp + " ");
			System.out.println(Registers.get(temp));
		}

	}

	static void printMemory(Map<String, Integer> Memory) {
		System.out.println("MEMORY");
		String temp = "";
		Iterator<String> it = Memory.keySet().iterator();
		while (it.hasNext()) {
			temp = (String) it.next();
			System.out.print(temp + " ");
			System.out.println(Memory.get(temp));
		}
	}
}

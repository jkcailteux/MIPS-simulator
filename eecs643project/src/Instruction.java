
public class Instruction {
	public String opcode="";
	public String source1="", source2="";
	public String dest="";
	public int inst_num;
	public int pipeline_number;
	public int offset;
	public Integer imm;
	public String branch_word="";//for branch instructions only
	public boolean branch_taken=false;
	public String raw_text;
	public String stage="";
	public boolean stall=false;
	public int LMD=0,ALUoutput = 0;
	public boolean re1=true, re2=false, we_bypass=false, we_stall=false;
	String ws="",rs="";
	public boolean stalling_instr=false;
	boolean PCsrc = false, Regwrite = false, Branch = false, MemtoReg = false, Memread = false,
      RegDst = false, ALUsrc = false, MemWrite = false;
}

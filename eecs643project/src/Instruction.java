
public class Instruction {
	public String opcode;
	public String source1, source2;
	public String dest;
	public int inst_num;
	public Integer offset;
	public Integer imm;
	public String branch_word;
	public String raw_text;
	public String stage;
	public boolean stall=false;
}

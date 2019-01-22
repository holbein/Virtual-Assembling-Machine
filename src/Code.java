
public class Code {
	public void check(String input) {
		switch(input.charAt(0)){
		case 'A'://A
			switch(input.charAt(1)){
			case 'D'://AD
				switch(input.charAt(2)){
				case 'D'://ADD
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
		case 'D'://D
			switch(input.charAt(1)){
			case 'L'://DL
				switch(input.charAt(2)){
				case 'O'://DLO
					switch(input.charAt(3)){
					case 'A'://DLOA
						switch(input.charAt(4)){
						case 'D'://DLOAD
							execute(input.substring(0, 5), input.substring(5));
						default:
							def(input);
						}
					default:
						def(input);
					}
				default:
					def(input);
				}
			case 'I'://DI
				switch(input.charAt(2)){
				case 'V'://DIV
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
		case 'E'://E
			switch(input.charAt(1)){
			case 'N'://EN
				switch(input.charAt(2)){
				case 'D'://END
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'J'://J
			switch(input.charAt(1)){
			case 'E'://JE
				switch(input.charAt(2)){
				case 'Q'://JEQ
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'G'://JG
				switch(input.charAt(2)){
				case 'E'://JGE
					execute(input.substring(0, 3), input.substring(3));
				case 'T'://JGT
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'L'://JL
				switch(input.charAt(2)){
				case 'E'://JLE
					execute(input.substring(0, 3), input.substring(3));
				case 'T'://JLT
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'N'://JN
				switch(input.charAt(2)){
				case 'E'://JNE
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
			switch(input.charAt(1)){
			case 'U'://JU
				switch(input.charAt(2)){
				case 'M'://JUM
					switch(input.charAt(3)){
					case 'P'://JUMP
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'L'://L
			switch(input.charAt(1)){
			case 'O'://LO
				switch(input.charAt(2)){
				case 'A'://LOA
					switch(input.charAt(3)){
					case 'D'://LOAD
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'M'://M
			switch(input.charAt(1)){
			case 'U'://MU
				switch(input.charAt(2)){
				case 'L'://MUL
					switch(input.charAt(3)){
					case 'T'://MULT
						execute(input.substring(0, 4), input.substring(4));
					default:
						def(input);
					}
				default:
					def(input);
				}
			default:
				def(input);
			}
			
		case 'S'://S
			switch(input.charAt(1)){
			case 'T'://ST
				switch(input.charAt(2)){
				case 'O'://STO
					switch(input.charAt(3)){
					case 'R'://STOR
						switch(input.charAt(4)){
						case 'E'://STORE
							execute(input.substring(0, 5), input.substring(5));
						default:
							def(input);
						}
					default:
						def(input);
					}
				default:
					def(input);
				}
			case 'U'://SU
				switch(input.charAt(2)){
				case 'B'://SUB
					execute(input.substring(0, 3), input.substring(3));
				default:
					def(input);
				}
			default:
				def(input);
			}
		
		default:
			def(input);
		}
	}
	
	private void def(String input) {
		System.out.println("unknown command: \"" + input +"\"");
	}
	
	private void execute(String command, String rest) {
		if(rest.charAt(0) == ' ' ) {
			switch(command) {
			case "ADD":
				add(Integer.valueOf(rest.substring(1)));
			case "DLOAD":
				dload(Integer.valueOf(rest.substring(1)));
			case "DIV":
				div(Integer.valueOf(rest.substring(1)));
			case "JER":
				jer(Integer.valueOf(rest.substring(1)));
			case "JGE":
				jge(Integer.valueOf(rest.substring(1)));
			case "JGT":
				jgt(Integer.valueOf(rest.substring(1)));
			case "JLE":
				jle(Integer.valueOf(rest.substring(1)));
			case "JLT":
				jlt(Integer.valueOf(rest.substring(1)));
			case "JNE":
				jne(Integer.valueOf(rest.substring(1)));
			case "JUMP":
				jump(Integer.valueOf(rest.substring(1)));
			case "LOAD":
				load(Integer.valueOf(rest.substring(1)));
			case "MULT":
				mult(Integer.valueOf(rest.substring(1)));
			case "STORE":
				store(Integer.valueOf(rest.substring(1)));
			case "SUB":
				sub(Integer.valueOf(rest.substring(1)));
			}
			
		}else {
			if(command.equals("END")) {
				end();
			}else {
				def(command+rest);
			}
		}
	}
	
	private void end() {
		//TODO end()
	}
	
	private void add(int number) {
		//TODO add()
	}
	
	private void dload(int number) {
		//TODO dload()
	}
	
	private void div(int number) {
		//TODO div()
	}
	
	private void jer(int number) {
		//TODO jer()
	}
	
	private void jge(int number) {
		//TODO jge()
	}
	
	private void jgt(int number) {
		//TODO jgt()
	}
	
	private void jle(int number) {
		//TODO jle()
	}
	
	private void jlt(int number) {
		//TODO jlt()
	}
	
	private void jne(int number) {
		//TODO jne()
	}
	
	private void jump(int number) {
		//TODO jump()
	}
	
	private void load(int number) {
		//TODO load()
	}
	
	private void mult(int number) {
		//TODO mult()
	}
	
	private void store(int number) {
		//TODO store()
	}
	
	private void sub(int number) {
		//TODO sub()
	}
	
	
}

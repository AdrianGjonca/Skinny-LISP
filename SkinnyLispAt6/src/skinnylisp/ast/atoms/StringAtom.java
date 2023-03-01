package skinnylisp.ast.atoms;

import skinnylisp.ast.exceptions.CodeInvalidEx;

public class StringAtom extends Atom{

	public String value;
	
	public StringAtom(String value, int a) {
		this.value = value;
	}
	
	public StringAtom(String value) throws CodeInvalidEx {
		this.value = toUsable(value.substring(1, value.length()-1));
	}
	
	private String toUsable(String input_str) throws CodeInvalidEx {
		for(int i = 0; i<input_str.length(); i++) {
			if(input_str.charAt(i) == '\\') {
				if(input_str.charAt(i+1) == 'n') {
					return toUsable(input_str.substring(0,i) + '\n' + input_str.substring(i+2));
				}else if(input_str.charAt(i+1) == '\\') {
					return toUsable(input_str.substring(0,i)) + '\\' + toUsable(input_str.substring(i+2));
				}else if(input_str.charAt(i+1) == '\'') {
					return toUsable(input_str.substring(0,i)) + '"' + toUsable(input_str.substring(i+2));
				}else throw new CodeInvalidEx();
			}
					
		}
		
		return input_str;
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + "{" +value.replace("\n", "@") + "}";
	}
}

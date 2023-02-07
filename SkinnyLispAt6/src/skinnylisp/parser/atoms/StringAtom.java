package skinnylisp.parser.atoms;

import skinnylisp.lexer.atoms.Atom;

public class StringAtom extends Atom{

	public String value;
	
	public StringAtom(String value, int a) {
		this.value = value;
	}
	
	public StringAtom(String value) {
		this.value = value.substring(1, value.length()-1).replace("\\n", "\n");
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

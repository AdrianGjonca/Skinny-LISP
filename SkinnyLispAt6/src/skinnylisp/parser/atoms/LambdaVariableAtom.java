package skinnylisp.parser.atoms;

import skinnylisp.lexer.atoms.Atom;

public class LambdaVariableAtom extends Atom {

	public String name;
	
	public LambdaVariableAtom(String name) {
		this.name = name.substring(1).replace(" ", "");
	}
	public LambdaVariableAtom(String name, int empty) {
		this.name = name;
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + "\\" + name + "\\";
	}

}

package skinnylisp.ast.atoms;

public class VariableAtom extends Atom {
	public String name;
	
	public VariableAtom(String name) {
		this.name = name.substring(1).replace(" ", "");
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + "|" + name + "|";
	}
}

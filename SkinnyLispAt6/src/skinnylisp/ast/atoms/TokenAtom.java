package skinnylisp.ast.atoms;

public class TokenAtom extends Atom {

	public String text = "";
	
	public TokenAtom(String text) {
		this.text = text;
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + text;
	}

}

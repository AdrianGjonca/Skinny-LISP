package skinnylisp.parser.atoms;

import skinnylisp.lexer.atoms.Atom;

public class KeywordAtom extends Atom {

	public String keyword;
	
	public KeywordAtom(String keyword) {
		this.keyword = keyword.replace("\n", "").replace(" ", "");
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + "~" + keyword + "~";
	}

}

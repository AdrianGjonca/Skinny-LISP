package skinnylisp.engine.lists;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import skinnylisp.lexer.atoms.Atom;

public class DataListAtom extends Atom{
	public List<Atom> list;
	
	public DataListAtom(ListType list_type) {
		if(list_type == ListType.ArrayList) {
			list = new ArrayList<Atom>();
		}else {
			list = new LinkedList<Atom>();
		}
	}
	
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		return tabStr + "" + list.toString();
	}

}

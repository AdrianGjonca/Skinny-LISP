package skinnylisp.engine.runtimeatoms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import skinnylisp.ast.atoms.Atom;

public class ListAtom extends Atom{
	public List<Atom> list;
	
	public enum ListType {
		ArrayList,
		LinkedList
	}
	
	public ListAtom(ListType list_type) {
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

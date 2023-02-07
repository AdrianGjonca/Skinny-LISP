package skinnylisp.lexer.atoms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import skinnylisp.OutC;
import skinnylisp.exceptions.CodeInvalidEx;
import skinnylisp.lexer.SlicerUpper;


public class ListAtom extends Atom {
	public List<Atom> nodes;

	public ListAtom() {
		nodes = new LinkedList<Atom>();
	}
	public ListAtom(String code) throws CodeInvalidEx {
		try {
			code = code.substring(code.indexOf("(")+1, code.lastIndexOf(")")).replace("\n", " ");
		}catch(Exception e) {
			throw new CodeInvalidEx();
		}
		
		//Console.debug(code);
		List<String> elements = SlicerUpper.slice(code);
		
		nodes = new LinkedList<Atom>();
		
		for(String single : elements) {
			if(single.startsWith("(")) {
				nodes.add(new ListAtom(single));
			}else {
				nodes.add(new TokenAtom(single));
			}
		}
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		String output = "";
		output += tabStr + "[\n";
		for(Atom atom : nodes) {
			output += atom.toString(tab+1)+"\n";
		}
		output += tabStr + "]\n";
		return output.replace("\n\n", "\n");
	}
}

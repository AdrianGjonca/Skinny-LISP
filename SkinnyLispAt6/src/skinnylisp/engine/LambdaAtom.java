package skinnylisp.engine;

import java.util.LinkedList;
import java.util.List;

import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.parser.atoms.LambdaVariableAtom;

public class LambdaAtom extends Atom {
	
	public List<String> params;
	public Atom process;
	public LambdaAtom(ListAtom vars, Atom process) {
		params = new LinkedList<String>();
		for(Atom n : vars.nodes) {
			if(n instanceof LambdaVariableAtom) {
				LambdaVariableAtom a = (LambdaVariableAtom) n;
				params.add(a.name);
			}
		}
		this.process = process;
	}
	
	@Override
	public String toString(int tab) {
		return "____";
	}

}

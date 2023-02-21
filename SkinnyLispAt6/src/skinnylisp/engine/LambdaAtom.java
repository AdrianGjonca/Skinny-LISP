package skinnylisp.engine;

import java.util.LinkedList;
import java.util.List;

import skinnylisp.engine.lists.ListType;
import skinnylisp.exceptions.LispRuntimeError;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.parser.atoms.KeywordAtom;
import skinnylisp.parser.atoms.LambdaVariableAtom;

public class LambdaAtom extends Atom {
		
	public List<String>   params;
	public List<LispType> types;
	public Atom process;
	public LambdaAtom(ListAtom vars, Atom process) throws LispRuntimeError {
		params = new LinkedList<String>();
		types = new LinkedList<LispType>();
		LispType typeon = LispType.Any;
		for(Atom n : vars.nodes) {
			if(n instanceof KeywordAtom) {
				String name = ((KeywordAtom) n).keyword;
				typeon = LispType.Any;
				boolean success = false;
				for(LispType r : LispType.values()) {
					if(name.equals(r.name)) {
						typeon = r;
						success = true;
						break;
					}
				}
				if(!success) throw Interpreter.error_arg(name + " is not a valid type");
			} else if(n instanceof LambdaVariableAtom) {
				LambdaVariableAtom a = (LambdaVariableAtom) n;
				params.add(a.name);
				types.add(typeon);
				typeon = LispType.Any;
			} else throw Interpreter.error_arg("(lambda !([argN | Type]) ?(~expression~))");
		}
		this.process = process;
	}
	
	@Override
	public String toString(int tab) {
		return params.toString() + "\n" + types.toString() + "\n" + process;
	}

}

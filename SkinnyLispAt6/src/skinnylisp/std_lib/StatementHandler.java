package skinnylisp.std_lib;

import java.util.HashMap;

import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.engine.Interpreter;
import skinnylisp.engine.WrapOf_RuntimeError;

public interface StatementHandler {
	public Atom handleKeyword(StatementAtom statement, 
							  HashMap<String, Atom> lambda_vars,
							  Interpreter me,
							  String head_name) throws WrapOf_RuntimeError;
}

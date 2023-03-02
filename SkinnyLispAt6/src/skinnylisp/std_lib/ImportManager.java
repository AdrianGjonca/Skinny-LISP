package skinnylisp.std_lib;

import java.util.HashMap;
import java.util.List;

import data.Pair;
import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.engine.Interpreter;
import skinnylisp.engine.WrapOf_RuntimeError;

public class ImportManager {
	private HashMap<String, StatementHandler> commands;
	
	public ImportManager(boolean use_std) {
		commands = new HashMap<String, StatementHandler>();
		if(use_std) install(STD_LIB);
	}
	
	public void install(Packet packet) {
		List<Pair<String, StatementHandler>> content = packet.getCommands();
		for(Pair<String, StatementHandler> entry : content) {
			commands.put(entry.A, entry.B);
		}
	}
	
	public Atom handleKeyword(StatementAtom statement, 
							  HashMap<String, Atom> lambda_vars,
	                          Interpreter me,
	                          String head_name
	)throws WrapOf_RuntimeError{
		StatementHandler func = commands.get(head_name);
		if(func == null) {
			System.out.println("Issue");
			return null;
		}else {
			return func.handleKeyword(statement, lambda_vars, me, head_name);
		}
	}
	public final static Packet STD_LIB = new Std_lib(); 
}

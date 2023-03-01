package skinnylisp.engine;

import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.NumberAtom;
import skinnylisp.ast.atoms.StringAtom;

public enum LispType {
	Any        ("Any"    , Atom.class),
	
	Number     ("Number" , NumberAtom.class),
	Integer    ("Integer", NumberAtom.class),
	Float      ("Float"  , NumberAtom.class),
	String     ("String" , StringAtom.class),
	
	;
	
	public final String name;
	public final Class<?> atom_class;
	LispType(String name, Class<?> atom_class){
		this.name = name;
		this.atom_class = atom_class;
	}
}

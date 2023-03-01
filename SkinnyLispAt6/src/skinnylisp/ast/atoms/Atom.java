package skinnylisp.ast.atoms;

public abstract class Atom {
	public abstract String toString(int tab);
	
	@Override
	public String toString() {
		return toString(0);
	}
}

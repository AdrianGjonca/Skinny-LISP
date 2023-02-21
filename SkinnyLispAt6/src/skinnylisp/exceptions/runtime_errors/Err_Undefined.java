package skinnylisp.exceptions.runtime_errors;

public class Err_Undefined extends LispError {
	
	public static enum AtomType {
		Keyword,
		Variable,
		LambdaVariable,
	}
	
	public AtomType atom_type;
	public String label;
	public Err_Undefined(String label, AtomType atom_type) {
		this.atom_type = atom_type;
		this.label = label;
	}
	
	@Override
	public String getErrorMessage() {
		return ((atom_type == AtomType.Keyword)        ? ""   : 
			    (atom_type == AtomType.Variable)       ? "$"  : 
			    (atom_type == AtomType.LambdaVariable) ? "\\" : "###" 
			   ) + label
				 + " is undefined";
	}

}

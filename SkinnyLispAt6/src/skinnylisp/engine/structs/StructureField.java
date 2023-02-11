package skinnylisp.engine.structs;

import skinnylisp.exceptions.StructureFieldInvalidEx;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.parser.atoms.KeywordAtom;
import skinnylisp.parser.atoms.VariableAtom;

public class StructureField {
	public String field_name;
	public Atom field_value;
	public FieldType field_type;
	
	public StructureField(ListAtom element) throws StructureFieldInvalidEx{
		if(element.nodes.size() == 2) {
			field_value = element.nodes.get(1);
			
			Atom token = element.nodes.get(0);
			if(token instanceof KeywordAtom) {
				field_name = ((KeywordAtom) token).keyword;
				field_type = FieldType.Immutable;
			}else if(token instanceof VariableAtom) {
				field_name = ((VariableAtom) token).name;
				field_type = FieldType.Mutable;
			}else {
				throw new StructureFieldInvalidEx();
			}
		}else {
			throw new StructureFieldInvalidEx();
		}
	}
}

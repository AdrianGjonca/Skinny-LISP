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
				field_type = FieldType.Immutable_Global;
			}else if(token instanceof VariableAtom) {
				field_name = ((VariableAtom) token).name;
				field_type = FieldType.Mutable_Global;
			}else {
				throw new StructureFieldInvalidEx();
			}
		}else if(element.nodes.size() == 3) {
			Atom public_or_private = element.nodes.get(0);
			boolean is_public = true;
			if(public_or_private instanceof KeywordAtom) {
				if(((KeywordAtom) public_or_private).keyword.equals("public")) is_public = true;
				else if(((KeywordAtom) public_or_private).keyword.equals("private"))is_public = false;
				else throw new StructureFieldInvalidEx();
			}
			
			field_value = element.nodes.get(2);
			
			Atom token = element.nodes.get(1);
			if(token instanceof KeywordAtom) {
				field_name = ((KeywordAtom) token).keyword;
				if(is_public) field_type = FieldType.Immutable_Global;
				else field_type = FieldType.Immutable_Hidden;
			}else if(token instanceof VariableAtom) {
				field_name = ((VariableAtom) token).name;
				if(is_public) field_type = FieldType.Mutable_Global;
				else field_type = FieldType.Mutable_Hidden;
			}else {
				throw new StructureFieldInvalidEx();
			}
		}
	}
}

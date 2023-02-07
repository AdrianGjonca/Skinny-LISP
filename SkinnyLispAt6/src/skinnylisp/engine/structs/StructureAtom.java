package skinnylisp.engine.structs;

import java.util.List;

import data.Pair;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.parser.atoms.KeywordAtom;

public class StructureAtom extends Atom{
	public List<StructureField> fields;
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		String output = "";
		output += tabStr + "#STRUCTURE#\n";
		for(StructureField field : fields) {
			if(field.field_value instanceof StructureAtom) {
				output += tabStr + "  * " +field.field_name + " INTERNAL STRUCT\n";
				output += field.field_value.toString(tab+2);
			}else {
				output += tabStr + "  * " +field.field_name + " - " + field.field_value + "\n";
				output += tabStr + "  |---->" + field.field_type + "\n";
			}
		}
		output += tabStr + "###########\n";
		return output.replace("\n\n", "\n");
	}
	
	public StructureAtom(List<StructureField> fields) {
		this.fields = fields;
	}
}

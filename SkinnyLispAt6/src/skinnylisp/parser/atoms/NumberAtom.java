package skinnylisp.parser.atoms;

import skinnylisp.OutC;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.parser.atoms.numtypes.NumberType;

public class NumberAtom extends Atom {

	public long rawData;
	public NumberType type;
	
	public NumberAtom(String value) {
		if(value.contains(".")) {
			rawData = Double.doubleToRawLongBits(Double.parseDouble(value));
			type = NumberType.FLOAT;
		}else {
			rawData = Long.parseLong(value);
			type = NumberType.INTEGER;
		}
	}
	
	public Object getValue() {
		if(type == NumberType.FLOAT) return (double) Double.longBitsToDouble(rawData);
		else return (long) rawData;
	}
	
	public boolean isTrue() {
		return ((type == NumberType.FLOAT) ? (Double.longBitsToDouble(rawData)>0) : (rawData>0));
	}
	
	public NumberAtom(int value) {
		this.rawData = (long) value;
		this.type = NumberType.INTEGER;
	}
	
	public NumberAtom(long value) {
		this.rawData = value;
		this.type = NumberType.INTEGER;
	}

	public NumberAtom(double value) {
		this.rawData = Double.doubleToRawLongBits(value);
		this.type = NumberType.FLOAT;
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		if(type == NumberType.FLOAT) return tabStr + "#" + Double.longBitsToDouble(rawData);
		else return tabStr + "#" + (long) rawData;
	}

}

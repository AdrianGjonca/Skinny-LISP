package skinnylisp.ast.atoms;

public class NumberAtom extends Atom {

	public enum Type {
		INTEGER,
		FLOAT
	}
	
	public long rawData;
	public Type type;
	
	public NumberAtom(String value) {
		if(value.contains(".")) {
			rawData = Double.doubleToRawLongBits(Double.parseDouble(value));
			type = Type.FLOAT;
		}else {
			rawData = Long.parseLong(value);
			type = Type.INTEGER;
		}
	}
	
	public Object getValue() {
		if(type == Type.FLOAT) return (double) Double.longBitsToDouble(rawData);
		else return (long) rawData;
	}
	
	public boolean isTrue() {
		return ((type == Type.FLOAT) ? (Double.longBitsToDouble(rawData)>0) : (rawData>0));
	}
	
	public NumberAtom(int value) {
		this.rawData = (long) value;
		this.type = Type.INTEGER;
	}
	
	public NumberAtom(long value) {
		this.rawData = value;
		this.type = Type.INTEGER;
	}

	public NumberAtom(double value) {
		this.rawData = Double.doubleToRawLongBits(value);
		this.type = Type.FLOAT;
	}
	
	public String convertToString() {
		if(type == Type.FLOAT) return (Double.longBitsToDouble(rawData) + "");
		else return ((long) rawData + "");
	}
	
	@Override
	public String toString(int tab) {
		String tabStr = "";
		for(int i = 0; i<tab; i++) {
			tabStr+="  ";
		}
		if(type == Type.FLOAT) return tabStr + "#" + Double.longBitsToDouble(rawData);
		else return tabStr + "#" + (long) rawData;
	}

}

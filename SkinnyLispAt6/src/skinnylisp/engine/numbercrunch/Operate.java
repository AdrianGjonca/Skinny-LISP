package skinnylisp.engine.numbercrunch;

import java.util.List;

import skinnylisp.ast.atoms.NumberAtom;

public class Operate {
	public static NumberAtom sum(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			for(NumberAtom a : atoms) {
				if(a.type == NumberAtom.Type.FLOAT) {
					runningsum += Double.longBitsToDouble(a.rawData);
				}else {
					runningsum += (long)a.rawData;
				}
			}
			return new NumberAtom(runningsum);
		}else {
			long runningsum = 0;
			for(NumberAtom a : atoms) {
				runningsum += a.rawData;
			}
			return new NumberAtom(runningsum);
		}
	}
	public static NumberAtom sub(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					runningsum -= Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					runningsum -= atoms.get(i).rawData;
				}
			}
			return new NumberAtom(runningsum);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			for(int i = 1; i < atoms.size(); i++) {
				runningsum -= atoms.get(i).rawData;
			}
			return new NumberAtom(runningsum);
		}
	}
	public static NumberAtom prod(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 1;
			for(NumberAtom a : atoms) {
				if(a.type == NumberAtom.Type.FLOAT) {
					runningsum *= Double.longBitsToDouble(a.rawData);
				}else {
					runningsum *= a.rawData;
				}
			}
			return new NumberAtom(runningsum);
		}else {
			long runningsum = 1;
			for(NumberAtom a : atoms) {
				runningsum *= a.rawData;
			}
			return new NumberAtom(runningsum);
		}
	}
	public static NumberAtom div(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					runningsum /= Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					runningsum /= atoms.get(i).rawData;
				}
			}
			return new NumberAtom(runningsum);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			for(int i = 1; i < atoms.size(); i++) {
				runningsum /= atoms.get(i).rawData;
			}
			return new NumberAtom(runningsum);
		}
	}
	public static NumberAtom pow(List<NumberAtom> atoms) {
		return pow(atoms.get(0), prod(atoms.subList(1,atoms.size())));
	}
	public static NumberAtom mod(List<NumberAtom> atoms) {
		NumberAtom runningsum = atoms.get(0);
		for(int i = 1; i<atoms.size(); i++) {
			runningsum = mod(runningsum, atoms.get(i));
		}
		return runningsum;
	}
	
	
	public static NumberAtom log(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.log(value_as_double));
	}
	
	public static NumberAtom sin(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.sin(value_as_double));
	}
	public static NumberAtom cos(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.cos(value_as_double));
	}
	public static NumberAtom tan(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.tan(value_as_double));
	}
	
	public static NumberAtom asin(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.asin(value_as_double));
	}
	public static NumberAtom acos(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.acos(value_as_double));
	}
	public static NumberAtom atan(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.atan(value_as_double));
	}
	
	public static NumberAtom pow(NumberAtom base, NumberAtom exponent) {
		if(exponent.type == NumberAtom.Type.INTEGER && base.type == NumberAtom.Type.INTEGER) {
			long exponent_value = exponent.rawData;
			long base_value = base.rawData;
			
			if(base_value == 0) return new NumberAtom(0);
			if(exponent_value == 0) return new NumberAtom (1); 
			
			boolean reciprocal = (exponent_value < 0) ? true : false;
			if(reciprocal) exponent_value = -exponent_value;
			
			long result = ipow(base_value, exponent_value);
			
			if(reciprocal) {
				return new NumberAtom(1.0/((double) result));
			}else return new NumberAtom(result);
		}else {
			double base_value = 0;
			double exponent_value = 0;
			
			if(base.type ==  NumberAtom.Type.INTEGER) {
				base_value = (double) base.rawData;
			}else {
				base_value = Double.longBitsToDouble(base.rawData);
			}
			if(exponent.type ==  NumberAtom.Type.INTEGER) {
				exponent_value = (double) exponent.rawData;
			}else {
				exponent_value = Double.longBitsToDouble(exponent.rawData);
			}
			
			return new NumberAtom(Math.pow(base_value, exponent_value));
		}
	}
	public static NumberAtom mod(NumberAtom A, NumberAtom B) {
		if(A.type == NumberAtom.Type.FLOAT || B.type == NumberAtom.Type.FLOAT) {
			double A_value;
			double B_value;
			if(A.type == NumberAtom.Type.FLOAT) A_value = Double.longBitsToDouble(A.rawData);
			else A_value = (double) A.rawData;
			if(B.type == NumberAtom.Type.FLOAT) B_value = Double.longBitsToDouble(B.rawData);
			else B_value = (double) B.rawData;
			
			return new NumberAtom(A_value % B_value);
		}else 
			return new NumberAtom(A.rawData % B.rawData);
	}
	
	public static NumberAtom round(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.round(value_as_double));
	}
	public static NumberAtom floor(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.floor(value_as_double));
	}
	public static NumberAtom ceil(NumberAtom value) {
		double value_as_double = 0;
		
		if(value.type ==  NumberAtom.Type.INTEGER) {
			value_as_double = (double) value.rawData;
		}else {
			value_as_double = Double.longBitsToDouble(value.rawData);
		}
		return new NumberAtom(Math.ceil(value_as_double));
	}
	
	static long ipow(long base, long exp)
	{
	    long result = 1;
	    for (;;)
	    {
	        if ((exp & 1) != 0)
	            result *= base;
	        exp >>= 1;
	        if (exp == 0)
	            break;
	        base *= base;
	    }

	    return result;
	}
	
	
	public static NumberAtom greaterThan(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					yes = runningsum > Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					yes = runningsum > atoms.get(i).rawData;
				}
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				yes = runningsum > atoms.get(i).rawData;
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}
	}
	public static NumberAtom lessThan(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					yes = runningsum < Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					yes = runningsum < atoms.get(i).rawData;
				}
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				yes = runningsum < atoms.get(i).rawData;
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}
	}
	public static NumberAtom greaterThanOrEQ(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					yes = runningsum >= Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					yes = runningsum >= atoms.get(i).rawData;
				}
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				yes = runningsum >= atoms.get(i).rawData;
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}
	}
	public static NumberAtom lessThanOrEQ(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					yes = runningsum <= Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					yes = runningsum <= atoms.get(i).rawData;
				}
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}else {
			long runningsum = 0;
			runningsum += atoms.get(0).rawData;
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				yes = runningsum <= atoms.get(i).rawData;
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}
	}

	public static NumberAtom equal(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberAtom.Type.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double mainValue = 0;
			if(atoms.get(0).type == NumberAtom.Type.FLOAT) {
				mainValue = Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				mainValue = atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberAtom.Type.FLOAT) {
					yes = mainValue == Double.longBitsToDouble(atoms.get(i).rawData);
				}else {
					yes = mainValue == atoms.get(i).rawData;
				}
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}else {
			
			long mainValue = 0;
			mainValue = atoms.get(0).rawData;
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				yes = mainValue == atoms.get(i).rawData;
				if(!yes) break;
			}
			return new NumberAtom((yes) ? 1 : 0);
		}
	}

}

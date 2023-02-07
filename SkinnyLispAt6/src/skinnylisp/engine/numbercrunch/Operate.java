package skinnylisp.engine.numbercrunch;

import java.util.List;

import skinnylisp.OutC;
import skinnylisp.parser.atoms.NumberAtom;
import skinnylisp.parser.atoms.numtypes.NumberType;

public class Operate {
	public static NumberAtom sum(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			for(NumberAtom a : atoms) {
				if(a.type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 1;
			for(NumberAtom a : atoms) {
				if(a.type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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

	public static NumberAtom greaterThan(List<NumberAtom> atoms) {
		boolean floatornot = false;
		for(NumberAtom a : atoms) {
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			double runningsum = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				runningsum += Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				runningsum += atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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
			if(a.type == NumberType.FLOAT) {
				floatornot = true;
				break;
			}
		}
		if(floatornot) {
			OutC.debug("This mode");
			double mainValue = 0;
			if(atoms.get(0).type == NumberType.FLOAT) {
				mainValue = Double.longBitsToDouble(atoms.get(0).rawData);
			}else {
				mainValue = atoms.get(0).rawData;
			}
			boolean yes = true;
			for(int i = 1; i < atoms.size(); i++) {
				if(atoms.get(i).type == NumberType.FLOAT) {
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

package skinnylisp.lexer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import skinnylisp.OutC;

public class SlicerUpper {
	public static int rejoinCalls = 0;
	public static String rejoin(List<String> toJoin) {
		rejoinCalls++;
		if(toJoin.size() == 0) return "";
		String out = "";
		
		int x = 0;
		for(String a : toJoin) {
			//out += a + " ";
			x += a.length() + 1;
		}
		char cOut[] = new char[x];
		x = 0;
		for(String a : toJoin) {
			for(char c:a.toCharArray()) {
				cOut[x] = c;
				x++;
			}
			cOut[x] = ' ';
			x ++;
		}
		out = new String(cOut);
		out.trim();
		OutC.debug(out);
		return out;
	}
	
	public static String insert(String main, String inject, int index) {
		char cMain[] = main.toCharArray();
		char cInject[] = inject.toCharArray();
		char cOut[] = new char[main.length() + inject.length()];
		for(int x = 0; x<index; x++) {
			cOut[x] = cMain[x];
		}
		int y = 0;
		for(int x = index; x<index+cInject.length; x++) {
			cOut[x] = cInject[y];
			y++;
		}
		y = index;
		for(int x = index+cInject.length; x<cOut.length; x++) {
			cOut[x] = cMain[y];
			y++;
		}
		return new String(cOut);
	}
	public static int sliceCalls = 0;
	public static List<String> slice(String command) {
		sliceCalls++;
		command = command.trim();
		command += " ";
		List<String> atoms = new LinkedList<String>();
		String atom = "";
		
		int i = 0;
		int atomstart = 0;
		char cCommand[] = command.toCharArray();
		while(i < command.length()) {
			if(cCommand[i] == ' ') {
				while(cCommand[atomstart] == ' ' && atomstart != i) atomstart++;
				atoms.add(command.substring(atomstart,i));
				i++;
				atomstart = i;
			}else if(cCommand[i] == '(') {
				i++;
				int level = 1;
				int start = i;
				while(level > 0) {
					if(cCommand[i] == ')') level--;
					if(cCommand[i] == '(') level++;
					i++;
				}
				atoms.add(command.substring(start-1,i));
				atomstart = i;
				if(i < command.length()-1) while(command.charAt(i) == ' ') i++;
			}else if(command.charAt(i) == '"') {
				i++;
				int start = i;
				while(cCommand[i] != '"') i++;
				i++;
				atoms.add(command.substring(start-1,i));
				atomstart = i;
			}else {
				i++;
			}
			
		}
		
		for(i = 0; i<atoms.size(); i++) {
			if(atoms.get(i).length() == 0) {
				atoms.remove(i);
				i--;
			}
		}
		return atoms;
	}
}

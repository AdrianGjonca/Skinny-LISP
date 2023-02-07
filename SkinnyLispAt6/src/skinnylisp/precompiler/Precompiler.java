package skinnylisp.precompiler;

import java.util.LinkedList;
import java.util.List;

import skinnylisp.OutC;

public class Precompiler {
	public static String precomp(String unprocessed) {
		return precomp_BAR(precomp_COLONS(unprocessed));
	}
	public static String precomp_COLONS(String unprocessed) {
		try {
			char[] chars = unprocessed.toCharArray();
			boolean inStr = false;
			for(int i = 0; i<unprocessed.length(); i++) {
				if(chars[i] == '"') inStr = !inStr;
				if(!inStr)
				if(chars[i] == ':') {
					int start = i;
					int level = 0;
					while(level >= 0) {
						if(chars[i] == '"') inStr = !inStr;
						i++;
						if(!inStr) {
							if(chars[i] == '(') level++;
							if(chars[i] == ')') level--;
						}
					}
					String toBracket = unprocessed.substring(start+1, i);
					//OutC.debug(toBracket);
					List<String> vals = new LinkedList<String>();
					int _start = 0;
					for(int r = 0; r<toBracket.length(); r++) { // (do : a; b; c;) (do : c; "hi;i"; e;)
						while(toBracket.charAt(r) != ';' || r>toBracket.length()) {
							r++;
							if(r == toBracket.length()) {
								r--;
								break;
							}
							if(toBracket.charAt(r) == '"') {
								int a = r;
								r++;
								while(toBracket.charAt(r) != '"') {
									r++;
									if(r == toBracket.length()) {
										r-= 2;
										break;
									}
								}
								r++;
								//OutC.debug(toBracket.substring(a,r));
							}
							if(toBracket.charAt(r) == '(') {
								int _level = 1;
								int a = r;
								r++;
								while(_level > 0) {
									if(toBracket.charAt(r) == '(') _level++;
									if(toBracket.charAt(r) == ')') _level--;
									r++;
									
								}
								//r++;
								//OutC.debug(toBracket.substring(a,r));
							}
							if(r == toBracket.length()) {
								r--;
								break;
							}
						}
						String o = precomp_COLONS(toBracket.substring(_start,r));
						if(o.length() > 0) vals.add(o);
						_start = r+1;
					}
					String out = unprocessed.substring(0, start);
					for(String a : vals) {
						out += " ( " + a + " ) ";
					}
					return out + precomp_COLONS(unprocessed.substring(i));
				};
			}
			/*
			char[] out = unprocessed.toCharArray();
			for(int i = 0; i<out.length; i++) {
				if(out[i] == '"') {
					i++;
					while(out[i] != '"') i++;
				}
				if(out[i] == ';') out[i] = ')';
			}*/
			return unprocessed;
		}catch (ArrayIndexOutOfBoundsException e) {
			throw new PrecompilerError();
		}
	}
	public static String precomp_BAR(String unprocessed) {
		try {
			char[] chars = unprocessed.toCharArray();
			boolean inStr = false;
			for(int i = 0; i<unprocessed.length(); i++) {
				if(chars[i] == '"') inStr = !inStr;
				if(!inStr)
				if(chars[i] == '|') {
					chars[i] = '(';
					int level = 0;
					while(level >= 0) {
						if(chars[i] == '"') inStr = !inStr;
						i++;
						if(!inStr) {
							if(chars[i] == '(') level++;
							if(chars[i] == ')') level--;
						}
					}
					String out = new String(chars);
					return precomp(out.substring(0,i) + ")" + out.substring(i));
				};
			}
			return unprocessed;
		}catch (ArrayIndexOutOfBoundsException e) {
			throw new PrecompilerError();
		}
	}
}

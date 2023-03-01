package skinnylisp.precompiler;

import java.util.LinkedList;
import java.util.List;

public class Precompiler {
	public static String precomp(String unprocessed) {
		return precomp_EX(
				precomp_BAR(
				 precomp_COLONS(unprocessed)));
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
								r++;
								while(toBracket.charAt(r) != '"') {
									r++;
									if(r == toBracket.length()) {
										r-= 2;
										break;
									}
								}
								r++;
							}
							if(toBracket.charAt(r) == '(') {
								int _level = 1;
								r++;
								while(_level > 0) {
									if(toBracket.charAt(r) == '(') _level++;
									if(toBracket.charAt(r) == ')') _level--;
									r++;
								}
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
	
	public static String precomp_EX(String unprocessed) {
		try {
			boolean in_string = false;
			for(int i = unprocessed.length()-1; i>=0; i--) {
				if(unprocessed.charAt(i) == '"') in_string = !in_string;
				if(!in_string) {
					if(unprocessed.charAt(i) == '!') {
						int level = 0;
						int pos = i-1;
						while(level >= 0) {
							if(unprocessed.charAt(pos) == ')') level++;
							if(unprocessed.charAt(pos) == '(') level--;
							pos--;
						}
						pos+=2;
						return precomp(
								unprocessed.substring(0, pos) +
								"("+unprocessed.substring(pos, i)+")" +
								unprocessed.substring(i + 1));
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new PrecompilerError();
		} catch (StringIndexOutOfBoundsException e) {
			throw new PrecompilerError();
		}
		return unprocessed;
	}
}

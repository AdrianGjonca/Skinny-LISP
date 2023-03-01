package skinnylisp.parser;

import skinnylisp.exceptions.CodeInvalidEx;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.lexer.atoms.TokenAtom;
import skinnylisp.parser.atoms.KeywordAtom;
import skinnylisp.parser.atoms.LambdaVariableAtom;
import skinnylisp.parser.atoms.NumberAtom;
import skinnylisp.parser.atoms.StringAtom;
import skinnylisp.parser.atoms.VariableAtom;

public class Parser {
	public static void pass1(ListAtom root) {
		int index = 0;
		for(Atom atom: root.nodes) {
			if(atom instanceof ListAtom) {
				pass1((ListAtom)atom);
			}else if(atom instanceof TokenAtom) {
				TokenAtom token = (TokenAtom) atom;
				if(token.text.charAt(0) == '"') {
					try {
						root.nodes.set(index, new StringAtom(token.text));
					} catch (CodeInvalidEx e) {
						e.printStackTrace();
					}
				}else if(token.text.charAt(0) == '$') {
					root.nodes.set(index, new VariableAtom(token.text));
				}else if(token.text.charAt(0) == '\\') {
					root.nodes.set(index, new LambdaVariableAtom(token.text));
				}else if(isNo(token.text)) {
					root.nodes.set(index, new NumberAtom(token.text));
				}else {
					root.nodes.set(index, new KeywordAtom(token.text));
				}
			}
			index++;
		}
	}
	
	public static ListAtom parse(ListAtom root) {
		Parser.pass1(root);
		return root;
	}
	
	public static boolean isNo(String a) {
		switch(a) {
		case "+":
			return false;
		case "-":
			return false;
		case "*":
			return false;
		case "/":
			return false;
		}
		if(a.startsWith("-")) a = a.substring(1);
		for (char x : a.toCharArray()) {
			if ((x < 48 || x > 57) && x != 46)
				return false;
		}
		return true;
	}
}

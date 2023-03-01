package skinnylisp.ast.parser;

import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.KeywordAtom;
import skinnylisp.ast.atoms.LambdaVariableAtom;
import skinnylisp.ast.atoms.NumberAtom;
import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.ast.atoms.StringAtom;
import skinnylisp.ast.atoms.TokenAtom;
import skinnylisp.ast.atoms.VariableAtom;
import skinnylisp.ast.exceptions.CodeInvalidEx;

public class Parser {
	public static void pass1(StatementAtom root) {
		int index = 0;
		for(Atom atom: root.nodes) {
			if(atom instanceof StatementAtom) {
				pass1((StatementAtom)atom);
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
	
	public static StatementAtom parse(StatementAtom root) {
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

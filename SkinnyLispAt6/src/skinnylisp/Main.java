package skinnylisp;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.function.Function;

import skinnylisp.engine.Interpreter;
import skinnylisp.entrypoints.REPL;
import skinnylisp.exceptions.CodeInvalidEx;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.parser.Parser;
import skinnylisp.precompiler.Precompiler;

public class Main {
	
	public static void main(String [] args) {
		repl();
	}
	
	public static void precompDebug() {
		Scanner scan = new Scanner(System.in);
		String input = "";
		while(!input.equals(":q")) {
			System.out.print("> ");
			input = scan.nextLine();
			String out = Precompiler.precomp(input);
			System.out.println("£"+out);
		} // (set $myFunc | lambda (/n) | do (return | add /n 5))
	}
	
	public static void loadFile() {
		
		String code = "";
		try {
			File    myObj    = new File("/home/adrian/Documents/test.slisp");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				code += data + "\n";
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		
		REPL repl = new REPL(System.out, System.in);
		repl.exec(code);
		repl.displayTitle();
		while(true) {
			repl.line();
		}
	}
	
	public static void repl() {
		REPL repl = new REPL(System.out, System.in);
		repl.displayTitle();
		while(true) {
			repl.line();
		}
	}
}

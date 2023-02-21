package skinnylisp.entrypoints;

import java.io.PrintStream;
import java.util.Scanner;

import skinnylisp.engine.Interpreter;
import skinnylisp.exceptions.CodeInvalidEx;
import skinnylisp.exceptions.LispRuntimeError;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.parser.Parser;
import skinnylisp.precompiler.Precompiler;

public class REPL {
	public PrintStream print_stream;
	public Scanner scanner;
	public Interpreter interpreter;
	public REPL(PrintStream print_stream, Scanner scanner) {
		this.print_stream = print_stream;
		this.scanner = scanner;
		this.interpreter = new Interpreter();
	}
	
	public void doIt(String code) {
		ListAtom abstract_syntax_tree = null;
		try {
			abstract_syntax_tree = new ListAtom(code);
			abstract_syntax_tree = Parser.parse(abstract_syntax_tree);
		} catch (CodeInvalidEx e) {
			print_stream.println("Code invalid");
		}
		
		try {
			print_stream.println(interpreter.run(abstract_syntax_tree));
		} catch (LispRuntimeError e) {
			e.printStackTrace();
			print_stream.println("ERROR \n    " + e.error.getErrorMessage().replace("\n", "\n    ") + "\n" + "END ERROR");
		}
	}
	
	public void exec(String code) {
		code = Precompiler.precomp(code);
		
		doIt(code);
	}
	
	public void line() {
		String code = takeInput();
		code = Precompiler.precomp(code);
		
		doIt(code);
	}
	
	public void displayTitle() {
		print_stream.println("Skinny-LISP by Adrian Gjonca");
		print_stream.println("[Interpreter written in Java]");
		print_stream.println("");
	}
	
	public String takeInput() {
		String input_buffer = "";
		System.out.print("> ");
		input_buffer = scanner.nextLine();
		
		while(!isCompleteStatement(input_buffer)) {
			System.out.print("  ");
			input_buffer += scanner.nextLine();
		}
		
		return input_buffer;
	}
	
	public boolean isCompleteStatement(String input_buffer) {
		int bracket_count = 0;
		boolean inside_string = false;
		for(int i = 0; i<input_buffer.length(); i++) {
			if(input_buffer.charAt(i) == '"') inside_string = !inside_string;
			if(!inside_string) {
				if(input_buffer.charAt(i) == '(') bracket_count++;
				if(input_buffer.charAt(i) == ')') bracket_count--;
			}
		}
		return bracket_count <= 0;
	}
}

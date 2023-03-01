package skinnylisp.entrypoints;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.ast.exceptions.CodeInvalidEx;
import skinnylisp.ast.lexer.Precompiler;
import skinnylisp.ast.parser.Parser;
import skinnylisp.engine.Interpreter;
import skinnylisp.engine.WrapOf_RuntimeError;

public class REPL {
	public PrintStream print_stream;
	public Scanner scanner;
	public Interpreter interpreter;
	public REPL(PrintStream print_stream, InputStream input_stream) {
		this.print_stream = print_stream;
		print_stream.print("\u001b[0m");
		this.scanner = new Scanner(input_stream);
		this.interpreter = new Interpreter();
		this.interpreter.printstream = print_stream;
		this.interpreter.inputstream = input_stream;
	}
	
	public void doIt(String code) {
		StatementAtom abstract_syntax_tree = null;
		try {
			abstract_syntax_tree = new StatementAtom(code);
			abstract_syntax_tree = Parser.parse(abstract_syntax_tree);
		} catch (CodeInvalidEx e) {
			print_stream.println("Code invalid");
		}
		
		try {
			OutC.debug(print_stream, interpreter.run(abstract_syntax_tree));
		} catch (WrapOf_RuntimeError e) {
			e.printStackTrace();
			OutC.error(print_stream, "ERROR \n    " + e.error.getErrorMessage().replace("\n", "\n    ") + "\n" + "END ERROR");
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

package skinnylisp;

import java.io.PrintStream;

public class OutC {
	public static void error(PrintStream stream,Object text) {
		stream.println("\u001b[31m"+ text + "\u001b[0m");
	}
	public static void debug(PrintStream stream,Object text) {
		stream.println("\u001b[35m"+ text + "\u001b[0m");
	}
}

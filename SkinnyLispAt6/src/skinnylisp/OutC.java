package skinnylisp;

public class OutC {
	public static void error(Object text) {
		System.out.println("\u001b[31m[ERROR] "+ text + "\u001b[0m");
	}
	public static void debug(Object text) {
		System.out.println("\u001b[35m[DEBUG] "+ text + "\u001b[0m");
	}
}

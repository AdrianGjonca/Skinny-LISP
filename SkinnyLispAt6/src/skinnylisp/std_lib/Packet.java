package skinnylisp.std_lib;

import java.util.LinkedList;
import java.util.List;

import data.Pair;

public class Packet {
	private List<Pair<String, StatementHandler>> commands;
	
	public Packet() {
		commands = new LinkedList<Pair<String, StatementHandler>>();
	}
	
	public List<Pair<String, StatementHandler>> getCommands(){
		return commands;
	}
	
	public void add(String token, StatementHandler function) {
		commands.add(new Pair<String, StatementHandler>(token, function));
	}
	public void add(String[] tokens, StatementHandler function) {
		for(String token : tokens) {
			commands.add(new Pair<String, StatementHandler>(token, function));
		}
	}
}

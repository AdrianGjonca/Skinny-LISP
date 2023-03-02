package skinnylisp.engine;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import data.Mutable;
import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.KeywordAtom;
import skinnylisp.ast.atoms.LambdaVariableAtom;
import skinnylisp.ast.atoms.NumberAtom;
import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.ast.atoms.StringAtom;
import skinnylisp.ast.atoms.VariableAtom;
import skinnylisp.engine.numbercrunch.Operate;
import skinnylisp.engine.runtime_errors.Err_IncorrectArgs;
import skinnylisp.engine.runtime_errors.Err_Ubounds;
import skinnylisp.engine.runtime_errors.Err_Undefined;
import skinnylisp.engine.runtime_errors.RuntimeError;
import skinnylisp.engine.runtime_errors.Err_Undefined.AtomType;
import skinnylisp.engine.runtimeatoms.FuncAtom;
import skinnylisp.engine.runtimeatoms.ListAtom;
import skinnylisp.engine.runtimeatoms.ListAtom.ListType;
import skinnylisp.std_lib.ImportManager;

public class Interpreter {
	private final static HashMap<String, Atom> NULL_LAMBDA_VARS = new HashMap<String, Atom>();
	private final static ImportManager ALLS = new ImportManager(true);
	
	public PrintStream printstream = System.out;
	public InputStream inputstream = System.in;
	
	
	public HashMap<String, Atom> variables_map;
	
	public Interpreter() {
		variables_map = new HashMap<String, Atom>();
	}
	public Atom run(Atom in_atom) throws WrapOf_RuntimeError {
		return run(in_atom, NULL_LAMBDA_VARS);
	}
	
	private Atom _commandPart(Atom head, StatementAtom statement, HashMap<String, Atom> lambda_vars, Mutable<Boolean> complete) throws WrapOf_RuntimeError{
		complete.value = true;
		if (head instanceof KeywordAtom) {
			return handleKeyword(statement, lambda_vars);
		}else if (head instanceof FuncAtom) {
			FuncAtom head_as_LambdaAtom = (FuncAtom) head;
			HashMap<String, Atom> lambda_vars_to_pass_on = new HashMap<String, Atom>();

			int index_on_nodes = 1;
			if(statement.nodes.size() - 1 != head_as_LambdaAtom.params.size()) {
				String error = "!("+ head_as_LambdaAtom.name +" ";
				for(int i = 0; i<head_as_LambdaAtom.types.size(); i++) {
					error += "?"
						   + head_as_LambdaAtom.types.get(i).toString()
						   + ((i+1 == head_as_LambdaAtom.types.size()) ? ")" : " ");
					
				}
				throw error_arg(error);
			}
			
			for (String par : head_as_LambdaAtom.params) {
				Atom n;
				n = run(statement.nodes.get(index_on_nodes), lambda_vars);
				
				boolean type_valid = false;
				for(LispType possible : head_as_LambdaAtom.types.get(index_on_nodes-1)) {
					Class<?> atom_class = possible.atom_class;
					if(atom_class.isInstance(n)) {
						if(atom_class == NumberAtom.class) {
							switch(possible) {
							case Integer:
								if(((NumberAtom) n).type == NumberAtom.Type.INTEGER) type_valid = true;
								break;
							case Float:
								if(((NumberAtom) n).type == NumberAtom.Type.FLOAT) type_valid = true;
								break;
							default:
								type_valid = true;
								break;
							}
							break;
						}else {
							type_valid = true;
							break;
						}
					}
				}
				
				if(!type_valid) {
					String error = "("+ head_as_LambdaAtom.name +" ";
					int check = 10000;
					for(int i = 0; i<head_as_LambdaAtom.types.size(); i++) {
						if(index_on_nodes-1 == i) {
							error +="!";
							check = i;
						}
						error += ((i>check) ? "?" : "") 
							   + head_as_LambdaAtom.types.get(i).toString()
							   + ((i+1 == head_as_LambdaAtom.types.size()) ? ")" : " ");
						
					}
					throw error_arg(error);
				}
				else lambda_vars_to_pass_on.put(par, n);
				index_on_nodes++;
			}
			lambda_vars_to_pass_on.put("", head_as_LambdaAtom);
			return run((StatementAtom) head_as_LambdaAtom.process, lambda_vars_to_pass_on);
		} else {
			complete.value = false;
			return null;
		}
		
		
	}
	private NumberAtom _operate(String operation, List<NumberAtom> nums) {
		return (operation.equals("+"))  ? Operate.sum(nums)  		    :
			   (operation.equals("-"))  ? Operate.sub(nums)   		    :
			   (operation.equals("*"))  ? Operate.prod(nums) 			:
			   (operation.equals("/"))  ? Operate.div(nums)  			:
			   (operation.equals("%"))  ? Operate.mod(nums)  			:
			   (operation.equals("^"))  ? Operate.pow(nums)  			:
			   (operation.equals("="))  ? Operate.equal(nums)           :
			   (operation.equals(">"))  ? Operate.greaterThan(nums)     :
			   (operation.equals("<"))  ? Operate.lessThan(nums)        :
			   (operation.equals(">=")) ? Operate.greaterThanOrEQ(nums) :
			   (operation.equals("<=")) ? Operate.lessThanOrEQ(nums)    : null;   
	}
	private Atom _handleInfix(NumberAtom head, StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError{
		if(statement.nodes.size() == 1) return head;
		String opperation = "+";
		List<NumberAtom> nums = new LinkedList<NumberAtom>();
		nums.add(head);
		int size = statement.nodes.size();
			
		for(int i = 1; i<size; i++) {
			Atom atom = statement.nodes.get(i); 
			if(atom instanceof KeywordAtom) {
				head = _operate(opperation, nums);   
				if(head == null) throw error_arg(opperation + " is an invalid numerical operation");
				opperation = ((KeywordAtom) atom).keyword;
				nums = new LinkedList<NumberAtom>();
				nums.add(head);
			}else {
				atom = run(atom, lambda_vars);
				if(atom instanceof NumberAtom) {
					nums.add((NumberAtom) atom);
				}else throw error_arg("On mathamatical opperation: ![Number | Keyowrd.math]" + atom + statement.nodes.get(i));
			}
			
			
		} 
		head = _operate(opperation, nums);
		if(head == null) throw error_arg(opperation + " is an invalid numerical operation");
		return head;	
	}
	private Atom _2_handleDataList(ListAtom head, StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError {
		Atom second_node = statement.nodes.get(1);
		
		if(second_node instanceof KeywordAtom) {
			String keyword_name = ((KeywordAtom) second_node).keyword;
			
			if(keyword_name.equals("size")) return new NumberAtom(head.list.size());
			else throw error_arg(("(List Keyword.!'size')"));
		}else {
			second_node = run(second_node, lambda_vars);
			
			if(!(second_node instanceof NumberAtom)) 
				throw error_arg(("(List ![Keyword.'size' | Integer])"));
			else {
				NumberAtom second_node_As_NumberAtom = (NumberAtom) second_node;
				if(second_node_As_NumberAtom.type != NumberAtom.Type.INTEGER) 
					throw error_arg(("(List !Integer)"));
				else {
					int index = (int) second_node_As_NumberAtom.rawData;
					int size = head.list.size();
					
					if(index >= size || index < 0) throw error(new Err_Ubounds(size, index));
					else return head.list.get(index); 
				}
			}
		}
	}
	private Atom _3_handleDataList(ListAtom head, StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError {
		Atom second_node = statement.nodes.get(1);
		Atom third_node = statement.nodes.get(2);
		
		if(!(second_node instanceof KeywordAtom)) 
			throw error_arg(("(List !Keyword.['append' | 'remove'] ?Integer)"));
		else {
			third_node = run(third_node, lambda_vars);
			List<Atom> the_list = head.list;
			
			switch(((KeywordAtom)second_node).keyword) {
			case "append":
				the_list.add(third_node);
				break;
			case "remove":
				if(!(third_node instanceof NumberAtom)) 
					throw error_arg(("(List Keyword.'remove' !Integer~\"we got a Float\")"));
				else {
					if(((NumberAtom)third_node).type != NumberAtom.Type.INTEGER) 
						throw error_arg(("(List Keyword.'remove' !Integer)"));
					else{
						int index = (int) ((NumberAtom)third_node).rawData;
						int size = the_list.size();
						
						if(index >= size || index < 0) throw error(new Err_Ubounds(size, index));
						else the_list.remove(index);
						
					}
				} 
				break;
			default:
				throw error_arg(("(List Keyword.!['append' | 'remove'] ?Integer)"));
			}
		}
		
		return null;
	}
	private Atom _4_handleDataList(ListAtom head, StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError {
		Atom second_node = statement.nodes.get(1);
		Atom third_node = statement.nodes.get(2);
		Atom fourth_node = statement.nodes.get(3);
		
		if(!(second_node instanceof KeywordAtom))
			throw error_arg(("(List !Keyword.'set' ?Integer ?Atom)"));
		else{
			
			third_node  = run(third_node, lambda_vars);
			fourth_node = run(fourth_node, lambda_vars);
			
			String name = ((KeywordAtom)second_node).keyword;
			if(!name.equals("set")) throw error_arg(("(List Keyword.!'set' ?Integer ?Atom)"));
			else {
				if(!(third_node instanceof NumberAtom)) 
					throw error_arg(("(List Keyword.'set' !Integer ?Atom)"));
				else {
					if(((NumberAtom)third_node).type != NumberAtom.Type.INTEGER)  
						throw error_arg(("(List Keyword.!'set' !Integer~\"we got a Float\" ?Atom)"));
					else {
						List<Atom> the_list = head.list;
						
						int index = (int) ((NumberAtom)third_node).rawData;
						int size = the_list.size();
						
						if(index >= size || index < 0) throw error(new Err_Ubounds(size, index));
						else the_list.set(index, fourth_node);
					}
				}
			}
		}
		
		return null;
	}
	private Atom _handleDataList(ListAtom head, StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError{		
		switch (statement.nodes.size()) {
		case 1:
			return head;
		case 2:
			return _2_handleDataList(head, statement, lambda_vars);
		case 3:
			return _3_handleDataList(head, statement, lambda_vars);
		case 4:
			return _4_handleDataList(head, statement, lambda_vars);
		default:
			throw error_arg(("(List Atom*![0 | 1 | 2 | 3]"));
		}
	}
	
	public Atom run(Atom in_statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError {
		if (!(in_statement instanceof StatementAtom)) {
			StatementAtom onwards_list = new StatementAtom();
			onwards_list.nodes.add(in_statement);
			in_statement = onwards_list;
		}
		
		StatementAtom statement = (StatementAtom) in_statement;
		if (statement.nodes.size() == 0) return null;
		
		Atom head = statement.nodes.get(0);
		if (head instanceof LambdaVariableAtom) {
			if (lambda_vars.containsKey(((LambdaVariableAtom) head).name)) head = lambda_vars.get(((LambdaVariableAtom) head).name);
			else throw error(new Err_Undefined(((VariableAtom) head).name , AtomType.LambdaVariable));
		}else if (head instanceof VariableAtom) {
			if (variables_map.containsKey(((VariableAtom) head).name)) head = variables_map.get(((VariableAtom) head).name);
			else throw error(new Err_Undefined(((VariableAtom) head).name , AtomType.Variable));
		}
		
		if (head instanceof StatementAtom) head = run(head, lambda_vars);
		
		if (head instanceof NumberAtom) return _handleInfix((NumberAtom) head, statement, lambda_vars);
		if (head instanceof ListAtom) return _handleDataList((ListAtom) head, statement, lambda_vars);
		else {
			Mutable<Boolean> complete = new Mutable<Boolean>(false);
			Atom possible = null;
			possible = _commandPart(head, statement, lambda_vars, complete);
			if(complete.value) return possible;
		}
		return head;
	}
	private Atom handleKeyword(StatementAtom statement, HashMap<String, Atom> lambda_vars) throws WrapOf_RuntimeError {
		String head_name = ((KeywordAtom) statement.nodes.get(0)).keyword;
		return ALLS.handleKeyword(statement, lambda_vars, this, head_name);
	}

	
	/*
	 * 
	 * UTILITIES
	 * 
	 */
	public static WrapOf_RuntimeError error(RuntimeError error){
		return new WrapOf_RuntimeError(error);
	}
	public static WrapOf_RuntimeError error() {
		return new WrapOf_RuntimeError();
	}
	public static WrapOf_RuntimeError error_arg(String e) {
		return new WrapOf_RuntimeError(new Err_IncorrectArgs(e));
	}

}

package skinnylisp.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import data.Pair;
import skinnylisp.OutC;
import skinnylisp.engine.lists.DataListAtom;
import skinnylisp.engine.lists.ListType;
import skinnylisp.engine.numbercrunch.Operate;
import skinnylisp.exceptions.LispRuntimeError;
import skinnylisp.exceptions.runtime_errors.Err_IncorrectArgs;
import skinnylisp.exceptions.runtime_errors.Err_Ubounds;
import skinnylisp.exceptions.runtime_errors.Err_Undefined;
import skinnylisp.exceptions.runtime_errors.Err_Undefined.AtomType;
import skinnylisp.exceptions.runtime_errors.LispError;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.lexer.atoms.TokenAtom;
import skinnylisp.parser.atoms.KeywordAtom;
import skinnylisp.parser.atoms.LambdaVariableAtom;
import skinnylisp.parser.atoms.NumberAtom;
import skinnylisp.parser.atoms.StringAtom;
import skinnylisp.parser.atoms.VariableAtom;
import skinnylisp.parser.atoms.numtypes.NumberType;
import skinnylisp.precompiler.Precompiler;

public class Interpreter {

	private final static HashMap<String, Atom> NULL_LAMBDA_VARS = new HashMap<String, Atom>();
	
	public HashMap<String, Atom> variables_map;
	public Interpreter() {
		variables_map = new HashMap<String, Atom>();
	}
	
	
	/*
	 * 
	 * THE RECURSIVE ENGINE
	 * 
	 */
	public Atom run(Atom in_atom, HashMap<String, Atom> lambda_vars) throws LispRuntimeError {
		if (in_atom instanceof ListAtom) {
			ListAtom in_as_ListAtom = (ListAtom) in_atom;
			if (in_as_ListAtom.nodes.size() == 0)
				return null;

			Atom head_of_list = in_as_ListAtom.nodes.get(0);
			if (head_of_list instanceof LambdaVariableAtom) {
				if (!lambda_vars.containsKey(((LambdaVariableAtom) head_of_list).name)) 
					throw error(new Err_Undefined(((VariableAtom) head_of_list).name , AtomType.LambdaVariable));
				head_of_list = lambda_vars.get(((LambdaVariableAtom) head_of_list).name);
			}
			if (head_of_list instanceof VariableAtom) {
				if (!variables_map.containsKey(((VariableAtom) head_of_list).name)) 
					throw error(new Err_Undefined(((VariableAtom) head_of_list).name , AtomType.Variable));
				head_of_list = variables_map.get(((VariableAtom) head_of_list).name);
			}

			if (head_of_list instanceof ListAtom) {
				head_of_list = run(head_of_list, lambda_vars);
			}

			if (head_of_list instanceof KeywordAtom) {
				return handleKeyword(in_as_ListAtom, lambda_vars);
			}

			if (head_of_list instanceof LambdaAtom) {
				LambdaAtom head_as_LambdaAtom = (LambdaAtom) head_of_list;
				HashMap<String, Atom> lambda_vars_to_pass_on = new HashMap<String, Atom>();

				int index_on_nodes = 1;
				for (String par : head_as_LambdaAtom.params) {
					lambda_vars_to_pass_on.put(par, run(in_as_ListAtom.nodes.get(index_on_nodes), lambda_vars));
					index_on_nodes++;
				}
				lambda_vars_to_pass_on.put("", head_as_LambdaAtom);
				return run((ListAtom) head_as_LambdaAtom.process, lambda_vars_to_pass_on);
			}
			
			if(head_of_list instanceof DataListAtom) {
				
				DataListAtom head_as_DataListAtom = (DataListAtom) head_of_list;
				
				Atom second_node;
				Atom third_node;
				Atom fourth_node;
				
				switch (in_as_ListAtom.nodes.size()) {
				case 1:
					return head_as_DataListAtom;
				case 2:
					second_node = in_as_ListAtom.nodes.get(1);
					
					if(second_node instanceof KeywordAtom) {
						String keyword_name = ((KeywordAtom) second_node).keyword;
						
						if(keyword_name.equals("size")) return new NumberAtom(head_as_DataListAtom.list.size());
						else throw error_arg(("(List Keyword.!'size')"));
					}else {
						second_node = run(second_node, lambda_vars);
						
						if(!(second_node instanceof NumberAtom)) 
							throw error_arg(("(List ![Keyword.'size' | Integer])"));
						else {
							NumberAtom second_node_As_NumberAtom = (NumberAtom) second_node;
							if(second_node_As_NumberAtom.type != NumberType.INTEGER) 
								throw error_arg(("(List !Integer)"));
							else {
								int index = (int) second_node_As_NumberAtom.rawData;
								int size = head_as_DataListAtom.list.size();
								
								if(index >= size || index < 0) throw error(new Err_Ubounds(size, index));
								else return head_as_DataListAtom.list.get(index); 
							}
						}
						
					}
				case 3:
					second_node = in_as_ListAtom.nodes.get(1);
					third_node = in_as_ListAtom.nodes.get(2);
					
					if(!(second_node instanceof KeywordAtom)) 
						throw error_arg(("(List !Keyword.['append' | 'remove'] ?Integer)"));
					else {
						third_node = run(third_node, lambda_vars);
						List<Atom> the_list = head_as_DataListAtom.list;
						
						switch(((KeywordAtom)second_node).keyword) {
						case "append":
							the_list.add(third_node);
							break;
						case "remove":
							if(!(third_node instanceof NumberAtom)) 
								throw error_arg(("(List Keyword.'remove' !Integer~\"we got a Float\")"));
							else {
								if(((NumberAtom)third_node).type != NumberType.INTEGER) 
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
					
					break;
				case 4:
					second_node = in_as_ListAtom.nodes.get(1);
					third_node = in_as_ListAtom.nodes.get(2);
					fourth_node = in_as_ListAtom.nodes.get(3);
					
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
								if(((NumberAtom)third_node).type != NumberType.INTEGER)  
									throw error_arg(("(List Keyword.!'set' !Integer~\"we got a Float\" ?Atom)"));
								else {
									List<Atom> the_list = head_as_DataListAtom.list;
									
									int index = (int) ((NumberAtom)third_node).rawData;
									int size = the_list.size();
									
									if(index >= size || index < 0) throw error(new Err_Ubounds(size, index));
									else the_list.set(index, fourth_node);
								}
							}
						}
					}
					break;
				default:
					throw error_arg(("(List Atom*![0 | 1 | 2 | 3]"));
				}
			}
			
			
			
			return head_of_list;
		} else {
			ListAtom onwards_list = new ListAtom();
			onwards_list.nodes.add(in_atom);
			return run(onwards_list, lambda_vars);
		}

	}
	public Atom run(Atom in_atom) throws LispRuntimeError {
		return run(in_atom, NULL_LAMBDA_VARS);
	}

	@SuppressWarnings("unused")
	private Atom handleKeyword(ListAtom in_ListAtom) throws LispRuntimeError {
		return handleKeyword(in_ListAtom, NULL_LAMBDA_VARS);
	}
	@SuppressWarnings("unchecked")
	private Atom handleKeyword(ListAtom in_ListAtom, HashMap<String, Atom> lambda_vars) throws LispRuntimeError {
		/*
		 * MASSIVE SWITCH STATEMENT INCOMING
		 * 
		 * Brace! Brace! Brace!
		 * 
		 */
		String head_name = ((KeywordAtom) in_ListAtom.nodes.get(0)).keyword;
		switch (head_name) {
		/*
		 * 
		 * DATA AND FUNCTIONS
		 * 
		 * 
		 */
		case "set":
			if(in_ListAtom.nodes.size() != 3) 
				throw error_arg(("!(set ?Variable ?Atom)"));
			else if (!(in_ListAtom.nodes.get(1) instanceof VariableAtom)) 
				throw error_arg(("(set !Variable ?Atom)"));
			else {
				Atom the_return = run(in_ListAtom.nodes.get(2), lambda_vars);
				variables_map.put(((VariableAtom) in_ListAtom.nodes.get(1)).name,
						the_return);
				return the_return;
			}
		case "lambda":
			if(in_ListAtom.nodes.size() != 3) throw error_arg(("!(lambda ?[(arg1 arg2...) | ()] ?(~expression~))"));
			if(!(in_ListAtom.nodes.get(1) instanceof ListAtom)) throw error_arg(("(lambda ![(arg1 arg2...) | ()] ?(~expression~))"));
			else {
				ListAtom listof_lambda_vars = (ListAtom) in_ListAtom.nodes.get(1);
				ListAtom function = (ListAtom) in_ListAtom.nodes.get(2);
				while (function.nodes.get(0) instanceof ListAtom)
					function = (ListAtom) function.nodes.get(0);
				return new LambdaAtom(listof_lambda_vars, function);
			}
		case "list", "array":
			if(true) {
				DataListAtom the_return = (head_name.equals("list")) ? new DataListAtom(ListType.LinkedList)
																	 : new DataListAtom(ListType.ArrayList);
				for(int index = 1; index < in_ListAtom.nodes.size(); index++) {
					the_return.list.add(run(in_ListAtom.nodes.get(index), lambda_vars));
				}
				return the_return;
			}
		/*
		 * 
		 * STRINGS
		 * 
		 * 
		 */
		case "to-str":
			if (in_ListAtom.nodes.size() != 2)
				throw error_arg(("!(to-str ?[Integer | Float | String])"));
			else {
				Object o = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					return new StringAtom(((NumberAtom)o).convertToString(), 0);
				} else if (o instanceof StringAtom) {
					return (Atom) o;
				}else throw error_arg(("(to-str ![Integer | Float | String])"));
			} 
		case "to-int":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!(to-int ?[Integer | Float | String])"));
			else {
				Object o = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					NumberAtom n = (NumberAtom) o;
					return (n.type == NumberType.INTEGER) ? n
														  : new NumberAtom((long)Double.longBitsToDouble(n.rawData));
				} else if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				} else throw error_arg(("(to-int ![Integer | Float | String])"));
			}
		case "to-float":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!(to-int ?[Integer | Float | String])"));
			else {
				Object o = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					NumberAtom n = (NumberAtom) o;
					return (n.type == NumberType.FLOAT) ? n
													    : new NumberAtom((double)n.rawData);
				} else if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				} else throw error_arg(("(to-float ![Integer | Float | String])"));
			}
		case "input":
			if (in_ListAtom.nodes.size() != 2 &&
				in_ListAtom.nodes.size() != 1) throw error_arg(("!(input ?[String | NumberAtom | Void])"));
			else{
				String prompt = "";
				
				if (in_ListAtom.nodes.size() >= 2) {
					Atom possible_prompt = run(in_ListAtom.nodes.get(1), lambda_vars);
					if (possible_prompt instanceof StringAtom) {
						prompt += ((StringAtom) possible_prompt).value;
					} else if (possible_prompt instanceof NumberAtom) {
						prompt += ((NumberAtom) possible_prompt).getValue();
					} else throw error_arg(("(input ![String | NumberAtom])"));
				}
				
				Scanner scan = new Scanner(System.in);
				System.out.print(prompt);
				String out = scan.nextLine();
				
				return new StringAtom(out, 0);
			}
		case "print":
			if(true) {
				String out = "";
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom text = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (text instanceof StringAtom) {
						out += ((StringAtom) text).value;
					} else if (text instanceof NumberAtom) {
						out += ((NumberAtom) text).getValue();
					} else {
						throw error_arg(("(print ![String | NumberAtom]...)"));
					}
				}
				System.out.println(out);
			} break;
		case "out":
			if(in_ListAtom.nodes.size() > 1) {
				String out = "";
				for (int index = 1; index < in_ListAtom.nodes.size(); index++) {
					out += run(in_ListAtom.nodes.get(index), lambda_vars) + " ";
				}
				System.out.println(out);
			} break;
			
		/*
		 * 
		 * MATHAMATICAL FUNCTIONS
		 * 
		 * 
		 */
		case "+","-","*","/":
			switch(in_ListAtom.nodes.size()) {
			case 1:
				throw error_arg(("!(" + head_name + " ?[NumberAtom]...)"));
			case 2:
				if (in_ListAtom.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) in_ListAtom.nodes.get(1);
				} else throw error_arg(("(" + head_name + " ![NumberAtom]...)"));
			default:
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw error_arg(("(" + head_name + " ![NumberAtom]...)"));
				}
				
				if (nums.size() > 0) 
					return (head_name.equals("+")) ? Operate.sum(nums)  :
						   (head_name.equals("-")) ? Operate.sub(nums)  :
						   (head_name.equals("*")) ? Operate.prod(nums) :
						   (head_name.equals("/")) ? Operate.div(nums)  : null;                       
				else throw error_arg(("(" + head_name + " ![NumberAtom]...)"));
			}
		case "^":
			if (in_ListAtom.nodes.size() != 3) 
				throw error_arg(("!(^ ?[NumberAtom] ?[NumberAtom])"));
			else {
				Atom base = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(base instanceof NumberAtom)) throw error_arg(("(^ ![NumberAtom] ?[NumberAtom])"));
				Atom exponent = run(in_ListAtom.nodes.get(2), lambda_vars);
				if (!(exponent instanceof NumberAtom)) throw error_arg(("(^ [NumberAtom] ![NumberAtom])"));
				
				return Operate.pow((NumberAtom) base, (NumberAtom) exponent);
			}
		case "log":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!(log ?[NumberAtom])"));
			else {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw error_arg(("(log ![NumberAtom])"));
				
				return Operate.log((NumberAtom) value);
			}  
		//
		//iDiv
		//
		case "mod":
			if (in_ListAtom.nodes.size() != 3) 
				throw error_arg(("!(mod ?[NumberAtom] ?[NumberAtom])"));
			else {
				Atom base = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(base instanceof NumberAtom)) throw error_arg(("(mod ![NumberAtom] ?[NumberAtom])"));
				Atom exponent = run(in_ListAtom.nodes.get(2), lambda_vars);
				if (!(exponent instanceof NumberAtom)) throw error_arg(("(mod [NumberAtom] ![NumberAtom])"));
				
				return Operate.mod((NumberAtom) base, (NumberAtom) exponent);
			} 
		case "round", "floor", "ceil":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!("+head_name+" ?[NumberAtom])"));
			else {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw error_arg(("!("+head_name+" ?[NumberAtom])"));
				
				return (head_name.equals("round")) ? Operate.round((NumberAtom) value) :
					   (head_name.equals("floor")) ? Operate.floor((NumberAtom) value) :
					   (head_name.equals("ceil"))  ? Operate.ceil((NumberAtom) value)  : null;
			}
		//
		//TRIG
		//
		case "sin", "cos", "tan", "asin", "acos", "atan":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!("+head_name+" ?[NumberAtom])"));
			else {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw error_arg(("!("+head_name+" ?[NumberAtom])"));
				
				return (head_name.equals("sin"))  ? Operate.sin((NumberAtom) value)   :
					   (head_name.equals("cos"))  ? Operate.cos((NumberAtom) value)   :
					   (head_name.equals("tan"))  ? Operate.tan((NumberAtom) value)   :
					   (head_name.equals("asin")) ? Operate.asin((NumberAtom) value)  :
					   (head_name.equals("acos")) ? Operate.acos((NumberAtom) value)  :
					   (head_name.equals("atan")) ? Operate.atan((NumberAtom) value)  : null;
			}
		/*
		 * 
		 * BOOLEAN LOGIC FUNCTIONS
		 * 
		 * 
		 */
		// COMPARATORS
		case "=", ">", "<", ">=", "<=":
			if (in_ListAtom.nodes.size() < 3) 
				throw error_arg(("!(" + head_name + " ?[NumberAtom] ?[NumberAtom]...)")); 
			else {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw error_arg(("(" + head_name + " ![NumberAtom] ![NumberAtom]... \"1+ of the args is invalid\")")); 
				}
				if (nums.size() <= 0)
					throw error_arg(("(" + head_name + " ![NumberAtom] ![NumberAtom]... \"empty args\")")); 
				else return (head_name.equals("="))  ? Operate.equal(nums)           :
					        (head_name.equals(">"))  ? Operate.greaterThan(nums)     :
					        (head_name.equals("<"))  ? Operate.lessThan(nums)        :
					        (head_name.equals(">=")) ? Operate.greaterThanOrEQ(nums) :
					        (head_name.equals("<=")) ? Operate.lessThanOrEQ(nums)    : null;   
			}
		// BOOL LOGIC
		case "AND", "OR":
			if (in_ListAtom.nodes.size() < 3) 
				throw error_arg(("!(" + head_name + " ?[NumberAtom] ?[NumberAtom]...)")); 
			else {
				boolean is_and = head_name.equals("AND");
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (is_and) {if (!((NumberAtom) internal).isTrue()) return new NumberAtom(0);}
						else        {if (((NumberAtom) internal).isTrue())  return new NumberAtom(1);}
						
					}else throw error_arg(("(" + head_name + " ![NumberAtom] ![NumberAtom]... \"1+ of the args is invalid\")")); 
				}
				return new NumberAtom((is_and) ? 1 : 0);
			}
		case "XOR":
			if (in_ListAtom.nodes.size() < 3) 
				throw error_arg(("!(XOR ?[NumberAtom] ?[NumberAtom]...)")); 
			else {
				int c = 0;
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							c++;
					}else throw error_arg(("(XOR ![NumberAtom] ![NumberAtom]... \"1+ of the args is invalid\")")); 
				}
				return new NumberAtom((c == 1) ? 1 : 0);
			}
		case "NOT":
			if (in_ListAtom.nodes.size() != 2) 
				throw error_arg(("!(NOT ?[NumberAtom])")); 
			else {
				Atom internal = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (internal instanceof NumberAtom) {
					boolean is_true = ((NumberAtom) internal).isTrue();
					return new NumberAtom((is_true) ? 0 : 1);
				} else throw error_arg(("(NOT ![NumberAtom])")); 
			}

		/*
		 * TEST
		 * 
		 */
		case "dummy":
			System.out.println("dummy!!");
			return null;

		/*
		 * 
		 * STRUCTURAL
		 * 
		 * 
		 */
		case "do", "hide", "then":
			if (in_ListAtom.nodes.size() < 2) 
				throw error_arg(("!(" + head_name + " ?(~expression~) ?(~expression~)... )")); 
			else {
				Interpreter sub_interpreter;
				if(head_name.equals("then")) {
					sub_interpreter = this;
				} else {
					sub_interpreter = new Interpreter();
					if(head_name.equals("hide")) sub_interpreter.variables_map = (HashMap<String, Atom>) this.variables_map.clone();
				}
				
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					if (!(in_ListAtom.nodes.get(i) instanceof ListAtom)) 
						throw error_arg(("(" + head_name + " !(~expression~) !(~expression~)... \"1+ of the args is invalid\")")); 
					{
						ListAtom statement_we_on = (ListAtom) in_ListAtom.nodes.get(i);
						if (statement_we_on.nodes.size() != 0) 
						if (statement_we_on.nodes.get(0) instanceof KeywordAtom) {
							KeywordAtom k = (KeywordAtom) statement_we_on.nodes.get(0);
							if (k.keyword.equals("return")) {
								if (statement_we_on.nodes.size() > 1)
									return sub_interpreter.run(statement_we_on.nodes.get(1), lambda_vars);
								else
									return null;
							}else {
								Atom internal = sub_interpreter.run(statement_we_on, lambda_vars);
							}
						} else {
							Atom internal = sub_interpreter.run(statement_we_on, lambda_vars);
						}
					}
				}
			}
		case "if":
			for (int i = 0; i < in_ListAtom.nodes.size(); i += 3) {
				try {
				if (!(in_ListAtom.nodes.get(i) instanceof KeywordAtom)) 
					throw error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
									+ " !elif ?NumberAtom ?(~expression~)\n"
									+ " !else ?NumberAtom\n"
									+ " \"1+ of the keys is invalid\")"));
				else {
					KeywordAtom keyword_atom = (KeywordAtom) in_ListAtom.nodes.get(i);
					switch(keyword_atom.keyword) {
					case "if", "elif":
						if ((i == 0 && keyword_atom.keyword.length() == 2) || 
						    (i != 0 && keyword_atom.keyword.length() == 4)) 
						{
							Atom condition_atom = run(in_ListAtom.nodes.get(i + 1), lambda_vars);
							if (!(condition_atom instanceof NumberAtom)) 
								throw error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
												+ " !elif ?NumberAtom ?(~expression~)\n"
												+ " !else ?NumberAtom\n"
												+ " \"1+ of the conditions is invalid\")"));
							else {
								if (((NumberAtom) condition_atom).isTrue())
									return run(in_ListAtom.nodes.get(i + 2), lambda_vars);
							}
						} else throw error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
												+ " !elif ?NumberAtom ?(~expression~)\n"
												+ " !else ?NumberAtom\n"
													+ " \"1+ of the keys is invalid\")"));
						break;
					case "else":
						return run(in_ListAtom.nodes.get(i + 1), lambda_vars);
					default:
						throw error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
										+ " !elif ?NumberAtom ?(~expression~)\n"
										+ " !else ?NumberAtom\n"
										+ " \"1+ of the keys is invalid\")"));
					}
				}
				} catch (IndexOutOfBoundsException e) {
					throw error_arg(("!(if    ?NumberAtom ?(~expression~)\n"
									+ "  ?elif ?NumberAtom ?(~expression~)\n"
									+ "  ?else ?NumberAtom\n"
									+ " )"));
				}
			} return null;
		case "while":
			if (in_ListAtom.nodes.size() != 3) 
				throw error_arg(("!(while ?NumberAtom ?(~expression~))"));
			else {
				Atom condition_atom;
				boolean condition_state = true;
				Atom return_atom = null;
				while (condition_state) {
					condition_atom = run(in_ListAtom.nodes.get(1), lambda_vars);
					if (condition_atom instanceof NumberAtom) {
						condition_state = ((NumberAtom) condition_atom).isTrue();
					} else
						condition_state = false;
					if (condition_state)
						return_atom = run(in_ListAtom.nodes.get(2), lambda_vars);
				}
				return return_atom;
			}
		}
		return null;
	}

	
	/*
	 * 
	 * UTILITIES
	 * 
	 */
	private static LispRuntimeError error(LispError error){
		return new LispRuntimeError(error);
	}
	
	private static LispRuntimeError error() {
		return new LispRuntimeError();
	}
	
	private static LispRuntimeError error_arg(String e) {
		return new LispRuntimeError(new Err_IncorrectArgs(e));
	}

}

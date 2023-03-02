package skinnylisp.std_lib;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import skinnylisp.ast.atoms.Atom;
import skinnylisp.ast.atoms.KeywordAtom;
import skinnylisp.ast.atoms.LambdaVariableAtom;
import skinnylisp.ast.atoms.NumberAtom;
import skinnylisp.ast.atoms.StatementAtom;
import skinnylisp.ast.atoms.StringAtom;
import skinnylisp.ast.atoms.VariableAtom;
import skinnylisp.engine.runtimeatoms.FuncAtom;
import skinnylisp.engine.runtimeatoms.ListAtom;
import skinnylisp.engine.runtimeatoms.ListAtom.ListType;
import skinnylisp.engine.Interpreter;
import skinnylisp.engine.LispType;
import skinnylisp.engine.numbercrunch.Operate;

public class Std_lib extends Packet{
	public Std_lib() {
		super();
		
		add("set", (statement, lambda_vars, me, head_name) -> {
			if(statement.nodes.size() != 3) 
				throw Interpreter.error_arg(("!(set ?Variable ?Atom)"));
			else {
				Atom the_var = statement.nodes.get(1);
					
				if (!(the_var instanceof VariableAtom)) 
					throw Interpreter.error_arg(("(set !Variable ?Atom)"));
				else {
					Atom the_return = me.run(statement.nodes.get(2), lambda_vars);
					me.variables_map.put(((VariableAtom) the_var).name,
							the_return);
					if(the_return instanceof FuncAtom) ((FuncAtom) the_return).name = ((VariableAtom) statement.nodes.get(1)).name;
					return the_return;
				}
			}
		});
		
		add("lambda", (statement, lambda_vars, me, head_name) -> {
			if(statement.nodes.size() != 3) throw Interpreter.error_arg(("!(lambda ?[(arg1 arg2...) | ()] ?(~expression~))"));
			if(!(statement.nodes.get(1) instanceof StatementAtom)) throw Interpreter.error_arg(("(lambda ![(arg1 arg2...) | ()] ?(~expression~))"));
			else {
				Atom body = statement.nodes.get(2);
				if(body instanceof LambdaVariableAtom ) body = lambda_vars.get(((LambdaVariableAtom)body).name);
				StatementAtom listof_lambda_vars = (StatementAtom) statement.nodes.get(1);
				StatementAtom function = (StatementAtom) body;
				while (function.nodes.get(0) instanceof StatementAtom)
					function = (StatementAtom) function.nodes.get(0);
				return new FuncAtom(listof_lambda_vars, function);
			}
		});
		
		add(new String[]{"list", "array"}, (statement, lambda_vars, me, head_name) -> {
			ListAtom the_return = (head_name.equals("list")) ? new ListAtom(ListType.LinkedList)
															 : new ListAtom(ListType.ArrayList);
			for(int index = 1; index < statement.nodes.size(); index++) {
				the_return.list.add(me.run(statement.nodes.get(index), lambda_vars));
			}
			return the_return;
		});
		
		add("is-null", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2)
				throw Interpreter.error_arg(("!(is-null ?[Any])"));
			else {
				Object check = me.run(statement.nodes.get(1), lambda_vars);
				return (check == null) ? new NumberAtom(1)
									   : new NumberAtom(0);
			}
		});
		
		add("is-type", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() < 3)
				throw Interpreter.error_arg(("!(is-type ?[Any] ?[Type as Keyword]...)"));
			else {
				Object check = me.run(statement.nodes.get(1), lambda_vars);
				List<LispType> types = new LinkedList<LispType>();
				for(int n = 2; n<statement.nodes.size(); n++) {
					if(!(statement.nodes.get(n) instanceof KeywordAtom)) throw me.error_arg("(is-type [Any] ![Type as Keyword]...)");
					String name = ((KeywordAtom) statement.nodes.get(n)).keyword;
					boolean success = false;
					for(LispType r : LispType.values()) {
						if(name.equals(r.name)) {
							types.add(r);
							success = true;
							break;
						}
					}
					if(!success) throw Interpreter.error_arg("is-type: " +name + " is not a valid type");
				}
				
				for(LispType t : types) {
					if(t.atom_class.isInstance(check)) {
						if(t.atom_class == NumberAtom.class) {
							switch(t) {
							case Integer:
								if(((NumberAtom) check).type == NumberAtom.Type.INTEGER) return new NumberAtom(1);
								break;
							case Float:
								if(((NumberAtom) check).type == NumberAtom.Type.FLOAT) return new NumberAtom(1);
								break;
							default:
								return new NumberAtom(1);
							}
						}else {
							return new NumberAtom(1);
						}
					}
				} return new NumberAtom(0);
			} 
		});
		
		add("to-str", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2)
				throw Interpreter.error_arg(("!(to-str ?[Integer | Float | String])"));
			else {
				Object o = me.run(statement.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					return new StringAtom(((NumberAtom)o).convertToString(), 0);
				} else if (o instanceof StringAtom) {
					return (Atom) o;
				}else throw Interpreter.error_arg(("(to-str ![Integer | Float | String])"));
			} 
		});
		
		add("to-int", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!(to-int ?[Integer | Float | String])"));
			else {
				Object o = me.run(statement.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					NumberAtom n = (NumberAtom) o;
					return (n.type == NumberAtom.Type.INTEGER) ? n
														       : new NumberAtom((long)Double.longBitsToDouble(n.rawData));
				} else if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				} else throw Interpreter.error_arg(("(to-int ![Integer | Float | String])"));
			}
		});
		
		add("to-float", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!(to-int ?[Integer | Float | String])"));
			else {
				Object o = me.run(statement.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					NumberAtom n = (NumberAtom) o;
					return (n.type == NumberAtom.Type.FLOAT) ? n
													         : new NumberAtom((double)n.rawData);
				} else if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				} else throw Interpreter.error_arg(("(to-float ![Integer | Float | String])"));
			}
		});
		
		add("input", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2 &&
					statement.nodes.size() != 1) throw Interpreter.error_arg(("!(input ?[String | NumberAtom | Void])"));
				else{
					String prompt = "";
					
					if (statement.nodes.size() >= 2) {
						Atom possible_prompt = me.run(statement.nodes.get(1), lambda_vars);
						if (possible_prompt instanceof StringAtom) {
							prompt += ((StringAtom) possible_prompt).value;
						} else if (possible_prompt instanceof NumberAtom) {
							prompt += ((NumberAtom) possible_prompt).getValue();
						} else throw Interpreter.error_arg(("(input ![String | NumberAtom])"));
					}
					
					Scanner scan = new Scanner(me.inputstream);
					me.printstream.print(prompt);
					String out = scan.nextLine();
					scan.close();
					return new StringAtom(out, 0);
				}
		});
		
		add("print", (statement, lambda_vars, me, head_name) -> {
			String out = "";
			for (int i = 1; i < statement.nodes.size(); i++) {
				Atom text = me.run(statement.nodes.get(i), lambda_vars);
				if (text instanceof StringAtom) {
					out += ((StringAtom) text).value;
				} else if (text instanceof NumberAtom) {
					out += ((NumberAtom) text).getValue();
				} else {
					throw Interpreter.error_arg(("(print ![String | NumberAtom]...)"));
				}
			}
			me.printstream.println(out);
			return null;
		});
		
		add("out", (statement, lambda_vars, me, head_name) -> {
			if(statement.nodes.size() > 1) {
				String out = "";
				for (int index = 1; index < statement.nodes.size(); index++) {
					out += me.run(statement.nodes.get(index), lambda_vars) + " ";
				}
				me.printstream.println(out);
			}
			return null;
		});
		
		add("-", (statement, lambda_vars, me, head_name) -> {
			if(statement.nodes.size() < 2) 
				throw Interpreter.error_arg(("!(- ?[NumberAtom]...)"));
			{
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				nums.add(new NumberAtom(0));
				for (int i = 1; i < statement.nodes.size(); i++) {
					Atom internal = me.run(statement.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw Interpreter.error_arg(("(" + head_name + " ![NumberAtom]...)"));
				}
				return Operate.sub(nums);   
			}
		});
		
		add("pow", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 3) 
				throw Interpreter.error_arg(("!(^ ?[NumberAtom] ?[NumberAtom])"));
			else {
				Atom base = me.run(statement.nodes.get(1), lambda_vars);
				if (!(base instanceof NumberAtom)) throw Interpreter.error_arg(("(^ ![NumberAtom] ?[NumberAtom])"));
				Atom exponent = me.run(statement.nodes.get(2), lambda_vars);
				if (!(exponent instanceof NumberAtom)) throw Interpreter.error_arg(("(^ [NumberAtom] ![NumberAtom])"));
				
				return Operate.pow((NumberAtom) base, (NumberAtom) exponent);
			}
		});
		
		add("log", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!(log ?[NumberAtom])"));
			else {
				Atom value = me.run(statement.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw Interpreter.error_arg(("(log ![NumberAtom])"));
				
				return Operate.log((NumberAtom) value);
			}  
		});
		
		add(new String[] {"round", "floor", "ceil"}, (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!("+head_name+" ?[NumberAtom])"));
			else {
				Atom value = me.run(statement.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw Interpreter.error_arg(("!("+head_name+" ?[NumberAtom])"));
				
				return (head_name.equals("round")) ? Operate.round((NumberAtom) value) :
					   (head_name.equals("floor")) ? Operate.floor((NumberAtom) value) :
					   (head_name.equals("ceil"))  ? Operate.ceil((NumberAtom) value)  : null;
			} 
		});
		
		add(new String[] {"sin", "cos", "tan", "asin", "acos", "atan"}, (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!("+head_name+" ?[NumberAtom])"));
			else {
				Atom value = me.run(statement.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw Interpreter.error_arg(("!("+head_name+" ?[NumberAtom])"));
				
				return (head_name.equals("sin"))  ? Operate.sin((NumberAtom) value)   :
					   (head_name.equals("cos"))  ? Operate.cos((NumberAtom) value)   :
					   (head_name.equals("tan"))  ? Operate.tan((NumberAtom) value)   :
					   (head_name.equals("asin")) ? Operate.asin((NumberAtom) value)  :
					   (head_name.equals("acos")) ? Operate.acos((NumberAtom) value)  :
					   (head_name.equals("atan")) ? Operate.atan((NumberAtom) value)  : null;
			}
		});
		
		add(new String[] {"AND", "OR"}, (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() < 3) 
				throw Interpreter.error_arg(("!(" + head_name + " ?[NumberAtom] ?[NumberAtom]...)")); 
			else {
				boolean is_and = head_name.equals("AND");
				for (int i = 1; i < statement.nodes.size(); i++) {
					Atom internal = me.run(statement.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (is_and) {if (!((NumberAtom) internal).isTrue()) return new NumberAtom(0);}
						else        {if (((NumberAtom) internal).isTrue())  return new NumberAtom(1);}
						
					}else throw Interpreter.error_arg(("(" + head_name + " ![NumberAtom] ![NumberAtom]... \"1+ of the args is invalid\")")); 
				}
				return new NumberAtom((is_and) ? 1 : 0);
			}
		});
		
		add("XOR", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() < 3) 
				throw Interpreter.error_arg(("!(XOR ?[NumberAtom] ?[NumberAtom]...)")); 
			else {
				int c = 0;
				for (int i = 1; i < statement.nodes.size(); i++) {
					Atom internal = me.run(statement.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							c++;
					}else throw Interpreter.error_arg(("(XOR ![NumberAtom] ![NumberAtom]... \"1+ of the args is invalid\")")); 
				}
				return new NumberAtom((c == 1) ? 1 : 0);
			}
		});
		
		add("NOT", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 2) 
				throw Interpreter.error_arg(("!(NOT ?[NumberAtom])")); 
			else {
				Atom internal = me.run(statement.nodes.get(1), lambda_vars);
				if (internal instanceof NumberAtom) {
					boolean is_true = ((NumberAtom) internal).isTrue();
					return new NumberAtom((is_true) ? 0 : 1);
				} else throw Interpreter.error_arg(("(NOT ![NumberAtom])")); 
			}
		});
		
		add("dummy", (statement, lambda_vars, me, head_name) -> {
			me.printstream.println("dummy!!");
			return null;
		});
		
		add(new String[] {"do", "hide", "then"}, (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() < 2) 
				throw Interpreter.error_arg(("!(" + head_name + " ?(~expression~) ?(~expression~)... )")); 
			else {
				Interpreter sub_interpreter;
				if(head_name.equals("then")) {
					sub_interpreter = me;
				} else {
					sub_interpreter = new Interpreter();
					if(head_name.equals("hide")) sub_interpreter.variables_map = (HashMap<String, Atom>) me.variables_map.clone();
				}
				
				for (int i = 1; i < statement.nodes.size(); i++) {
					if (!(statement.nodes.get(i) instanceof StatementAtom)) 
						throw Interpreter.error_arg(("(" + head_name + " !(~expression~) !(~expression~)... \"1+ of the args is invalid\")")); 
					{
						StatementAtom statement_we_on = (StatementAtom) statement.nodes.get(i);
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
			return null;
		});
		
		add("if", (statement, lambda_vars, me, head_name) -> {
			for (int i = 0; i < statement.nodes.size(); i += 3) {
				try {
				if (!(statement.nodes.get(i) instanceof KeywordAtom)) 
					throw Interpreter.error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
									+ " !elif ?NumberAtom ?(~expression~)\n"
									+ " !else ?NumberAtom\n"
									+ " \"1+ of the keys is invalid\")"));
				else {
					KeywordAtom keyword_atom = (KeywordAtom) statement.nodes.get(i);
					switch(keyword_atom.keyword) {
					case "if", "elif":
						if ((i == 0 && keyword_atom.keyword.length() == 2) || 
						    (i != 0 && keyword_atom.keyword.length() == 4)) 
						{
							Atom condition_atom = me.run(statement.nodes.get(i + 1), lambda_vars);
							if (!(condition_atom instanceof NumberAtom)) 
								throw Interpreter.error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
												+ " !elif ?NumberAtom ?(~expression~)\n"
												+ " !else ?NumberAtom\n"
												+ " \"1+ of the conditions is invalid\")"));
							else {
								if (((NumberAtom) condition_atom).isTrue())
									return me.run(statement.nodes.get(i + 2), lambda_vars);
							}
						} else throw Interpreter.error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
												+ " !elif ?NumberAtom ?(~expression~)\n"
												+ " !else ?NumberAtom\n"
													+ " \"1+ of the keys is invalid\")"));
						break;
					case "else":
						return me.run(statement.nodes.get(i + 1), lambda_vars);
					default:
						throw Interpreter.error_arg(("(!if   ?NumberAtom ?(~expression~)\n"
										+ " !elif ?NumberAtom ?(~expression~)\n"
										+ " !else ?NumberAtom\n"
										+ " \"1+ of the keys is invalid\")"));
					}
				}
				} catch (IndexOutOfBoundsException e) {
					throw Interpreter.error_arg(("!(if    ?NumberAtom ?(~expression~)\n"
									+ "  ?elif ?NumberAtom ?(~expression~)\n"
									+ "  ?else ?NumberAtom\n"
									+ " )"));
				}
			} return null;
		});
		
		
		add("while", (statement, lambda_vars, me, head_name) -> {
			if (statement.nodes.size() != 3) 
				throw Interpreter.error_arg(("!(while ?NumberAtom ?(~expression~))"));
			else {
				Atom condition_atom;
				boolean condition_state = true;
				Atom return_atom = null;
				while (condition_state) {
					condition_atom = me.run(statement.nodes.get(1), lambda_vars);
					if (condition_atom instanceof NumberAtom) {
						condition_state = ((NumberAtom) condition_atom).isTrue();
					} else
						condition_state = false;
					if (condition_state)
						return_atom = me.run(statement.nodes.get(2), lambda_vars);
				}
				return return_atom;
			}
		});
	}
}

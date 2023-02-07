package skinnylisp.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import data.Pair;
import skinnylisp.OutC;
import skinnylisp.engine.numbercrunch.Operate;
import skinnylisp.engine.structs.FieldType;
import skinnylisp.engine.structs.StructureAtom;
import skinnylisp.engine.structs.StructureField;
import skinnylisp.exceptions.StructureFieldInvalidEx;
import skinnylisp.exceptions.StructureParameterUnavailiable;
import skinnylisp.lexer.atoms.Atom;
import skinnylisp.lexer.atoms.ListAtom;
import skinnylisp.lexer.atoms.TokenAtom;
import skinnylisp.parser.atoms.KeywordAtom;
import skinnylisp.parser.atoms.LambdaVariableAtom;
import skinnylisp.parser.atoms.NumberAtom;
import skinnylisp.parser.atoms.StringAtom;
import skinnylisp.parser.atoms.VariableAtom;
import skinnylisp.precompiler.Precompiler;

public class Interpreter {

	public HashMap<String, Atom> vars;

	public Interpreter() {
		vars = new HashMap<String, Atom>();
	}

	public Atom run(Atom n, HashMap<String, Atom> INlVars) throws Exception {
		// OutC.debug(n);
		if (n instanceof ListAtom) {
			ListAtom l = (ListAtom) n;
			if (l.nodes.size() == 0)
				return null;

			Atom key = l.nodes.get(0);
			if (key instanceof LambdaVariableAtom) {
				key = INlVars.get(((LambdaVariableAtom) key).name);
			}
			if (key instanceof VariableAtom) {
				if (!vars.containsKey(((VariableAtom) key).name)) {
					System.err.println("YIKES : ");
					System.err.println(vars);
				}
				key = vars.get(((VariableAtom) key).name);
			}

			if (key instanceof ListAtom) {
				key = run(key, INlVars);
			}

			if (key instanceof KeywordAtom) {
				return handleKeyword(l, INlVars);
			}

			if (key instanceof LambdaAtom) {
				LambdaAtom lA = (LambdaAtom) key;
				HashMap<String, Atom> lVars = new HashMap<String, Atom>(); // ((lambda (/n) (add 5 /n)) 4) <- Carry on
																			// from this
				int index = 1;
				for (String par : lA.params) {
					lVars.put(par, run(l.nodes.get(index), INlVars));
					index++;
				}
				lVars.put("", lA);
				return run((ListAtom) lA.process, lVars);
			}
			if(key instanceof StructureAtom) {
				StructureAtom the_struct = (StructureAtom) key;
				if(l.nodes.size() == 1) return the_struct;
				Atom ret = null;
				for(int i = 1; i<l.nodes.size(); i++) {
					Atom internal_token = l.nodes.get(i);
					String name;
					if(internal_token instanceof VariableAtom) {
						VariableAtom as_VariableAtom = (VariableAtom) internal_token;
						name = as_VariableAtom.name;
					} else if(internal_token instanceof KeywordAtom) {
						KeywordAtom as_KeywordAtom = (KeywordAtom) internal_token;
						name = as_KeywordAtom.keyword;
					}else {
						throw new StructureParameterUnavailiable();
					}
					
					StructureField field = null;
					for(StructureField field_being_checked : the_struct.fields) {
						if(
						  field_being_checked.field_name.equals(name) &&
						  (field_being_checked.field_type == FieldType.Immutable_Global || field_being_checked.field_type == FieldType.Mutable_Global)
						) {
							field = field_being_checked;
							break;
						}else {
							//OutC.debug(field_being_checked.field_name);
						}
					}
					
					if(field == null) {
						throw new StructureParameterUnavailiable();
					}
					ret = field.field_value;
					if(ret instanceof StructureAtom) {
						the_struct = (StructureAtom) ret;
					}
				}
				return ret;
			}
			return key;
		} else {
			ListAtom l = new ListAtom();
			l.nodes.add(n);
			return run(l, INlVars);
		}

	}

	public Atom run(Atom n) throws Exception {
		return run(n, lVarsNull);
	}

	private final static HashMap<String, Atom> lVarsNull = new HashMap<String, Atom>();

	private Atom handleKeyword(ListAtom n) throws Exception {
		return handleKeyword(n, lVarsNull);
	}

	private Atom handleKeyword(ListAtom n, HashMap<String, Atom> lVars) throws Exception {
		switch (((KeywordAtom) n.nodes.get(0)).keyword) {
		case "set":
			if (n.nodes.get(1) instanceof ListAtom) {
				StructureField ret = null;
				ListAtom internal = (ListAtom) n.nodes.get(1);
				Atom key = run(internal.nodes.get(0), lVars);
				//OutC.debug(key);
				if(key instanceof StructureAtom) {
					StructureAtom the_struct = (StructureAtom) key;
					if(internal.nodes.size() == 1) return the_struct;
					for(int i = 1; i<internal.nodes.size(); i++) {
						Atom internal_token = internal.nodes.get(i);
						String name;
						if(internal_token instanceof VariableAtom) {
							VariableAtom as_VariableAtom = (VariableAtom) internal_token;
							name = as_VariableAtom.name;
						} else if(internal_token instanceof KeywordAtom) {
							KeywordAtom as_KeywordAtom = (KeywordAtom) internal_token;
							name = as_KeywordAtom.keyword;
						}else {
							throw new StructureParameterUnavailiable();
						}
						
						StructureField field = null;
						for(StructureField field_being_checked : the_struct.fields) {
							if(
							  field_being_checked.field_name.equals(name) &&
							  (field_being_checked.field_type == FieldType.Immutable_Global || field_being_checked.field_type == FieldType.Mutable_Global)
							) {
								
								field = field_being_checked;
								break;
							}
						}
						if(field == null) {
							throw new StructureParameterUnavailiable();
						}
						ret = field;
						if(ret.field_value instanceof StructureAtom) {
							the_struct = (StructureAtom) ret.field_value;
						}
					}
					if(ret.field_type != FieldType.Mutable_Global) throw new StructureParameterUnavailiable();
					ret.field_value = run(n.nodes.get(2), lVars);
				} else throw new StructureParameterUnavailiable();
			}else if (n.nodes.get(1) instanceof VariableAtom) {
				vars.put(((VariableAtom) n.nodes.get(1)).name, run(n.nodes.get(2), lVars));
			}
			break;
		case "lambda":
			if (n.nodes.get(1) instanceof ListAtom) {
				ListAtom l = (ListAtom) n.nodes.get(1);
				ListAtom f = (ListAtom) n.nodes.get(2);
				while (f.nodes.get(0) instanceof ListAtom)
					f = (ListAtom) f.nodes.get(0);
				return new LambdaAtom(l, f);
			}
			break;
		case "structure":
			if (n.nodes.size() != 2) {
				System.out.println("EMPTY STRUCT");
				return null;
			}
			if (n.nodes.get(1) instanceof ListAtom) {
				List<StructureField> elements_on_struct = new LinkedList<StructureField>();
				ListAtom struct_body = (ListAtom) n.nodes.get(1);
				for (Atom element : struct_body.nodes) {
					if (element instanceof ListAtom) {
						ListAtom element_as_ListAtom = (ListAtom) element;
						StructureField to_add = new StructureField(element_as_ListAtom);
						to_add.field_value = run(to_add.field_value, lVars);
						if(element_as_ListAtom.nodes.size() > 0) elements_on_struct.add(to_add);
					}else {
						throw new StructureFieldInvalidEx();
					}
				}
				return new StructureAtom(elements_on_struct);
			}
			break;
		/*
		 * 
		 * STRINGS
		 * 
		 * 
		 */
		case "parse-int":
			if (n.nodes.size() == 2) {
				Object o = run(n.nodes.get(1), lVars);
				if (o instanceof NumberAtom) {
					return (Atom) o;
				}
				if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				}
			}
			break;
		case "input":
			String _out2 = "";
			if (n.nodes.size() >= 2) {
				Object o = run(n.nodes.get(1), lVars);
				if (o instanceof StringAtom) {
					_out2 += ((StringAtom) o).value;
				}
				if (o instanceof NumberAtom) {
					_out2 += ((NumberAtom) o).getValue();
				}
			}
			Scanner scan = new Scanner(System.in);
			System.out.print(_out2);
			return new StringAtom(Precompiler.precomp(scan.nextLine()), 0);
		case "print":
			String _out1 = "";
			for (int i = 1; i < n.nodes.size(); i++) {
				Object o = run(n.nodes.get(i), lVars);
				if (o instanceof StringAtom) {
					_out1 += ((StringAtom) o).value;
				}
				if (o instanceof NumberAtom) {
					_out1 += ((NumberAtom) o).getValue();
				}
			}
			System.out.println(_out1);
			return null;
		case "out":
			String _out3 = "";
			for (int i = 1; i < n.nodes.size(); i++) {
				_out3 += run(n.nodes.get(i), lVars) + " ";
			}
			System.out.println(_out3);
			return null;
		/*
		 * 
		 * MATHAMATICAL FUNCTIONS
		 * 
		 * 
		 */
		case "+":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.sum(nums);
				else
					break;
			} else if (n.nodes.size() == 2) {
				if (n.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) n.nodes.get(1);
				}
			}
			break;
		case "-":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.sub(nums);
				else
					break;
			} else if (n.nodes.size() == 2) {
				if (n.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) n.nodes.get(1);
				}
			}
			break;
		case "*":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.prod(nums);
				else
					break;
			} else if (n.nodes.size() == 2) {
				if (n.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) n.nodes.get(1);
				}
			}
			break;
		case "/":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.div(nums);
				else
					break;
			} else if (n.nodes.size() == 2) {
				if (n.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) n.nodes.get(1);
				}
			}
			break;

		/*
		 * 
		 * BOOLEAN LOGIC FUNCTIONS
		 * 
		 * 
		 */
		// COMPARATORS
		case "=":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.equal(nums);
				else
					break;
			}
			break;
		case ">":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.greaterThan(nums);
				else
					break;
			}
			break;
		case "<":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.lessThan(nums);
				else
					break;
			}
			break;
		case ">=":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.greaterThanOrEQ(nums);
				else
					break;
			}
			break;
		case "<=":
			if (n.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					}
				}
				if (nums.size() > 0)
					return Operate.lessThanOrEQ(nums);
				else
					break;
			}
			break;
		// BOOL LOGIC
		case "AND":
			if (n.nodes.size() >= 3) {
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						if (!((NumberAtom) internal).isTrue())
							return new NumberAtom(0);
					}

				}
				return new NumberAtom(1);
			}
			break;
		case "OR":
			if (n.nodes.size() >= 3) {
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							return new NumberAtom(1);
					}

				}
				return new NumberAtom(0);
			}
			break;
		case "XOR":
			if (n.nodes.size() >= 3) {
				int c = 0;
				for (int i = 1; i < n.nodes.size(); i++) {
					Atom internal = run(n.nodes.get(i), lVars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							c++;
					}
				}
				return new NumberAtom((c == 1) ? 1 : 0);
			}
			break;
		case "NOT":
			if (n.nodes.size() == 2) {
				Atom internal2 = run(n.nodes.get(1), lVars);
				if (internal2 instanceof NumberAtom) {
					if (((NumberAtom) internal2).isTrue())
						return new NumberAtom(0);
					else
						return new NumberAtom(1);
				} else
					break;
			}
			break;

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
		case "do":
			if (n.nodes.size() >= 2) {
				Interpreter inter = new Interpreter();
				for (int i = 1; i < n.nodes.size(); i++) {
					if (n.nodes.get(i) instanceof ListAtom) {
						ListAtom l = (ListAtom) n.nodes.get(i);
						if (l.nodes.size() == 0) {
							return null;
						}
						if (l.nodes.get(0) instanceof KeywordAtom) {
							KeywordAtom k = (KeywordAtom) l.nodes.get(0);
							if (k.keyword.equals("return")) {
								if (l.nodes.size() > 1)
									return inter.run(l.nodes.get(1), lVars);
								else
									return null;
							}
						}
						Atom internal = inter.run(l, lVars);
					}
				}
			}
			break;
		case "then":
			for (int i = 1; i < n.nodes.size(); i++) {
				if (n.nodes.get(i) instanceof ListAtom) {
					ListAtom l = (ListAtom) n.nodes.get(i);
					if (l.nodes.size() == 0) {
						return null;
					}
					if (l.nodes.get(0) instanceof KeywordAtom) {
						KeywordAtom k = (KeywordAtom) l.nodes.get(0);
						if (k.keyword.equals("return")) {
							if (l.nodes.size() > 1)
								return run(l.nodes.get(1), lVars);
							else
								return null;
						}
					}
					Atom internal = run(l, lVars);
				}
			}
			break;
		case "if":
			for (int i = 0; i < n.nodes.size(); i += 3) {
				if (n.nodes.get(i) instanceof KeywordAtom) {
					KeywordAtom k = (KeywordAtom) n.nodes.get(i);
					if ((i == 0 && k.keyword.equals("if")) || (i != 0 && k.keyword.equals("elif"))) {
						Atom a = run(n.nodes.get(i + 1), lVars);
						if (a instanceof NumberAtom) {
							if (((NumberAtom) a).isTrue())
								return run(n.nodes.get(i + 2), lVars);
						}
					} else if (k.keyword.equals("else")) {
						return run(n.nodes.get(i + 1), lVars);
					}
				}
			}
			return null;
		case "while":
			if (n.nodes.size() == 3) {
				Atom condition;
				boolean a = true;
				Atom ret = null;
				while (a) {
					condition = run(n.nodes.get(1), lVars);
					if (condition instanceof NumberAtom) {
						a = ((NumberAtom) condition).isTrue();
					} else
						a = false;
					if(a) ret = run(n.nodes.get(2), lVars);
				}
				return ret;
			}
			break;

		}
		return null;
	}
}

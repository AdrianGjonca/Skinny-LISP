package skinnylisp.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import data.Pair;
import skinnylisp.OutC;
import skinnylisp.engine.lists.DataListAtom;
import skinnylisp.engine.lists.DataListQuestionError;
import skinnylisp.engine.lists.ListType;
import skinnylisp.engine.numbercrunch.Operate;
import skinnylisp.engine.structs.FieldType;
import skinnylisp.engine.structs.StructureAtom;
import skinnylisp.engine.structs.StructureField;
import skinnylisp.exceptions.ParametersIncorectEx;
import skinnylisp.exceptions.StructureFieldInvalidEx;
import skinnylisp.exceptions.structure_exceptions.SRE_FieldIsNotAccessibleInThisContext;
import skinnylisp.exceptions.structure_exceptions.SRE_FieldNonExistent;
import skinnylisp.exceptions.structure_exceptions.SRE_TokenTypeNotValid;
import skinnylisp.exceptions.structure_exceptions.StructureReferenceError;
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

	public HashMap<String, Atom> variables_map;

	public Interpreter() {
		variables_map = new HashMap<String, Atom>();
	}

	public Atom run(Atom in_atom, HashMap<String, Atom> lambda_vars) throws Exception {
		if (in_atom instanceof ListAtom) {
			ListAtom in_as_ListAtom = (ListAtom) in_atom;
			if (in_as_ListAtom.nodes.size() == 0)
				return null;

			Atom head_of_list = in_as_ListAtom.nodes.get(0);
			if (head_of_list instanceof LambdaVariableAtom) {
				head_of_list = lambda_vars.get(((LambdaVariableAtom) head_of_list).name);
			}
			if (head_of_list instanceof VariableAtom) {
				if (!variables_map.containsKey(((VariableAtom) head_of_list).name)) {
					System.err.println("YIKES : ");
					System.err.println(variables_map);
				}
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
			if (head_of_list instanceof StructureAtom) {
				StructureAtom head_as_StructureAtom = (StructureAtom) head_of_list;
				if (in_as_ListAtom.nodes.size() == 1)
					return head_as_StructureAtom;
				Atom return_value = null;

				for (int i = 1; i < in_as_ListAtom.nodes.size(); i++) {

					Atom internal_token = in_as_ListAtom.nodes.get(i);
					String name;
					if (internal_token instanceof VariableAtom) {
						VariableAtom as_VariableAtom = (VariableAtom) internal_token;
						name = as_VariableAtom.name;
					} else if (internal_token instanceof KeywordAtom) {
						KeywordAtom as_KeywordAtom = (KeywordAtom) internal_token;
						name = as_KeywordAtom.keyword;
					} else {
						throw new StructureReferenceError();
					}

					StructureField field = null;
					for (StructureField field_being_checked : head_as_StructureAtom.fields) {
						if (field_being_checked.field_name.equals(name)
								&& (field_being_checked.field_type == FieldType.Immutable
										|| field_being_checked.field_type == FieldType.Mutable)) {
							field = field_being_checked;
							break;
						} else {

						}
					}

					if (field == null) {
						throw new StructureReferenceError();
					}
					return_value = field.field_value;
					if (return_value instanceof StructureAtom) {
						head_as_StructureAtom = (StructureAtom) return_value;
					}
				}
				return return_value;
			}
			if(head_of_list instanceof DataListAtom) {
				DataListAtom head_as_DataListAtom = (DataListAtom) head_of_list;
				if(in_as_ListAtom.nodes.size() == 1)
					return head_as_DataListAtom;
				if(in_as_ListAtom.nodes.size() == 2) {
					Atom second_node = in_as_ListAtom.nodes.get(1);
					if(second_node instanceof KeywordAtom) {
						KeywordAtom second_node_as_KeywordAtom = (KeywordAtom) second_node;
						if(second_node_as_KeywordAtom.keyword.equals("size")) {
							return new NumberAtom(head_as_DataListAtom.list.size());
						}else throw new DataListQuestionError();
					}else {
						second_node = run(second_node, lambda_vars);
						if(second_node instanceof NumberAtom) {
							NumberAtom second_node_As_NumberAtom = (NumberAtom) second_node;
							if(second_node_As_NumberAtom.type == NumberType.INTEGER) {
								if(second_node_As_NumberAtom.rawData >= 0 &&
								   second_node_As_NumberAtom.rawData < head_as_DataListAtom.list.size())
									return head_as_DataListAtom.list.get((int) second_node_As_NumberAtom.rawData);
								else throw new DataListQuestionError();
							}else throw new DataListQuestionError();
						}
					}
				} else if(in_as_ListAtom.nodes.size() == 3) {
					Atom second_node = in_as_ListAtom.nodes.get(1);
					Atom third_node = in_as_ListAtom.nodes.get(2);
					if(second_node instanceof KeywordAtom) {
						third_node = run(third_node, lambda_vars);
						if(((KeywordAtom)second_node).keyword.equals("append")) {
							List<Atom> the_list = head_as_DataListAtom.list;
							the_list.add(third_node);
						} else if(((KeywordAtom)second_node).keyword.equals("remove")) {
							if(third_node instanceof NumberAtom) {
								if(((NumberAtom)third_node).type == NumberType.INTEGER) {
									int index = (int) ((NumberAtom)third_node).rawData;
									List<Atom> the_list = head_as_DataListAtom.list;
									
									if(index >= the_list.size() || index < 0) throw new DataListQuestionError();
									
									the_list.remove(index);
								} else throw new DataListQuestionError();
							} else throw new DataListQuestionError();
						} else throw new DataListQuestionError();
					} else throw new DataListQuestionError();
				} else if(in_as_ListAtom.nodes.size() == 4) {
					Atom second_node = in_as_ListAtom.nodes.get(1);
					Atom third_node = in_as_ListAtom.nodes.get(2);
					Atom fourth_node = in_as_ListAtom.nodes.get(3);
					if(second_node instanceof KeywordAtom) {
						third_node = run(third_node, lambda_vars);
						fourth_node = run(fourth_node, lambda_vars);
						if(((KeywordAtom)second_node).keyword.equals("set")) {
							if(third_node instanceof NumberAtom) {
								if(((NumberAtom)third_node).type == NumberType.INTEGER) {
									int index = (int) ((NumberAtom)third_node).rawData;
									List<Atom> the_list = head_as_DataListAtom.list;
									
									if(index >= the_list.size() || index < 0) throw new DataListQuestionError();
									
									the_list.set(index, fourth_node);
								} else throw new DataListQuestionError();
							} else throw new DataListQuestionError();
						} else throw new DataListQuestionError();
					}else throw new DataListQuestionError();
				}else throw new DataListQuestionError();
			}
			return head_of_list;
		} else {
			ListAtom onwards_list = new ListAtom();
			onwards_list.nodes.add(in_atom);
			return run(onwards_list, lambda_vars);
		}

	}

	public Atom run(Atom in_atom) throws Exception {
		return run(in_atom, NULL_LAMBDA_VARS);
	}

	private final static HashMap<String, Atom> NULL_LAMBDA_VARS = new HashMap<String, Atom>();

	@SuppressWarnings("unused")
	private Atom handleKeyword(ListAtom in_ListAtom) throws Exception {
		return handleKeyword(in_ListAtom, NULL_LAMBDA_VARS);
	}

	@SuppressWarnings("unchecked")
	private Atom handleKeyword(ListAtom in_ListAtom, HashMap<String, Atom> lambda_vars) throws Exception {
		/*
		 * MASSIVE SWITCH STATEMENT INCOMING
		 * 
		 * Brace! Brace! Brace!
		 * 
		 */
		switch (((KeywordAtom) in_ListAtom.nodes.get(0)).keyword) {
		/*
		 * 
		 * DATA AND FUNCTIONS
		 * 
		 * 
		 */
		case "set":
			if (in_ListAtom.nodes.get(1) instanceof ListAtom) {
				StructureField field_to_edit = null;

				ListAtom structure_reference = (ListAtom) in_ListAtom.nodes.get(1);

				Atom head_atom = run(structure_reference.nodes.get(0), lambda_vars);

				if (head_atom instanceof StructureAtom) {

					StructureAtom the_struct_we_on = (StructureAtom) head_atom;

					if (structure_reference.nodes.size() == 1)
						return the_struct_we_on;

					for (int index_on_ref = 1; index_on_ref < structure_reference.nodes.size(); index_on_ref++) {
						Atom token_at_ref_index = structure_reference.nodes.get(index_on_ref);

						String name_of_this_token;

						if (token_at_ref_index instanceof VariableAtom) {
							VariableAtom token_at_index_as_VariableAtom = (VariableAtom) token_at_ref_index;

							name_of_this_token = token_at_index_as_VariableAtom.name;

						} else if (token_at_ref_index instanceof KeywordAtom) {
							KeywordAtom token_at_index_as_KeywordAtom = (KeywordAtom) token_at_ref_index;
							name_of_this_token = token_at_index_as_KeywordAtom.keyword;
						} else {
							throw new SRE_TokenTypeNotValid();
						}

						StructureField the_field_sofar_referenced = null;
						for (StructureField field_being_checked : the_struct_we_on.fields)
							if (field_being_checked.field_name.equals(name_of_this_token)
									&& (field_being_checked.field_type == FieldType.Immutable
											|| field_being_checked.field_type == FieldType.Mutable)) {

								the_field_sofar_referenced = field_being_checked;
								break;
							}
						;

						if (the_field_sofar_referenced == null)
							throw new SRE_FieldNonExistent();

						field_to_edit = the_field_sofar_referenced;
						if (field_to_edit.field_value instanceof StructureAtom) {
							the_struct_we_on = (StructureAtom) field_to_edit.field_value;
						}
					}

					if (field_to_edit.field_type != FieldType.Mutable)
						throw new SRE_FieldIsNotAccessibleInThisContext();
					/*
					 * The final SET!
					 */
					Atom the_return = run(in_ListAtom.nodes.get(2), lambda_vars);
					field_to_edit.field_value = run(the_return, lambda_vars);
					return the_return;
					// Ah
				} else
					throw new SRE_TokenTypeNotValid();
			} else if (in_ListAtom.nodes.get(1) instanceof VariableAtom) {
				Atom the_return = run(in_ListAtom.nodes.get(2), lambda_vars);
				variables_map.put(((VariableAtom) in_ListAtom.nodes.get(1)).name,
						the_return);
				return the_return;
			}
			break;
		case "lambda":
			if (in_ListAtom.nodes.get(1) instanceof ListAtom) {
				if(in_ListAtom.nodes.size() != 3) throw new ParametersIncorectEx();
				
				ListAtom listof_lambda_vars = (ListAtom) in_ListAtom.nodes.get(1);
				ListAtom function = (ListAtom) in_ListAtom.nodes.get(2);
				while (function.nodes.get(0) instanceof ListAtom)
					function = (ListAtom) function.nodes.get(0);
				return new LambdaAtom(listof_lambda_vars, function);
			} else throw new ParametersIncorectEx();
		case "list":
			if(true) {
				DataListAtom the_return = new DataListAtom(ListType.LinkedList);
				
				for(int index = 1; index < in_ListAtom.nodes.size(); index++) {
					the_return.list.add(run(in_ListAtom.nodes.get(index), lambda_vars));
				}
				return the_return;
			}
		case "array":
			if(true) {
				DataListAtom the_return = new DataListAtom(ListType.ArrayList);
				
				for(int index = 1; index < in_ListAtom.nodes.size(); index++) {
					the_return.list.add(run(in_ListAtom.nodes.get(index), lambda_vars));
				}
				return the_return;
			}
		case "structure":
			if (in_ListAtom.nodes.size() != 2) throw new ParametersIncorectEx();
			
			if (in_ListAtom.nodes.get(1) instanceof ListAtom) {
				List<StructureField> elements_on_struct = new LinkedList<StructureField>();
				ListAtom struct_body = (ListAtom) in_ListAtom.nodes.get(1);
				for (Atom element : struct_body.nodes) {
					if (element instanceof ListAtom) {
						ListAtom element_as_ListAtom = (ListAtom) element;
						StructureField to_add = new StructureField(element_as_ListAtom);
						to_add.field_value = run(to_add.field_value, lambda_vars);
						if (element_as_ListAtom.nodes.size() > 0)
							elements_on_struct.add(to_add);
					} else {
						throw new StructureFieldInvalidEx();
					}
				}
				return new StructureAtom(elements_on_struct);
			} throw new ParametersIncorectEx();
		/*
		 * 
		 * STRINGS
		 * 
		 * 
		 */
		case "to-str":
			if (in_ListAtom.nodes.size() == 2) {
				Object o = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					return new StringAtom(((NumberAtom)o).convertToString(), 0);
				} else if (o instanceof StringAtom) {
					return (Atom) o;
					
				}else throw new ParametersIncorectEx();
			} throw new ParametersIncorectEx();
		case "parse-int":
			if (in_ListAtom.nodes.size() == 2) {
				Object o = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (o instanceof NumberAtom) {
					return (Atom) o;
				} else if (o instanceof StringAtom) {
					StringAtom atom = (StringAtom) o;
					return new NumberAtom(Integer.parseInt(atom.value));
				} else throw new ParametersIncorectEx();
			} throw new ParametersIncorectEx();
		case "input":
			if (in_ListAtom.nodes.size() == 2 ||
			    
				in_ListAtom.nodes.size() == 1) {
				String prompt = "";
				
				if (in_ListAtom.nodes.size() >= 2) {
					Atom possible_prompt = run(in_ListAtom.nodes.get(1), lambda_vars);
					if (possible_prompt instanceof StringAtom) {
						prompt += ((StringAtom) possible_prompt).value;
					} else if (possible_prompt instanceof NumberAtom) {
						prompt += ((NumberAtom) possible_prompt).getValue();
					} else throw new ParametersIncorectEx();
				}
				
				Scanner scan = new Scanner(System.in);
				System.out.print(prompt);
				String out = scan.nextLine();
				
				return new StringAtom(out, 0);
			}else throw new ParametersIncorectEx();
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
						OutC.debug(text);
						throw new ParametersIncorectEx();
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
		case "+":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				
				if (nums.size() > 0) return Operate.sum(nums);
				else throw new ParametersIncorectEx();
				
			} else if (in_ListAtom.nodes.size() == 2) {
				if (in_ListAtom.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) in_ListAtom.nodes.get(1);
				} else throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case "-":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				
				if (nums.size() > 0) return Operate.sub(nums); 
				else throw new ParametersIncorectEx();
				
			} else if (in_ListAtom.nodes.size() == 2) {
				if (in_ListAtom.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) in_ListAtom.nodes.get(1);
				} else throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case "*":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				
				if (nums.size() > 0) return Operate.prod(nums);
				else throw new ParametersIncorectEx();
				
			} else if (in_ListAtom.nodes.size() == 2) {
				if (in_ListAtom.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) in_ListAtom.nodes.get(1);
				} else throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case "/":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				
				if (nums.size() > 0) return Operate.div(nums);
				else throw new ParametersIncorectEx();
				
			} else if (in_ListAtom.nodes.size() == 2) {
				if (in_ListAtom.nodes.get(1) instanceof NumberAtom) {
					return (NumberAtom) in_ListAtom.nodes.get(1);
				} else throw new ParametersIncorectEx();
			} throw new ParametersIncorectEx();
		case "^":
			if (in_ListAtom.nodes.size() == 3) {
				Atom base = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(base instanceof NumberAtom)) throw new ParametersIncorectEx();
				Atom exponent = run(in_ListAtom.nodes.get(2), lambda_vars);
				if (!(exponent instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.pow((NumberAtom) base, (NumberAtom) exponent);
			} else throw new ParametersIncorectEx();
		case "log":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.log((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		//
		//iDiv
		//
		case "mod":
			if (in_ListAtom.nodes.size() == 3) {
				Atom base = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(base instanceof NumberAtom)) throw new ParametersIncorectEx();
				Atom exponent = run(in_ListAtom.nodes.get(2), lambda_vars);
				if (!(exponent instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.mod((NumberAtom) base, (NumberAtom) exponent);
			} else throw new ParametersIncorectEx();
		case "round":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.round((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "floor":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.floor((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "ceil":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.ceil((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "as-int":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				NumberAtom value_as_NumberAtom = (NumberAtom) value;
				if(value_as_NumberAtom.type==NumberType.INTEGER) return value_as_NumberAtom;
				else return new NumberAtom((long)Double.longBitsToDouble(value_as_NumberAtom.rawData));
			} else throw new ParametersIncorectEx();
		case "as-float":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				NumberAtom value_as_NumberAtom = (NumberAtom) value;
				if(value_as_NumberAtom.type==NumberType.FLOAT) return value_as_NumberAtom;
				else return new NumberAtom((double) value_as_NumberAtom.rawData);
			} else throw new ParametersIncorectEx();
		//
		//TRIG
		//
		case "sin":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.sin((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "cos":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.cos((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "tan":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.tan((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "asin":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.asin((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "acos":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.acos((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		case "atan":
			if (in_ListAtom.nodes.size() == 2) {
				Atom value = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (!(value instanceof NumberAtom)) throw new ParametersIncorectEx();
				
				return Operate.atan((NumberAtom) value);
			} else throw new ParametersIncorectEx();
		/*
		 * 
		 * BOOLEAN LOGIC FUNCTIONS
		 * 
		 * 
		 */
		// COMPARATORS
		case "=":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				if (nums.size() > 0)
					return Operate.equal(nums);
				else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case ">":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				if (nums.size() > 0)
					return Operate.greaterThan(nums);
				else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case "<":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				if (nums.size() > 0)
					return Operate.lessThan(nums);
				else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case ">=":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				if (nums.size() > 0)
					return Operate.greaterThanOrEQ(nums);
				else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		case "<=":
			if (in_ListAtom.nodes.size() >= 3) {
				List<NumberAtom> nums = new LinkedList<NumberAtom>();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						nums.add((NumberAtom) internal);
					} else throw new ParametersIncorectEx();
				}
				if (nums.size() > 0)
					return Operate.lessThanOrEQ(nums);
				else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		// BOOL LOGIC
		case "AND":
			if (in_ListAtom.nodes.size() >= 3) {
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (!((NumberAtom) internal).isTrue())
							return new NumberAtom(0);
					}else if(internal != null) {
						throw new ParametersIncorectEx();
					}
				}
				return new NumberAtom(1);
			} else throw new ParametersIncorectEx();
		case "OR":
			if (in_ListAtom.nodes.size() >= 3) {
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							return new NumberAtom(1);
					} else throw new ParametersIncorectEx();

				}
				return new NumberAtom(0);
			} else throw new ParametersIncorectEx();
		case "XOR":
			if (in_ListAtom.nodes.size() >= 3) {
				int c = 0;
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					Atom internal = run(in_ListAtom.nodes.get(i), lambda_vars);
					if (internal instanceof NumberAtom) {
						if (((NumberAtom) internal).isTrue())
							c++;
					}else throw new ParametersIncorectEx();
				}
				return new NumberAtom((c == 1) ? 1 : 0);
			} else throw new ParametersIncorectEx();
		case "NOT":
			if (in_ListAtom.nodes.size() == 2) {
				Atom internal2 = run(in_ListAtom.nodes.get(1), lambda_vars);
				if (internal2 instanceof NumberAtom) {
					if (((NumberAtom) internal2).isTrue())
						return new NumberAtom(0);
					else
						return new NumberAtom(1);
				} else
					throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();

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
			if (in_ListAtom.nodes.size() >= 2) {
				Interpreter inter = new Interpreter();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					if (in_ListAtom.nodes.get(i) instanceof ListAtom) {
						ListAtom statement_we_on = (ListAtom) in_ListAtom.nodes.get(i);
						if (statement_we_on.nodes.size() != 0) 
						if (statement_we_on.nodes.get(0) instanceof KeywordAtom) {
							KeywordAtom k = (KeywordAtom) statement_we_on.nodes.get(0);
							if (k.keyword.equals("return")) {
								if (statement_we_on.nodes.size() > 1)
									return inter.run(statement_we_on.nodes.get(1), lambda_vars);
								else
									return null;
							}else {
								Atom internal = inter.run(statement_we_on, lambda_vars);
							}
						} else {
							Atom internal = inter.run(statement_we_on, lambda_vars);
						}
					} else throw new ParametersIncorectEx();
				}
			} else throw new ParametersIncorectEx();
		case "hide":
			if (in_ListAtom.nodes.size() >= 2) {
				Interpreter inter = new Interpreter();
				inter.variables_map = (HashMap<String, Atom>) this.variables_map.clone();
				for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
					if (in_ListAtom.nodes.get(i) instanceof ListAtom) {
						ListAtom statement_we_on = (ListAtom) in_ListAtom.nodes.get(i);
						if (statement_we_on.nodes.size() != 0) 
						if (statement_we_on.nodes.get(0) instanceof KeywordAtom) {
							KeywordAtom k = (KeywordAtom) statement_we_on.nodes.get(0);
							if (k.keyword.equals("return")) {
								if (statement_we_on.nodes.size() > 1)
									return inter.run(statement_we_on.nodes.get(1), lambda_vars);
								else
									return null;
							}else {
								Atom internal = inter.run(statement_we_on, lambda_vars);
							}
						} else {
							Atom internal = inter.run(statement_we_on, lambda_vars);
						}
					} else throw new ParametersIncorectEx();
				}
			} else throw new ParametersIncorectEx();
		case "then":
			for (int i = 1; i < in_ListAtom.nodes.size(); i++) {
				if (in_ListAtom.nodes.get(i) instanceof ListAtom) {
					ListAtom statement_we_on = (ListAtom) in_ListAtom.nodes.get(i);
					if (statement_we_on.nodes.size() != 0) 
						if (statement_we_on.nodes.get(0) instanceof KeywordAtom) {
							KeywordAtom k = (KeywordAtom) statement_we_on.nodes.get(0);
							if (k.keyword.equals("return")) {
								if (statement_we_on.nodes.size() > 1)
									return run(statement_we_on.nodes.get(1), lambda_vars);
								else
									return null;
							}else {
								Atom internal = run(statement_we_on, lambda_vars);
							}
						} else {
							Atom internal = run(statement_we_on, lambda_vars);
						}
				} else throw new ParametersIncorectEx();
			} return null;
		case "if":
			for (int i = 0; i < in_ListAtom.nodes.size(); i += 3) {
				if (in_ListAtom.nodes.get(i) instanceof KeywordAtom) {
					KeywordAtom keyword_atom = (KeywordAtom) in_ListAtom.nodes.get(i);
					if ((i == 0 && keyword_atom.keyword.equals("if")) || (i != 0 && keyword_atom.keyword.equals("elif"))) {
						Atom condition_atom = run(in_ListAtom.nodes.get(i + 1), lambda_vars);
						if (condition_atom instanceof NumberAtom) {
							if (((NumberAtom) condition_atom).isTrue())
								return run(in_ListAtom.nodes.get(i + 2), lambda_vars);
						} else {
							OutC.debug(condition_atom);
							throw new ParametersIncorectEx();
						}
					} else if (keyword_atom.keyword.equals("else")) {
						return run(in_ListAtom.nodes.get(i + 1), lambda_vars);
					} else throw new ParametersIncorectEx();
				} else throw new ParametersIncorectEx();
			} return null;
		case "while":
			if (in_ListAtom.nodes.size() == 3) {
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
			} else if (in_ListAtom.nodes.size() == 2) {
				throw new ParametersIncorectEx();
			} else throw new ParametersIncorectEx();
		}
		return null;
	}
}

package skinnylisp.exceptions;

import skinnylisp.exceptions.runtime_errors.Err_Dummy;
import skinnylisp.exceptions.runtime_errors.LispError;

public class LispRuntimeError extends Exception {
	private static final long serialVersionUID = 1L;
	
	public LispError error;
	public LispRuntimeError(LispError error) {
		this.error = error;
	}
	
	public LispRuntimeError() {
		this.error = new Err_Dummy();
	}
}

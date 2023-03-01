package skinnylisp.engine;

import skinnylisp.engine.runtime_errors.Err_Dummy;
import skinnylisp.engine.runtime_errors.RuntimeError;

public class WrapOf_RuntimeError extends Exception {
	private static final long serialVersionUID = 1L;
	
	public RuntimeError error;
	public WrapOf_RuntimeError(RuntimeError error) {
		this.error = error;
	}
	
	public WrapOf_RuntimeError() {
		this.error = new Err_Dummy();
	}
}

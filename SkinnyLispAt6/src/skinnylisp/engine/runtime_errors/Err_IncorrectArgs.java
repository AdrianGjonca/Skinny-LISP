package skinnylisp.engine.runtime_errors;

public class Err_IncorrectArgs extends RuntimeError {

	public String message;
	public Err_IncorrectArgs(String message) {
		this.message = message;
	}
	
	@Override
	public String getErrorMessage() {
		return message;
	}

}

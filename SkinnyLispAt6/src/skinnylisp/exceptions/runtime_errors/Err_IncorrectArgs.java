package skinnylisp.exceptions.runtime_errors;

public class Err_IncorrectArgs extends LispError {

	public String message;
	public Err_IncorrectArgs(String message) {
		this.message = message;
	}
	
	@Override
	public String getErrorMessage() {
		return message;
	}

}

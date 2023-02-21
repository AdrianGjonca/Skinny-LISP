package skinnylisp.exceptions.runtime_errors;

public class Err_Dummy extends LispError{

	@Override
	public String getErrorMessage() {
		return "DUMMY";
	}

}

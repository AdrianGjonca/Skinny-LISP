package skinnylisp.engine.runtime_errors;

public class Err_Ubounds extends RuntimeError {

	public int size;
	public int index;
	
	public Err_Ubounds(int size, int index) {
		this.size = size;
		this.index = index;
	}
	
	@Override
	public String getErrorMessage() {
		return index + " is out of bounds for list of size " + size;
	}

}

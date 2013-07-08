package mm19.exceptions;

public class InputException extends EngineException {

	private static final long serialVersionUID = -8454920366701176361L;
	public InputException(){
		super("I");
	}
	public InputException(String m){
		super(m);
	}
}

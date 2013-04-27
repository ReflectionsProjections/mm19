package mm19.exceptions;

public class ResourceException extends EngineException {
	public ResourceException(){
		super("R");
	}
	public ResourceException(String m){
		super(m);
	}
}

package mm19.exceptions;

public class ResourceException extends EngineException {
	
	private static final long serialVersionUID = -6180027649694058740L;
	public ResourceException(){
		super("R");
	}
	public ResourceException(String m){
		super(m);
	}
}

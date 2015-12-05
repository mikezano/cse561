package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class TargetClient extends ViewableAtomic {
	protected double waitTime = 0.1;
	protected int duration;
	protected entity currentVal;
 
	public TargetClient(){
		super("TC");
		addInport("in");
		addOutport("out");
	}

	public TargetClient(String name){
		super(name);
	}
	
	public void initialize(){
		passivate();
		super.initialize();
	}
	
	public void deltext(double e,message x){
		Continue(e);//resets timer
		if(messageOnPort(x,"in1",0) ){
			
			currentVal = x.getValOnPort("In1",0);
			holdIn("active", 1);
		}
	}
	
	public void deltint( ){
		passivate();
	}
	
	public message out( ){
		message m = new message();
		content con = makeContent("out", new entity("out"));
		m.add(con);
		return m;
	}
} 
package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class AuthenticationManager extends ViewableAtomic {
	protected double waitTime = 0.1;
	protected int duration;
	protected entity currentVal;
 
	public AuthenticationManager(){
		super("Authentication Manager");
		addInport("in");
		addOutport("out");
	}

	public AuthenticationManager(String name){
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
			//can check for input value (i.e., input event = 1M)
			//phase = "WAIT";
			//duration = 1;//storing for later what the duration of the light switch is
			//sigma = waitTime;
			holdIn("active", 1);
		}
	}
	
	public void deltint( ){
		passivate();
	}
	
	public message out( ){
		message m = new message();
		content con = makeContent("out", new entity("out"));
		return m;
	}
} 
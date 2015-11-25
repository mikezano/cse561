package cse561;
import GenCol.doubleEnt;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class KerberosServer extends ViewableAtomic {
	protected double waitTime = 0.1;
	protected int duration;
	protected entity currentVal;
 
	public KerberosServer(){
		super("KS");
		addInport("in");
		addOutport("out");
	}

	public KerberosServer(String name){
		super(name);
	}
	
	public void initialize(){
		passivate();
		super.initialize();
		holdIn("active", 0); //immediately trigger delta internal, and output
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
//		message m = new message();
//		content securityLevel = makeContent("SecurityLevel", new doubleEnt(1));
//		content applicationName = makeContent("ApplicationName", new entity("XYZ"));
//		content payloadSize = makeContent("PayloadSize", new doubleEnt(3));
//		m.add(securityLevel);
//		m.add(applicationName);
//		m.add(payloadSize);
//		return m;
		return null;
	}
} 
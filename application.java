package cse561;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class Application extends ViewableAtomic {

	private Integer m_securityLevel;
 
	public Application(){
		super("Application");
		SetupModel();
	}

	public Application(String name){
		super(name);
		SetupModel();
	}

	void SetupModel()
	{
		m_securityLevel = 0;
		addInport("in_start");
		addOutport("out_initId");
		addOutport("out_recvId");
		addOutport("out_securityLevel");
		
		phase = "passive";
	}
	
	public void initialize(){
		super.initialize();
	}
	
	public void deltext(double e,message x){
		Continue(e);
		
		if (messageOnPort(x,"in_start",0) && phase.equals("passive") ){
			
			entity val = x.getValOnPort("in_start",0);
			m_securityLevel = Integer.parseInt(val.toString());
			
			sigma = 0.1;
			phase = "active";
		}
	}
	
	public void deltint( ){
		phase = "passive";
		sigma = 1/0.0;
	}
	
	public message out( ){
		message m = new message();
		
		content securityLevel = makeContent("out_securityLevel", new entity(m_securityLevel.toString()));
		content sourceId = makeContent("out_initId", new entity("Source ID"));
		content recvId = makeContent("out_recvId", new entity("Destination ID"));
		m.add(securityLevel);
		m.add(sourceId);
		m.add(recvId);
		return m;
	}
} 
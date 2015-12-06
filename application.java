package cse561;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


/*
 * This class acts as a generator in our model construct. It receives an indication to start and the level it should start
 * on (with respect to securitylevels 1 - 3). Based on that it'll stimulate inputs to the respective blocks to kick off
 * the authentication process.
 */
public class Application extends ViewableAtomic 
{

	private Integer m_securityLevel;
 
	public Application()
	{
		super("Application");
		SetupModel();
	}

	public Application(String name)
	{
		super(name);
		SetupModel();
	}

	void SetupModel()
	{
		//Initialize privates.
		m_securityLevel = 0;
		phase = "passive";
		
		//Create IO ports.
		addInport("in_start");
		addOutport("out_initId");
		addOutport("out_recvId");
		addOutport("out_securityLevel");
	}
	
	public void initialize()
	{
		super.initialize();
	}
	
	public void deltext(double e,message x)
	{
		Continue(e);
		
		if (messageOnPort(x,"in_start",0) && phase.equals("passive") ){
			
			entity val = x.getValOnPort("in_start",0);
			m_securityLevel = Integer.parseInt(val.toString());
			
			sigma = 0.1;
			phase = "active";
		}
	}
	
	public void deltint( )
	{
		phase = "passive";
		sigma = 1/0.0;
	}
	
	public message out( )
	{
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
package cse561;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class AsyncCoord extends ViewableAtomic 
{
	//Private variables.
	private double m_delay = 0.1;	//0.1 for simplicity sake. Future work can expand to get more specific behavior and measurements.
	private Integer m_currentPayload = 0;
	
	//Default constructor.
	public GenericCryptoEngine() 
	{
		super("AsyncCryptoCoordinator");
		SetupModel();
	}
	
	//Parameterized constructor.
	public GenericCryptoEngine(String name, CryptoEngineType type, double mwPerByte) 
	{
		super(name);
		SetupModel();
	}

	private void SetupModel()
	{
		addInport("in_payloadSize");
		addInport("in_opType");
		addOutport("out_encryptPayloadSize");
		addOutport("out_decryptPayloadSize");
		
		m_currentPayload = 0;
		
		phase = "Passive";
	}

	//Getter for default milliwatts per byte based on engine type.
	private double GetDefaultMwPerByte(CryptoEngineType type) 
	{
		switch (type) {
			case HASH: 
				return DefaultHashMilliWattsPerByte;
			case ASYMMETRIC:
				return DefaultAsymmMilliWattsPerByte;
			case SYMMETRIC:
				return DefaultSymmMilliWattsPerByte;
		}
		
		return 0;
	}
		
	public void deltext(double e,message x)
	{
		Continue(e);

		if(messageOnPort(x, "in_payloadSize",0) && phaseIs("Passive")){
			entity val = x.getValOnPort("in_payloadSize",0);
			m_currentPayload = Integer.parseInt(val.toString());
			phase = "Active";
			sigma = m_delay;
		}
	}

	public void deltint( )
	{
		phase = "Passive";
		sigma = 1/0.0;	//Infinity.
	}
	
	public message out( )
	{
		Double powerConsumed = m_currentPayload * m_mwPerByte;
		
		message m = new message();

		content power = makeContent("out_power", new entity(powerConsumed.toString()));
		content size = makeContent("out_size", new entity(m_currentPayload.toString()));
		
		m.add(power);
		m.add(size);
		
		return m;
	}
	
	public void initialize()
	{
		super.initialize();
	}
}



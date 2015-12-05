package cse561;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class AsymmCoord extends ViewableAtomic 
{
	//Private variables.
	private double m_delay = 0.1;	//0.1 for simplicity sake. Future work can expand to get more specific behavior and measurements.
	private Integer m_currentPayload = 0;
	private AsymmOpType m_type;
	
	//Default constructor.
	public AsymmCoord() 
	{
		super("AsyncCryptoCoordinator");
		SetupModel();
	}
	
	//Parameterized constructor.
	public AsymmCoord(String name) 
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
		
		m_type = AsymmOpType.ENCRYPT;
		phase = "Passive";
	}
		
	public void deltext(double e,message x)
	{
		Continue(e);
		
		m_currentPayload = 0;

		//Get inputs.
		for (int idx = 0; idx < x.size(); idx++) {
			//Get the payload size.
			if(messageOnPort(x, "in_payloadSize", idx) && phaseIs("Passive")) {
				entity val = x.getValOnPort("in_payloadSize", idx);
				m_currentPayload = Integer.parseInt(val.toString());
			}
			
			//Get the operation type.
			if (messageOnPort(x, "in_opType", idx) && phaseIs("Passive")) {
				entity val = x.getValOnPort("in_opType", idx);
				if (val.toString().equals(AsymmOpType.DECRYPT.toString())) {
					m_type = AsymmOpType.DECRYPT;
				} else {
					m_type = AsymmOpType.ENCRYPT;
				}
			}
		}
		
		//If we got payload to deliver, activate.
		if (m_currentPayload > 0) {
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
		message m = new message();
		content payload;

		if (m_currentPayload > 0) {

			if(m_type == AsymmOpType.ENCRYPT) {
				payload = makeContent("out_encryptPayloadSize", new entity(m_currentPayload.toString()));
			} else {
				payload = makeContent("out_decryptPayloadSize", new entity(m_currentPayload.toString()));
			}
			
			m.add(payload);
		}

		return m;
	}
	
	public void initialize()
	{
		super.initialize();
	}
}



package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/*
 * This block mimics an authentication factor such as a pin process to authenticate user or 
 * fingerprint or facial-scanning scheme, etc. 
 */
public class AuthenticationFactorManager extends ViewableAtomic 
{
	private static final Double m_facialPowerRequired = 5.0;
	private static final Double m_irisPowerRequired = 5.0;
	private static final Double m_fingerprintPowerRequired = 5.0;
	private double m_delay;
	private AuthenticationFactorType m_reqType;
	
	public AuthenticationFactorManager()
	{
		super("Authentication Factor Manager");
		SetupModel();
	}

	public AuthenticationFactorManager(String name)
	{
		super(name);
		SetupModel();
	}
	
	private void SetupModel()
	{
		//Add ports.
		addInport("in_authType");
		addOutport("out_power");
		addOutport("out_authResult");

		//Add test inputs.
		addTestInput("in_authType", new entity(AuthenticationFactorType.FACIAL.toString()));
		addTestInput("in_authType", new entity(AuthenticationFactorType.IRIS.toString()));
		addTestInput("in_authType", new entity(AuthenticationFactorType.FINGERPRINT.toString()));
		
		//Initialize privates and variables.
		phase = "Passive";
		m_delay = 0.1;
		m_reqType = AuthenticationFactorType.NONE;
	}
	
	public void initialize()
	{
		super.initialize();
	}
	
	public void deltext(double e, message x)
	{
		Continue(e);

		//Check for inputs only if we're in the passive state.
		if(messageOnPort(x, "in_authType", 0) && phaseIs("Passive")) {
			entity val = x.getValOnPort("in_authType",0);

			if (val.toString().equals(AuthenticationFactorType.FACIAL.toString())) {
				m_reqType = AuthenticationFactorType.FACIAL;
				
			} else if (val.toString().equals(AuthenticationFactorType.IRIS.toString())) {
				m_reqType = AuthenticationFactorType.IRIS;
			
			} else if (val.toString().equals(AuthenticationFactorType.FINGERPRINT.toString())) {
				m_reqType = AuthenticationFactorType.FINGERPRINT;
			}
			
			phase = "Active";
			sigma = m_delay;
		}
	}
	
	public void deltint( )
	{
		m_reqType = AuthenticationFactorType.NONE;
		phase = "Passive";
		sigma = 1/0.0;	//Infinity.
	}
	
	public message out( )
	{
		message m = new message();

		Double power = 0.0;

		switch (m_reqType) {
			case FACIAL:
				power = m_facialPowerRequired;
				break;
			case IRIS:
				power = m_irisPowerRequired;
				break;
			case FINGERPRINT:
				power = m_fingerprintPowerRequired;
				break;
			default:
				System.out.println("Error! Authentication factor chosen does not exist");
				break;
		}
		
		//Generate output.
		content powerUsed = makeContent("out_power", new entity(power.toString()));
		content result = makeContent("out_authResult", new entity("Pass"));
		m.add(powerUsed);
		m.add(result);
		
		return m;
	}
} 
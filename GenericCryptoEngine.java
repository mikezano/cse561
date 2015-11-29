package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class GenericCryptoEngine extends ViewableAtomic {
	
	//Public variables.
	public static final CryptoEngineType DefaultEngineType = CryptoEngineType.SYMMETRIC;
	public static final double DefaultHashMilliWattsPerByte = 0.1;
	public static final double DefaultSymmMilliWattsPerByte = 0.2;
	public static final double DefaultAsymmMilliWattsPerByte = 0.2;
	
	//Private variables.
	private CryptoEngineType m_engineType = DefaultEngineType;
	private double m_mwPerByte;
	private double m_delay = 0.1;	//0.1 for simplicity sake. Future work can expand to get more specific behavior and measurements.
	private Integer m_currentPayload = 0;
	
	//Default constructor.
	public GenericCryptoEngine() 
	{
		super("Generic Crypto Engine");
		
		m_mwPerByte = GetDefaultMwPerByte(DefaultEngineType);
		SetupModel(DefaultEngineType);
	}
	
	//Parameterized constructor.
	public GenericCryptoEngine(String name, CryptoEngineType type, double mwPerByte) 
	{
		super(name);
		
		m_mwPerByte = mwPerByte;
		SetupModel(type);
	}

	/*
	 * This routine sets up the model according to the parameters passed.
	 * 
	 * @param type indicates the crypto engine type according to CryptoEngineType enumeration.
	 * @param mwPerByte indicates the milliwatts per byte to associate with this crypto block. 
	 * 
	 * return N/A
	 */
	private void SetupModel(CryptoEngineType type)
	{
		m_engineType = type;
		m_currentPayload = 0;
		
		phase = "Passive";
		
		addInport("in_payloadSize");
		addOutport("out_power");
		addOutport("out_size");
		
		addTestInput("in_payloadSize", new entity("100"));
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



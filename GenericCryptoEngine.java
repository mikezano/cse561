package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class GenericCryptoEngine extends ViewableAtomic {
	
	//Public variables.
	public static final CryptoEngineType DefaultEngineType = CryptoEngineType.SYMMETRIC;
	public static final double DefaultHashMilliJoulesPerByte = 0.1;
	public static final double DefaultSymmMilliJoulesPerByte = 0.2;
	public static final double DefaultAsymmMilliJoulesPerByte = 0.2;
	
	//Private variables.
	private CryptoEngineType m_engineType = DefaultEngineType;
	private double m_mjPerByte;
	private double m_delay = 0.1;	//0.1 for simplicity sake. Future work can expand to get more specific behavior and measurements.
	private Integer m_currentPayload = 0;
	
	//Default constructor.
	public GenericCryptoEngine() 
	{
		super("Generic Crypto Engine");
		
		m_mjPerByte = GetDefaultMjPerByte(DefaultEngineType);
		SetupModel(DefaultEngineType);
	}
	
	/*
	 * This routine sets up the model according to the parameters passed.
	 * 
	 * @param type indicates the crypto engine type according to CryptoEngineType enumeration.
	 * @param mjPerByte indicates the milli Joules per byte to associate with this crypto block. 
	 * 
	 * return N/A
	 */
	public GenericCryptoEngine(String name, CryptoEngineType type, double mjPerByte) 
	{
		super(name);
		
		m_mjPerByte = mjPerByte;
		SetupModel(type);
	}

	/*
	 * This routine sets up the model according to the parameters passed.
	 * 
	 * @param type indicates the crypto engine type according to CryptoEngineType enumeration.
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

	//Getter for default milli Joules per byte based on engine type.
	private double GetDefaultMjPerByte(CryptoEngineType type) 
	{
		switch (type) {
			case HASH: 
				return DefaultHashMilliJoulesPerByte;
			case ASYMMETRIC:
				return DefaultAsymmMilliJoulesPerByte;
			case SYMMETRIC:
				return DefaultSymmMilliJoulesPerByte;
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
		Double powerConsumed = m_currentPayload * m_mjPerByte;
		
		message m = new message();

		content power = makeContent("out_power", new entity(powerConsumed.toString()));
		content size;
		
		if (m_engineType == CryptoEngineType.HASH) {
			size = makeContent("out_size", new entity("20")); //Hash1 always outputs 20 bytes.
		} else {
			size = makeContent("out_size", new entity(m_currentPayload.toString()));
		}
		
		m.add(power);
		m.add(size);
		
		return m;
	}
	
	public void initialize()
	{
		super.initialize();
	}
}



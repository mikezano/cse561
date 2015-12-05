package cse561;

import model.modeling.message;
import GenCol.entity;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

public class AsymmetricEncryption extends ViewableDigraph 
{
	private ViewableAtomic m_decryptEngine;
	private ViewableAtomic m_encryptEngine;
	

	//Milli Joules per byte
	public static final double DefEncryptMjPerByte = 0.2;
	public static final double DefDecryptMjPerByte = 0.2;
	
	public AsymmetricEncryption()
	{
		super("Asymm Encryption");
		SetupModel(DefEncryptMjPerByte, DefDecryptMjPerByte);
	}

	public AsymmetricEncryption(String name)
	{
		super(name);
		SetupModel(DefEncryptMjPerByte, DefDecryptMjPerByte);
	}
	
	public AsymmetricEncryption(String name, double encryptMj, double decryptMj)
	{
		super(name);
		SetupModel(encryptMj, decryptMj);
	}

	
	private void SetupModel(double encryptMj, double decryptMj)
	{
		//Add ports.
		addInport("in_payloadSize");
		addInport("in_opType");
		addOutport("out_power");
		addOutport("out_size");

		//Instantiate encryption and decryption engine blocks.
		ViewableAtomic m_decryptEngine = new GenericCryptoEngine("AsymmDecryptionEngine", CryptoEngineType.ASYMMETRIC, encryptMj);
		ViewableAtomic m_encryptEngine = new GenericCryptoEngine("AsymmEncryptionEngine", CryptoEngineType.ASYMMETRIC, decryptMj);
		
		//Add them to model.
		add(m_decryptEngine);
		add(m_encryptEngine);
		
		initialize();

		addCoupling(this,"in_payloadSize", m_decryptEngine, "in_payloadSize");
		addCoupling(this,"in_payloadSize", m_encryptEngine, "in_payloadSize");
		addCoupling(this,"in_payloadSize", m_decryptEngine, "in_payloadSize");
		addCoupling(this,"in_payloadSize", m_decryptEngine, "in_payloadSize");
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

	
	public void initialize()
	{
		super.initialize();
	}
} 
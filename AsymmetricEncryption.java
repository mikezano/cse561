package cse561;

import java.awt.Dimension;
import java.awt.Point;

import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

/*
 * This model serves to behave as an asymmetric encryption/decryption block.
 * This is a composite model where it instantiates a decryption block, an encryption block
 * and a coordinator to manage which cipher block to use.
 */
public class AsymmetricEncryption extends ViewableDigraph 
{
	private ViewableAtomic m_decryptEngine;
	private ViewableAtomic m_encryptEngine;
	private ViewableAtomic m_coordinator;

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
	
	/*
	 * Parameterized constructor to allow user to specify the decryption and encryption milli-Joules per byte required.
	 */
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
		m_decryptEngine = new GenericCryptoEngine("DecryptEngine", CryptoEngineType.ASYMMETRIC, encryptMj);
		m_encryptEngine = new GenericCryptoEngine("EncryptEngine", CryptoEngineType.ASYMMETRIC, decryptMj);
		m_coordinator = new AsymmCoord("Coordinator");
		
		//Add them to model.
		add(m_decryptEngine);
		add(m_encryptEngine);
		add(m_coordinator);
		
		initialize();

		//Wire blocks.
		addCoupling(this,"in_payloadSize", m_coordinator, "in_payloadSize");
		addCoupling(this,"in_opType", m_coordinator, "in_opType");
		
		addCoupling(m_coordinator, "out_decryptPayloadSize", m_decryptEngine, "in_payloadSize");
		addCoupling(m_coordinator, "out_encryptPayloadSize", m_encryptEngine, "in_payloadSize");
		
		addCoupling(m_encryptEngine, "out_power", this, "out_power");
		addCoupling(m_decryptEngine, "out_power", this, "out_power");
		addCoupling(m_encryptEngine, "out_size", this, "out_size");
		addCoupling(m_decryptEngine, "out_size", this, "out_size");
		
		//Set layout.
		m_coordinator.setPreferredLocation(new Point(0, 50));
		m_decryptEngine.setPreferredLocation(new Point(350, 50));
		m_encryptEngine.setPreferredLocation(new Point(350, 200));
	}
	
	public void initialize()
	{
		super.initialize();
	}
	

    public void layoutForSimView()
    {
        preferredSize = new Dimension(800, 300);
    }
} 
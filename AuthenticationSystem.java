package cse561;

import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

/*
 * This is the main class integrating the entire model components for our simulation needs.
 */
public class AuthenticationSystem extends ViewableDigraph {

	public AuthenticationSystem(){
		super("Authentication System");
		CoupledModelConstruct();
	}

	public AuthenticationSystem(String name) {
		super(name);
		CoupledModelConstruct();
	}

	private static final String m_authMgrName = "Authentication Manager"; 
	private static final String m_authFactorMgrName = "Authentication Factor";
	private static final String m_authServerName = "Authentication Server";
	private static final String m_symmCryptoName = "Symmetric Crypto Engine";
	private static final String m_asymmCryptoName = "Asymmetric Crypto Engine";
	private static final String m_hashName = "Hash Engine";
	private static final String m_appName = "Generator";
	private static final String m_xducerName = "Transducer";
	
	/*	These are potential component configurations. All measurements and data are based on the paper
	 * "Energy Analysis of Public-Key Cryptography on Small Wireless Devices" by A. S. Wanter, N. Gura, H. Eberle, V. Gupta and S. C. Shantz.
	 * 		
	 * AES128		ECDSA160 Encrypt	ECDSA160 Decrypt		ECDSA224 Encrypt		ECDSA224 Decrypt		RSA1024 Encrypt		RSA1024 Decrypt		SHA1
	 * 			1.62		1.141				2.2545					3.077					6.099					15.2				0.595				5.9
	 * 
	 * 			Symm Crypto		Asymm Encrypt		Asymm Decrypt		Hash
	 * case 1	AES128			ECDSA160			ECDSA160			SHA1
	 * case 2	AES128			ECDSA224			ECDSA224			SHA1
	 * case 3	AES128			RSA1024				RSA1024				SHA1
	 */
	public static final double[][] TestCases = {
													{1.62, 1.141, 2.2545, 0.0059},
													{1.62, 3.077, 6.099,  0.0059},
													{1.62, 15.2,  0.595,  0.0059}
												};

	public static final int SYMM = 0;
	public static final int ASYMM_ENCRYPT = 1;
	public static final int ASYMM_DECRYPT = 2;
	public static final int HASH = 3;

	private void CoupledModelConstruct() 
	{
		//We use case 3 (as documented above) as AES128 and RSA1024 are the most popular configurations. 
		int testCaseIdx = 2;
		
		//Declare port to start the model.
		addInport("in_start");

		//Instantiate all components required.		
		AuthenticationManager authMngr = new AuthenticationManager(m_authMgrName, 4096);
		ViewableAtomic authFactorMngr = new AuthenticationFactorManager(m_authFactorMgrName);
		ViewableAtomic authServer = new AuthenticationServer(m_authServerName);
		ViewableAtomic symmCrypto = new SymmetricEncryption(m_symmCryptoName, TestCases[testCaseIdx][SYMM]);
		ViewableDigraph asymmCrypto = new AsymmetricEncryption(m_asymmCryptoName, TestCases[testCaseIdx][ASYMM_ENCRYPT], TestCases[testCaseIdx][ASYMM_DECRYPT]);
		ViewableAtomic hashEngine = new Hash(m_hashName, TestCases[testCaseIdx][HASH]);
		ViewableAtomic app = new Application(m_appName);
		ViewableAtomic xducer = new Transducer(m_xducerName);
	
		//Add them to the model.
		add(authMngr);
		add(authFactorMngr);
		add(symmCrypto);
		add(asymmCrypto);
		add(hashEngine);
		add(authServer);
		add(app);
		add(xducer);
		
		initialize();
		
		//Add test inputs to inject. These correspond to the security level of interest in this model.
		addTestInput("in_start", new entity("1"));
		addTestInput("in_start", new entity("2"));
		addTestInput("in_start", new entity("3"));

		//Glue the different components.
		addCoupling(this, "in_start", app, "in_start");
		
		addCoupling(app,"out_securityLevel", authMngr, "in_security");
		addCoupling(app,"out_initId", authMngr, "in_initId");
		addCoupling(app,"out_recvId", authMngr, "in_recvId");

		addCoupling(authFactorMngr, "out_authResult", authMngr, "in_authResult");
		addCoupling(symmCrypto, "out_size", authMngr, "in_symmSize");
		addCoupling(asymmCrypto, "out_size", authMngr, "in_asymmSize");
		addCoupling(hashEngine, "out_size", authMngr, "in_hashSize");
		addCoupling(authServer, "out_payloadSize", authMngr, "in_srvPayloadSize");
		
		addCoupling(authMngr, "out_symmSize", symmCrypto, "in_payloadSize");
		addCoupling(authMngr, "out_asymmSize", asymmCrypto, "in_payloadSize");
		addCoupling(authMngr, "out_asymmOp", asymmCrypto, "in_opType");
		addCoupling(authMngr, "out_hashSize", hashEngine, "in_payloadSize");
		addCoupling(authMngr, "out_authType", authFactorMngr, "in_authType");
		addCoupling(authMngr, "out_srvReq", authServer, "in_type");
		
		addCoupling(symmCrypto, "out_power", xducer, "in_sePower");
		addCoupling(asymmCrypto, "out_power", xducer, "in_aePower");
		addCoupling(hashEngine, "out_power", xducer, "in_hPower");
		addCoupling(authFactorMngr, "out_power", xducer, "in_afPower");
		
		//Make them look pretty on the canvas.
		asymmCrypto.setPreferredLocation(new Point(100, 50));
		authFactorMngr.setPreferredLocation(new Point(650, 400));
		hashEngine.setPreferredLocation(new Point(650, 500));
		symmCrypto.setPreferredLocation(new Point(250, 600));
		authServer.setPreferredLocation(new Point(650, 600));
		authMngr.setPreferredLocation(new Point(275, 400));
		app.setPreferredLocation(new Point(15, 400));
		xducer.setPreferredLocation(new Point(15, 600));
	}
	
	public void initialize()
	{
		super.initialize();
	}
	
    public void layoutForSimView()
    {
        preferredSize = new Dimension(1100, 700);
    }
}

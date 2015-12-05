package cse561;

import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

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
	
	private void CoupledModelConstruct() {
		addInport("in_start");

		//Instantiate all components required.		
		AuthenticationManager authMngr = new AuthenticationManager(m_authMgrName);
		
		ViewableAtomic authFactorMngr = new AuthenticationFactorManager(m_authFactorMgrName);
		ViewableAtomic authServer = new AuthenticationServer(m_authServerName);
		ViewableAtomic symmCrypto = new SymmetricEncryption(m_symmCryptoName);
		ViewableDigraph asymmCrypto = new AsymmetricEncryption(m_asymmCryptoName);
		ViewableAtomic hashEngine = new Hash(m_hashName);
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
		
		asymmCrypto.setPreferredLocation(new Point(267, 50));
		authFactorMngr.setPreferredLocation(new Point(600, 50));
		hashEngine.setPreferredLocation(new Point(900, 50));
		symmCrypto.setPreferredLocation(new Point(350, 400));
		authServer.setPreferredLocation(new Point(750, 400));
		authMngr.setPreferredLocation(new Point(275, 200));
		app.setPreferredLocation(new Point(15, 200));
		xducer.setPreferredLocation(new Point(1100, 400));
	}
	
	public void initialize()
	{
		super.initialize();
	}
	
    public void layoutForSimView()
    {
        preferredSize = new Dimension(1400, 600);
    }
}

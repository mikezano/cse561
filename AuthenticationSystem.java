package cse561;

import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
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
	
	private void CoupledModelConstruct() {
		addInport("in_start");
//		addInport("in_initId");
//		addInport("in_recvId");
		addOutport("out_msg");

		//Instantiate all components required.
		
		AuthenticationManager authMngr = new AuthenticationManager(m_authMgrName);
		
		ViewableAtomic authFactorMngr = new AuthenticationFactorManager(m_authFactorMgrName);
		ViewableAtomic authServer = new AuthenticationServer(m_authServerName);
		ViewableAtomic symmCrypto = new SymmetricEncryption(m_symmCryptoName);
		ViewableAtomic asymmCrypto = new AsymmetricEncryption(m_asymmCryptoName);
		ViewableAtomic hashEngine = new Hash(m_hashName);
		ViewableAtomic app = new Application(m_appName);
		//Transducer t = new Transducer();
	
		//Add them to the model.
		add(authMngr);
		add(authFactorMngr);
		add(symmCrypto);
		add(asymmCrypto);
		add(hashEngine);
		add(authServer);
		add(app);
		//add(t);
		
		initialize();
		
		addTestInput("in_start", new entity("1"));
		addTestInput("in_start", new entity("2"));
		addTestInput("in_start", new entity("3"));
		/*
		addTestInput("in_initId", new entity("Source ID"));
		addTestInput("in_recvId", new entity("Destination ID"));
		addTestInput("in_secLvl", new entity("1"));
		addTestInput("in_secLvl", new entity("2"));
		addTestInput("in_secLvl", new entity("3"));
		*/		

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
		addCoupling(authMngr, "out_hashSize", hashEngine, "in_payloadSize");
		addCoupling(authMngr, "out_authType", authFactorMngr, "in_authType");
		addCoupling(authMngr, "out_srvReq", authServer, "in_type");
		
		asymmCrypto.setPreferredLocation(new Point(267, 50));
		authFactorMngr.setPreferredLocation(new Point(600, 50));
		hashEngine.setPreferredLocation(new Point(900, 50));
		symmCrypto.setPreferredLocation(new Point(350, 400));
		authServer.setPreferredLocation(new Point(750, 400));
		authMngr.setPreferredLocation(new Point(275, 200));
		app.setPreferredLocation(new Point(15, 200));
		
		//Transducer
		/*
		addCoupling(authMngr, "out", t, "arriveAM");
		addCoupling(authFactorMngr, "out", t, "arriveAFM");
		addCoupling(symmCrypto, "out", t, "arriveSE");
		addCoupling(asymmCrypto, "out", t, "arriveAE");
		*/
	}
	

	
	public void initialize()
	{
		super.initialize();
	}
	
    public void layoutForSimView()
    {
        preferredSize = new Dimension(1200, 600);
    }
}

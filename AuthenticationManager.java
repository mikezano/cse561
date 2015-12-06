package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

/*
 * This model drives the entire authentication flow by redirecting and initiating 
 * server requests to authenticate, payload to decrypt/encrypt and payload to hash.
 */
public class AuthenticationManager extends ViewableAtomic 
{
	public static final String AuthenticationPass = "Pass";
	public static final String AuthenticationFail = "Fail";
	
	//Array of states this model needs to follow. This is basically a state machine description.
	private State states[] = new State[] {new IdleState(), new GetCertificateState(), new DecryptCertificateState(),
							 new GetCertificateHashState(), new EncryptAsymmReceiverRequestState(), new GetMessageHashState(),
							 new EncryptAsymmHashState(), new SendMessageState(), new AuthenticateUserState(), 
							 new EncryptKdcSessionRequestState(), new InitKdcSessionState(), new DecryptKdcSessionKeyState(),
							 new EncryptRecipientSessionRequestState(), new RequestRecipientSessionState(), 
							 new DecryptRecipientSessionKeyState(), new EncryptRecipientRequestState()};
	//Next state this model will go into.
	private String m_nextState;
	
	//Time delay to switch between states.
	private double waitTime = 0.1;

	//Variables to keep track of state specifics.
	private boolean m_authenticated = false;
	private String m_initId = null;
	private String m_recvId = null;
	private int m_securityLevel = 0;
	private int m_symmSize = 0;
	private int m_asymmSize = 0;
	private int m_hashSize = 0;
	private String m_authResult = null;
	private int m_srvPayloadSize = 0;
	private int m_recvPayloadSize = 0;
	
	//Setter/getter methods for attributes related to state variables.
	protected void SetInitId(String id) { m_initId = id; }
	protected String GetInitId() { return m_initId; }
	
	protected void SetRecvId(String id) { m_recvId = id; }
	protected String GetRecvId() { return m_recvId; }
	
	protected void SetSecurityLevel(int lvl) { m_securityLevel = lvl; }
	protected int GetSecurityLevel() { return m_securityLevel; }

	protected void SetSymmSize(int size) { m_symmSize = size; }
	protected int GetSymmSize() { return m_symmSize;}
	
	protected void SetAsymmSize(int size) { m_asymmSize = size; }
	protected int GetAsymmSize() { return m_asymmSize; }
	
	protected void SetRecvPayloadSize(int size) {m_recvPayloadSize = size;}
	protected int GetRecvPayloadSize() { return m_recvPayloadSize;}
	
	protected void SetHashSize(int size) { m_hashSize = size; }
	protected int GetHashSize() { return m_hashSize; }
	
	protected void SetAuthResult(String result) { m_authResult = result; }
	protected String GetAuthResult() { return m_authResult; }
	
	protected void SetSrvPayloadSize(int size) { m_srvPayloadSize = size; }
	protected int GetSrvPayloadSize() { return m_srvPayloadSize; }
	
	//Default constructor
	public AuthenticationManager(){
		super("Authentication Manager");
		SetupModel();
	}

	//Constructor with parameterized name.
	public AuthenticationManager(String name)
	{
		super(name);
		SetupModel();
	}
	
	//Constructor with parameterized name and parameterized receiver payload size once user is authenticated.
	public AuthenticationManager(String name, int recvPayload) 
	{
		super(name);
		SetupModel();
		SetRecvPayloadSize(recvPayload);
	}
	
	
	private void SetupModel()
	{
		//Add input ports.
		addInport("in_security");
		addInport("in_symmSize");
		addInport("in_asymmSize");
		addInport("in_hashSize");
		addInport("in_authResult");
		addInport("in_srvPayloadSize");
		addInport("in_initId");
		addInport("in_recvId");

		//Add output ports.
		addOutport("out_asymmOp");
		addOutport("out_hashSize");
		addOutport("out_symmSize");
		addOutport("out_asymmSize");
		addOutport("out_srvReq");
		addOutport("out_authType");
		
		//Initialize internal variables.
		m_authenticated = false;
		m_nextState = null;
		phase = AuthState.IDLE.toString();
		sigma = 1/0.0;
	}
	
	public void initialize()
	{
		super.initialize();
	}
	
	
	public void deltext(double e, message x)
	{
		//Loop through the states defined in this model's state machine.
		for (int idx = 0; idx < states.length; idx++) {
			
			//If we find out current state
			if (states[idx].GetState().toString().equals(phase)) {
				
				//Get the external transition function and execute it to find out the next state.
				String state = states[idx].Deltext(this, e, x);

				//If next state indicator is valid, set the model to change state on next internal transition.
				if (state != null) {
					m_nextState = state;
					phase = "WAIT";
					sigma = waitTime;
					break;
				}
			}
		}
	}
	
	public void deltint( )
	{
		
		//If we're at SEND_MESSAGE, change to authenticated if we just got here.
		if (m_nextState.equals(AuthState.SEND_MESSAGE.toString())) {
			
			if (m_authenticated == false) {
				m_authenticated = true;
				//Set timeout for authentication expiration.
				sigma = 5;
				
			//If we've been authenticated for a while, force re-authentication next time. 
			} else {
				m_authenticated = false;
				phase = AuthState.IDLE.toString();
				sigma = 1/0.0;
			}

		//If next state is valid, go to that state and wait indefinitely. 
		} else if (m_nextState != null) {
			phase = m_nextState;
			sigma = 1/0.0;
		}
	}
	

	public message out( ){
		message m = null;

		int idx = 0;
		
		//Iterate through all states to find current state.
		for (idx = 0; idx < states.length; idx++) {
			
			if (states[idx].GetState().toString().equals(m_nextState)) {
				
				//Get the output we need to generate from this state.
				m = states[idx].GetStateMessageOutput();
				break;
			}
		}
		
		//If we didn't find our current state in the state machine, something went wrong.
		if (idx == states.length) {
			System.out.println("Error! Did not find the output function corresponding to this state!");
		}

		return m;
	}


	/*
	 * State class definition. This represents a single state of the authentication process and it contains
	 * the external transition function as well as the output function for the associated state.
	 */
	class State {
		AuthState m_state;
		public State(AuthState state) {m_state = state;}
		AuthState GetState() { return m_state; }
		
		//This method performs what deltext does, however it does not update sigma nor phase.
		//The return value is a string indicative of the next state to advance to.
		String Deltext(AuthenticationManager model, double e, message x) { return null; }
		message GetStateMessageOutput() { return new message(); };
	}
	
	//Enumeration of all the states in the state machine.
	enum AuthState {
		IDLE, GET_CERTIFICATE, DECRYPT_CERTIFICATE, GET_CERTIFICATE_HASH, ENCRYPT_ASYMM_RECEIVER_REQUEST, GET_MESSAGE_HASH,
		ENCRYPT_ASYMM_HASH, SEND_MESSAGE, AUTHENTICATE_USER, ENCRYPT_KDC_SESSION_REQUEST, INIT_KDC_SESSION, DECRYPT_KDC_SESSION_KEY, 
		ENCRYPT_RECIPIENT_SESSION_REQUEST, REQUEST_RECIPIENT_SESSION, DECRYPT_RECIPIENT_SESSION_KEY, ENCRYPT_RECIPIENT_REQUEST
	}

	/*
	 * Definition for the DecryptRecipientRequest state.
	 */
	class EncryptRecipientRequestState extends State {
		public EncryptRecipientRequestState() { super(AuthState.ENCRYPT_RECIPIENT_REQUEST); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_symmSize", 0) && model.phaseIs(AuthState.ENCRYPT_RECIPIENT_REQUEST.toString())) {
				entity val = x.getValOnPort("in_symmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.SEND_MESSAGE.toString();
					model.SetSymmSize(payload);
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			Integer size = GetSrvPayloadSize();
			if (GetRecvPayloadSize() > 0) {
				size += GetRecvPayloadSize();
			}
			content payload = makeContent("out_symmSize", new entity(size.toString()));
			m.add(payload);
			return m;
		}
	}
	
	/*
	 * Definition for the DecryptRecipientSessionKey state
	 */
	class DecryptRecipientSessionKeyState extends State {
		public DecryptRecipientSessionKeyState() { super(AuthState.DECRYPT_RECIPIENT_SESSION_KEY); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_symmSize", 0) && model.phaseIs(AuthState.DECRYPT_RECIPIENT_SESSION_KEY.toString())) {
				entity val = x.getValOnPort("in_symmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.ENCRYPT_RECIPIENT_REQUEST.toString();
					model.SetSymmSize(payload);
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			Integer size = GetSrvPayloadSize();
			content payload = makeContent("out_symmSize", new entity(size.toString()));
			m.add(payload);
			return m;
		}
	}
	
	/*
	 * Definition for the RequestRecipientSession state
	 */
	class RequestRecipientSessionState extends State {
		public RequestRecipientSessionState() { super(AuthState.REQUEST_RECIPIENT_SESSION); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_srvPayloadSize", 0) && model.phaseIs(AuthState.REQUEST_RECIPIENT_SESSION.toString())) {
				entity val = x.getValOnPort("in_srvPayloadSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.DECRYPT_RECIPIENT_SESSION_KEY.toString();
					model.SetSrvPayloadSize(payload);
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			content kdcSession = makeContent("out_srvReq", new entity(ServerRequestType.KDC_SESSION.toString()));
			m.add(kdcSession);
			return m;
		}
	}

	/*
	 * Definition for the EncryptRecipientSessionRequest state
	 */
	class EncryptRecipientSessionRequestState extends State {
		public EncryptRecipientSessionRequestState() { super(AuthState.ENCRYPT_RECIPIENT_SESSION_REQUEST); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_symmSize", 0) && model.phaseIs(AuthState.ENCRYPT_RECIPIENT_SESSION_REQUEST.toString())) {
				entity val = x.getValOnPort("in_symmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.REQUEST_RECIPIENT_SESSION.toString();
					model.SetSymmSize(payload);
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			Integer size = GetSymmSize();
			content payload = makeContent("out_symmSize", new entity(size.toString()));
			content opType = makeContent("out_asymmOp", new entity(AsymmOpType.ENCRYPT.toString()));
			m.add(opType);
			m.add(payload);
			return m;
		}
	}
	
	/*
	 * Definition for the DecryptKdcSessionKey state
	 */
	class DecryptKdcSessionKeyState extends State {
		public DecryptKdcSessionKeyState() { super(AuthState.DECRYPT_KDC_SESSION_KEY); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_symmSize", 0) && model.phaseIs(AuthState.DECRYPT_KDC_SESSION_KEY.toString())) {
				entity val = x.getValOnPort("in_symmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.ENCRYPT_RECIPIENT_SESSION_REQUEST.toString();
					model.SetSymmSize(payload);
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			Integer size = GetSrvPayloadSize();
			content payloadSize = makeContent("out_symmSize", new entity(size.toString()));
			m.add(payloadSize);
			return m;
		}
	}

	/*
	 * Definition for the InitKdcSession state
	 */
	class InitKdcSessionState extends State {
		public InitKdcSessionState() { super(AuthState.INIT_KDC_SESSION); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_srvPayloadSize", 0) && model.phaseIs(AuthState.INIT_KDC_SESSION.toString())) {
				entity val = x.getValOnPort("in_srvPayloadSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.DECRYPT_KDC_SESSION_KEY.toString();
					model.SetSrvPayloadSize(payload);
				}
			}			
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			content kdcSession = makeContent("out_srvReq", new entity(ServerRequestType.KDC_SESSION.toString()));
			m.add(kdcSession);
			return m;
		}
	}
	
	/*
	 * Definition for the EncryptKdcSessionRequest state
	 */
	class EncryptKdcSessionRequestState extends State {
		public EncryptKdcSessionRequestState() { super(AuthState.ENCRYPT_KDC_SESSION_REQUEST); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_symmSize", 0) && model.phaseIs(AuthState.ENCRYPT_KDC_SESSION_REQUEST.toString())) {
				entity val = x.getValOnPort("in_symmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.INIT_KDC_SESSION.toString();
					model.SetSymmSize(payload);
				}
			}
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			Integer size = GetSymmSize();
			content payloadSize = makeContent("out_symmSize", new entity(size.toString()));
			m.add(payloadSize);
			return m;
		}
	}
	
	/*
	 * Definition for the AuthenticateUser state
	 */
	class AuthenticateUserState extends State {
		public AuthenticateUserState() { super(AuthState.AUTHENTICATE_USER); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_authResult", 0) && model.phaseIs(AuthState.AUTHENTICATE_USER.toString())) {
				entity val = x.getValOnPort("in_authResult",0);
				if (val.toString().equals(AuthenticationManager.AuthenticationPass)) {
					nextState = AuthState.ENCRYPT_KDC_SESSION_REQUEST.toString();
				} else if (val.toString().equals(AuthenticationManager.AuthenticationFail)) {
					nextState = AuthState.IDLE.toString();
				}
			}			
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();

			//AR: how will it choose what factor? Or do we want to choose different factors at all or just keep it simple?
			content factorType = makeContent("out_authType", new entity(AuthenticationFactorType.FINGERPRINT.toString()));
			m.add(factorType);
			return m;
		}
	}
	
	/*
	 * Definition for the SendMessage state
	 */
	class SendMessageState extends State {
		public SendMessageState() { super(AuthState.SEND_MESSAGE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			return AuthState.IDLE.toString();
		}
	}

	/*
	 * Definition for the EncryptAsymmHash state
	 */
	class EncryptAsymmHashState extends State {
		public EncryptAsymmHashState() { super(AuthState.ENCRYPT_ASYMM_HASH); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_asymmSize", 0) && model.phaseIs(AuthState.ENCRYPT_ASYMM_HASH.toString())) {
				entity val = x.getValOnPort("in_asymmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.SEND_MESSAGE.toString();
				}
			}	
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Get the size of content we need to encrypt from previous step.
			Integer payload = GetAsymmSize();
			content contentSizeToEncrypt = makeContent("out_asymmSize", new entity(payload.toString()));
			content opType = makeContent("out_asymmOp", new entity(AsymmOpType.ENCRYPT.toString()));
			m.add(opType);
			m.add(contentSizeToEncrypt);
			return m;
		}
	}


	/*
	 * Definition for the GetMessageHash state
	 */
	class GetMessageHashState extends State 
	{
		public GetMessageHashState() { super(AuthState.GET_MESSAGE_HASH); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_hashSize", 0) && model.phaseIs(AuthState.GET_MESSAGE_HASH.toString())) {
				entity val = x.getValOnPort("in_hashSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.ENCRYPT_ASYMM_HASH.toString();
					model.SetAsymmSize(payload);
				}
			}			
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Get the size of content we need to hash (message) and 
			//generate output to feed to hash engine.
			Integer payload = GetHashSize();
			content contentSizeToHash = makeContent("out_hashSize", new entity(payload.toString()));
			m.add(contentSizeToHash);
			return m;
		}
	}

	/*
	 * Definition for the EncryptAsymmReceiverRequest state
	 */
	class EncryptAsymmReceiverRequestState extends State {
		public EncryptAsymmReceiverRequestState() { super(AuthState.ENCRYPT_ASYMM_RECEIVER_REQUEST); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_asymmSize", 0) && model.phaseIs(AuthState.ENCRYPT_ASYMM_RECEIVER_REQUEST.toString())) {
				entity val = x.getValOnPort("in_asymmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.GET_MESSAGE_HASH.toString();
					model.SetHashSize(payload);
				}
			}
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Get the size of content we need to encrypt from previous step.
			Integer payload = GetAsymmSize();
			if (GetRecvPayloadSize() > 0) {
				payload += GetRecvPayloadSize();
			}
			content contentSizeToEncrypt = makeContent("out_asymmSize", new entity(payload.toString()));
			content opType = makeContent("out_asymmOp", new entity(AsymmOpType.ENCRYPT.toString()));
			m.add(opType);
			m.add(contentSizeToEncrypt);
			return m;
		}
	}
	
	/*
	 * Definition for the GetCertificateHash state
	 */
	class GetCertificateHashState extends State 
	{
		public GetCertificateHashState() { super(AuthState.GET_CERTIFICATE_HASH); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_hashSize", 0) && model.phaseIs(AuthState.GET_CERTIFICATE_HASH.toString())) {
				entity val = x.getValOnPort("in_hashSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.ENCRYPT_ASYMM_RECEIVER_REQUEST.toString();
					model.SetHashSize(payload);
				}
			}
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Get the size of content we need to hash (stuff decrypted containing the certificate) and 
			//generate output to feed to hash engine.
			Integer payload = GetHashSize();
			content contentSizeToHash = makeContent("out_hashSize", new entity(payload.toString()));
			m.add(contentSizeToHash);
			return m;
		}
	}
	
	/*
	 * Definition for the DecryptCertificate state
	 */
	class DecryptCertificateState extends State 
	{
		public DecryptCertificateState() { super(AuthState.DECRYPT_CERTIFICATE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			//Once we get the decrypted content (certificate), advance to get hash.
			if(model.messageOnPort(x, "in_asymmSize", 0) && model.phaseIs(AuthState.DECRYPT_CERTIFICATE.toString())) {
				entity val = x.getValOnPort("in_asymmSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.GET_CERTIFICATE_HASH.toString();
					//Store contents received which we need to hash.
					model.SetHashSize(payload);
				}
			}
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Get the payload received from the server, which is the certificate and feed as output to the crypto block for decryption.
			Integer payload = GetSrvPayloadSize();
			content encryptedCert = makeContent("out_asymmSize", new entity(payload.toString()));
			content opType = makeContent("out_asymmOp", new entity(AsymmOpType.DECRYPT.toString()));
			m.add(encryptedCert);
			m.add(opType);
			return m;
		}
	}

	/*
	 * Definition for the GetCertificate state
	 */
	class GetCertificateState extends State 
	{
		public GetCertificateState() { super(AuthState.GET_CERTIFICATE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			if(model.messageOnPort(x, "in_srvPayloadSize", 0) && model.phaseIs(AuthState.GET_CERTIFICATE.toString())) {
				entity val = x.getValOnPort("in_srvPayloadSize",0);
				Integer payload = Integer.parseInt(val.toString());
				if (payload != 0) {
					nextState = AuthState.DECRYPT_CERTIFICATE.toString();
					//Store the certificate received for future references.
					model.SetSrvPayloadSize(payload);
				}
			}
			return nextState;
		}
		
		@Override 
		public message GetStateMessageOutput() {
			message m = new message();
			//Request the server to send a certificate.
			content cert = makeContent("out_srvReq", new entity(ServerRequestType.CERTIFICATE.toString()));
			m.add(cert);
			return m;
		}		
	}
	
	/*
	 * Definition for the Idle state
	 */
	class IdleState extends State 
	{
		public IdleState () { super(AuthState.IDLE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			
			String recvId = null;
			String initId = null;
			Integer size = 0;
			entity val = null;
			
			//Parse all inputs.
			for (int idx = 0; idx < x.size(); idx++) {
				if (model.messageOnPort(x, "in_recvId", idx)) {
					recvId = x.getValOnPort("in_recvId", idx).toString();
				}
				
				if (model.messageOnPort(x, "in_initId", idx)) {
					initId = x.getValOnPort("in_initId", idx).toString();
					SetInitId(initId);
					size = GetInitId().length();
				}
				
				if (model.messageOnPort(x, "in_security", idx)) {
					val = x.getValOnPort("in_security",idx);
				}
			}
			
			//If no security level is received, return;
			if (val == null) {
				System.out.println("No security level detected");
				return nextState;
			}

			//If we are in the idle state
			if (model.phaseIs(AuthState.IDLE.toString())) {
				Double securityLevel = Double.parseDouble(val.toString());

				//If user wants to start security level 3, set the next state to get certificate.
				if (securityLevel == 3) {
					nextState = AuthState.GET_CERTIFICATE.toString();

				} else if (recvId != null && initId != null && 
						   !recvId.equals("") && !initId.equals("")) {

					//Set to initiate KDC session if level 1.
					if (securityLevel == 1) {
						nextState = AuthState.ENCRYPT_KDC_SESSION_REQUEST.toString();
						SetInitId(initId);
						SetRecvId(recvId);
						SetSymmSize(size);
						
					//Set to initiate user authentication if level 2.
					} else if (securityLevel == 2) {
						nextState = AuthState.AUTHENTICATE_USER.toString();
						SetInitId(initId);
						SetRecvId(recvId);
						SetSymmSize(size);
					}	
				}
			}
			return nextState;
		}
	}
}

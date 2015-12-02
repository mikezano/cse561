package cse561;
import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class AuthenticationManager extends ViewableAtomic 
{
	public static final String AuthenticationPass = "Pass";
	public static final String AuthenticationFail = "Fail";
	
	private State states[] = new State[] {new IdleState(), new GetCertificateState(), new DecryptCertificateState(),
							 new GetCertificateHashState(), new EncryptAsymmReceiverRequestState(), new GetMessageHashState(),
							 new EncryptAsymmHashState(), new SendMessageState(), new AuthenticateUserState(), 
							 new EncryptKdcSessionRequestState(), new InitKdcSessionState(), new DecryptKdcSessionKeyState(),
							 new EncryptRecipientSessionRequestState(), new RequestRecipientSessionState(), 
							 new DecryptRecipientSessionKeyState(), new EncryptRecipientRequestState()};
	private String m_nextState;
	
	private double waitTime = 0.1;

	private boolean m_authenticated;
	private String m_initId;
	private String m_recvId;
	private int m_securityLevel;
	private int m_symmSize;
	private int m_asymmSize;
	private int m_hashSize;
	private String m_authResult;
	private int m_srvPayloadSize;
	
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
	
	protected void SetHashSize(int size) { m_hashSize = size; }
	protected int GetHashSize() { return m_hashSize; }
	
	protected void SetAuthResult(String result) { m_authResult = result; }
	protected String GetAuthResult() { return m_authResult; }
	
	protected void SetSrvPayloadSize(int size) { m_srvPayloadSize = size; }
	protected int GetSrvPayloadSize() { return m_srvPayloadSize; }
	
	public AuthenticationManager(){
		super("Authentication Manager");
		SetupModel();
	}

	public AuthenticationManager(String name)
	{
		super(name);
		SetupModel();
	}
	
	private void SetupModel()
	{
		addInport("in_security");
		addInport("in_symmSize");
		addInport("in_asymmSize");
		addInport("in_hashSize");
		addInport("in_authResult");
		addInport("in_srvPayloadSize");
		addInport("in_initId");
		addInport("in_recvId");

		addOutport("out_hashSize");
		addOutport("out_symmSize");
		addOutport("out_asymmSize");
		addOutport("out_srvReq");
		addOutport("out_authType");
		
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
		System.out.println("deltext");
		for (int idx = 0; idx < states.length; idx++) {
			System.out.println("idx: " + idx + " state = " + states[idx].GetState().toString());
			if (states[idx].GetState().toString().equals(phase)) {
				System.out.println("Yes it found IDLE state.");
				String state = states[idx].Deltext(this, e, x);
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
		if (m_nextState.equals(AuthState.SEND_MESSAGE.toString())) {
			//If we're at SEND_MESSAGE, change to authenticated if we just got here.
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
		} else if (m_nextState != null) {
			phase = m_nextState;
			sigma = 1/0.0;
		}
	}
	
	public message out( ){
		message m = null;

		int idx = 0;
		for (idx = 0; idx < states.length; idx++) {
			System.out.println("idx: " + idx + " state = " + states[idx].GetState().toString());
			if (states[idx].GetState().toString().equals(m_nextState)) {
				m = states[idx].GetStateMessageOutput();
				break;
			}
		}
		
		if (idx == states.length) {
			System.out.println("Error! Did not find the output function corresponding to this state!");
		}

		return m;
	}


	
	class State {
		AuthState m_state;
		public State(AuthState state) {m_state = state;}
		AuthState GetState() { return m_state; }
		
		//This method performs what deltext does, however it does not update sigma nor phase.
		//The return value is a string indicative of the next state to advance to.
		String Deltext(AuthenticationManager model, double e, message x) { return null; }
		message GetStateMessageOutput() { return new message(); };
	}
	
	
	enum AuthState {
		IDLE, GET_CERTIFICATE, DECRYPT_CERTIFICATE, GET_CERTIFICATE_HASH, ENCRYPT_ASYMM_RECEIVER_REQUEST, GET_MESSAGE_HASH,
		ENCRYPT_ASYMM_HASH, SEND_MESSAGE, AUTHENTICATE_USER, ENCRYPT_KDC_SESSION_REQUEST, INIT_KDC_SESSION, DECRYPT_KDC_SESSION_KEY, 
		ENCRYPT_RECIPIENT_SESSION_REQUEST, REQUEST_RECIPIENT_SESSION, DECRYPT_RECIPIENT_SESSION_KEY, ENCRYPT_RECIPIENT_REQUEST
	}

	
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
			content payload = makeContent("out_symmSize", new entity(size.toString()));
			m.add(payload);
			return m;
		}
	}
	
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
			m.add(payload);
			return m;
		}
	}
	
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
	class SendMessageState extends State {
		public SendMessageState() { super(AuthState.SEND_MESSAGE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			return AuthState.IDLE.toString();
		}
		//AR: What to send as output in sendMessage?
	}

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
			m.add(contentSizeToEncrypt);
			return m;
		}
	}

	
	class GetMessageHashState extends State {
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
			content contentSizeToEncrypt = makeContent("out_asymmSize", new entity(payload.toString()));
			m.add(contentSizeToEncrypt);
			return m;
		}
	}
	class GetCertificateHashState extends State {
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
					model.SetAsymmSize(payload);
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
	
	class DecryptCertificateState extends State {
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
			m.add(encryptedCert);
			return m;
		}
	}

	class GetCertificateState extends State {
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
	
	class IdleState extends State {
		public IdleState () { super(AuthState.IDLE); }
		
		@Override
		public String Deltext(AuthenticationManager model, double e, message x) {
			model.Continue(e);
			String nextState = null;
			System.out.println("idle: deltext. message size = " + x.size());
			
			String recvId = null;
			String initId = null;
			Integer size = 0;
			entity val = null;
			
			//Parse all inputs.
			for (int idx = 0; idx < x.size(); idx++) {
				if (model.messageOnPort(x, "in_recvId", idx)) {
					System.out.println("receiver ID latched at idx " + idx);
					recvId = x.getValOnPort("in_recvId", idx).toString();
				}
				
				if (model.messageOnPort(x, "in_initId", idx)) {
					System.out.println("initiator ID latched at idx" + idx);
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

			if(model.phaseIs(AuthState.IDLE.toString())){
				System.out.println("idle: in_security port has something");
				Double securityLevel = Double.parseDouble(val.toString());

				System.out.println("idle: about to check security levels");
				if (securityLevel == 3) {
					nextState = AuthState.GET_CERTIFICATE.toString();

				} else if (recvId != null && initId != null && 
						   !recvId.equals("") && !initId.equals("")) {
					System.out.println("checking security level");
					if (securityLevel == 1) {
						nextState = AuthState.ENCRYPT_KDC_SESSION_REQUEST.toString();
						SetInitId(initId);
						SetRecvId(recvId);
						SetSymmSize(size);
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

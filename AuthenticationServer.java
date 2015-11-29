package cse561;

import GenCol.entity;
import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;


public class AuthenticationServer extends ViewableAtomic 
{
	public static final int DefaultCertificateSize = 1024;
	public static final int DefaultSessionKeySize = 1024;
	private double m_delay = 0.1; //0.1 for simplicity sake. Can be expanded in the future for granularity.
	private ServerRequestType m_reqType;
	private Integer m_certPayloadSize;
	private Integer m_sessionPayloadSize;
	
	public AuthenticationServer()
	{
		super("Authentication Server");
		SetupModel(DefaultCertificateSize, DefaultSessionKeySize);
	}
	
	public AuthenticationServer(String name) 
	{
		super(name);
		SetupModel(DefaultCertificateSize, DefaultSessionKeySize);
	}
	
	public AuthenticationServer(String name, int certificateSize, int sessionKeySize) 
	{
		super(name);
		SetupModel(certificateSize, sessionKeySize);
	}

	private void SetupModel(int certificateSize, int sessionKeySize) 
	{
		addInport("in_type");
		addOutport("out_payloadSize");

		addTestInput("in_type", new entity(ServerRequestType.KDC_SESSION.toString()));
		addTestInput("in_type", new entity(ServerRequestType.CERTIFICATE.toString()));
		
		m_certPayloadSize = certificateSize;
		m_sessionPayloadSize = sessionKeySize;
		
		m_reqType = ServerRequestType.NONE;
		phase = "Passive";
		m_delay = 0.1;
	}
	
	public void  deltext(double e, message x)
	{
		Continue(e);

		if(messageOnPort(x, "in_type",0) && phaseIs("Passive")){
			entity val = x.getValOnPort("in_type",0);

			if (val.toString().equals(ServerRequestType.CERTIFICATE.toString())) {
				m_reqType = ServerRequestType.CERTIFICATE;
				
			} else if (val.toString().equals(ServerRequestType.KDC_SESSION.toString())) {
				m_reqType = ServerRequestType.KDC_SESSION;
			}
			
			phase = "Active";
			sigma = m_delay;
		}
	}
	
	public void deltint( )
	{
		m_reqType = ServerRequestType.NONE;
		phase = "Passive";
		sigma = 1/0.0;	//Infinity.
	}
	

	public message out( )
	{
		message m = new message();

		Integer payload = 0;
		
		if (m_reqType == ServerRequestType.CERTIFICATE) {
			payload = m_certPayloadSize;

		} else if (m_reqType == ServerRequestType.KDC_SESSION) {
			payload = m_sessionPayloadSize;
		}

		content size = makeContent("out_payloadSize", new entity(payload.toString()));
		m.add(size);
		
		return m;
	}
	
	public void initialize()
	{
		super.initialize();
	}
}

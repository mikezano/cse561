package cse561;

import GenCol.entity;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Transducer extends  ViewableAtomic
{
	private double m_totalPower;
	private double m_totalSymmCryptoPower;
	private double m_totalAsymmCryptoPower;
	private double m_totalHashPower;
	private double m_totalAuthFactorPower;

	public Transducer() 
	{
		super("Transducer");
		
		TransducerSetup();
	}	
	
	public Transducer(String  name) 
	{
		super(name);
		TransducerSetup();
	}
	
	private void TransducerSetup()
	{
		m_totalPower = 0;
		m_totalSymmCryptoPower = 0;
		m_totalAsymmCryptoPower = 0;
		m_totalHashPower = 0;
		m_totalAuthFactorPower = 0;

		addInport("in_sePower");
		addInport("in_aePower");
		addInport("in_hPower");
		addInport("in_afPower");
	}

	public void initialize()
	{
		super.initialize();
		phase = "active";
		sigma = 100;
	}

	public void  deltext(double e,message  x)
	{
		Continue(e);
		
		entity  val;
		for (int i=0; i< x.size();i++) {
			Double power = 0.0;
			if (messageOnPort(x, "in_sePower", i)) {
				val = x.getValOnPort("in_sePower",i);
				power = Double.parseDouble(val.toString());
				m_totalPower += power;
				m_totalSymmCryptoPower += power;
			}
			
			if (messageOnPort(x, "in_aePower", i)) {
				val = x.getValOnPort("in_aePower",i);
				power = Double.parseDouble(val.toString());
				m_totalPower += power;
				m_totalAsymmCryptoPower += power;
			}
			
			if (messageOnPort(x, "in_hPower", i)) {
				val = x.getValOnPort("in_hPower",i);
				power = Double.parseDouble(val.toString());
				m_totalPower += power;
				m_totalHashPower += power;
			}
			
			if (messageOnPort(x, "in_afPower", i)) {
				val = x.getValOnPort("in_afPower",i);
				power = Double.parseDouble(val.toString());
				m_totalPower += power;
				m_totalAuthFactorPower += power;
			}
		}
	}

	public void  deltint()
	{
		sigma = 1/0.0;
		phase = "passive";
		show_state();
	}
	
	public void  show_state()
	{
		System.out.println("Total power consumed: " + m_totalPower);
		System.out.println("Total power consumed by asymmetric encryption: " + m_totalAsymmCryptoPower);
		System.out.println("Total power consumed by symmetric encryption: " + m_totalSymmCryptoPower);
		System.out.println("Total power consumed by hashing: " + m_totalHashPower);
		System.out.println("Total power consumed by authenticationf actor: " + m_totalAuthFactorPower);
	}
}

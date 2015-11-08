package cse561;


import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import SimpArcMod.genr;
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
		super("Authentication System");
		CoupledModelConstruct();
	}

	private void CoupledModelConstruct() {
		// TODO Auto-generated method stub
		addInport("in");
		addOutport("out");
		
		AuthenticationManager am = new AuthenticationManager();
		AuthenticationFactorManager afm = new AuthenticationFactorManager();
		SymmetricEncryption se = new SymmetricEncryption();
		AsymmetricEncryption ae = new AsymmetricEncryption();
	
		add(am);
		add(afm);
		add(se);
		add(ae);
		
		addTestInput("start",new entity("20"));
		
		
		addCoupling(this, "in", am, "in");
		addCoupling(am, "out", afm, "in");
		addCoupling(afm, "out", this, "out");
		
		addCoupling(am, "out", se, "in");
		addCoupling(am, "out", ae, "in");
		
		
		
	}
	
//	public void  Deltext(double e, message x){
//		System.out.println("hello");
//		double num = getValOnPort
//	}

    /**
     * Automatically generated by the SimView program.
     * Do not edit this manually, as such changes will get overwritten.
     */
    public void layoutForSimView()
    {
        preferredSize = new Dimension(591, 332);
        ((ViewableComponent)withName("Authentication Factor Manager")).setPreferredLocation(new Point(53, 189));
        ((ViewableComponent)withName("Authentication Manager")).setPreferredLocation(new Point(30, 50));
    }
}
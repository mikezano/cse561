package cse561;


import java.awt.Dimension;
import java.awt.Point;

import GenCol.entity;
import SimpArcMod.genr;
import model.modeling.message;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class SimulationSystem extends ViewableDigraph {

	public SimulationSystem(){
		super("Simulation System");
		CoupledModelConstruct();
	}
	public SimulationSystem(String name) {
		super(name);
		CoupledModelConstruct();
	}

	private void CoupledModelConstruct() {
		// TODO Auto-generated method stub
		addInport("in");
		addOutport("out");
		
		Application a = new Application();
		AuthenticationSystem as = new AuthenticationSystem();

		add(a);
		add(as);

		
		addTestInput("start",new entity("20"));
		
		addCoupling(a, "out", as, "in");
	
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
        preferredSize = new Dimension(1137, 532);
        ((ViewableComponent)withName("Application")).setPreferredLocation(new Point(26, 205));
        ((ViewableComponent)withName("Authentication System")).setPreferredLocation(new Point(292, 120));
    }
}

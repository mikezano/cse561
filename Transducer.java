package cse561;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import GenCol.doubleEnt;
import GenCol.entity;


import model.modeling.content;
import model.modeling.message;

import view.modeling.ViewableAtomic;



public class Transducer extends  ViewableAtomic{
	//hash map
	protected Map arrived, solved;
	protected double clock,total_ta;
	public Double count=0.00;
	public double count1m=0.0; //count of the 1M jobs completed
	public double count5m=0.0; //count of the 5M jobs completed
	public double total_1m_ta=0.0;
	public double total_5m_ta=0.0;	

	public Transducer() {
		this("TRANSDUCER");
	}	
	
	public Transducer(String  name){
		super(name);
		addInport("GenOut");
		addInport("arriveAM");
		addInport("arriveSE");
		addInport("arriveAE");
		addInport("arriveHE");
		addInport("arriveKS");
		addInport("arriveCA");
		addInport("solvedAM");
		addInport("solvedSE");
		addInport("solvedAE");
		addInport("solvedHE");
		addInport("solvedKS");
		addInport("solvedCA");		

		arrived = new HashMap();
		solved = new HashMap();
			
		initialize();
 }

	public void initialize(){
		phase = "active";
		sigma = 100;
		clock = 0;
		total_ta = 0;
		super.initialize();
	}

	public void  deltext(double e,message  x){
		
		clock = clock + e;
		System.out.println("e:\t\t" + e);
		System.out.println("clock:\t\t" + clock);
		
		Continue(e);
		
		entity  val;
		/*
		for(int i=0; i< x.size();i++){
			if(messageOnPort(x,"GenOut",i)){
				val = x.getValOnPort("GenOut",i);
				arrived.put(val.getName(),new doubleEnt(clock));//timestamping arrival of time				
				//holdIn("active",1);
			}
			if(messageOnPort(x,"SwOut",i)){
				
				val = x.getValOnPort("SwOut",i);
				count++;

				if(arrived.containsKey(val.getName())){

					entity  ent = (entity)arrived.get(val.getName());
					
					
					doubleEnt  num = (doubleEnt)ent;
					double arrival_time = num.getv(); //get the original arrival time
					
					System.out.println("name:\t\t"+val.getName());
					System.out.println("value:\t\t"+num.getv());					

					double turn_around_time = clock - arrival_time;
					total_ta = total_ta + turn_around_time;
					solved.put(val.getName(), new doubleEnt(clock));
					
					if(val.getName().contains("1M")){
						count1m++;
						total_1m_ta += turn_around_time;
					}
					
					if(val.getName().contains("5M")){
						count5m++;
						total_5m_ta += turn_around_time;
					}
				}
			}
		}*/
	}

	 public void  deltint(){
		 clock = clock + sigma;
		 passivate();
		 show_state();
	 }

	 public message out( ){
		 message  m = new message();
		 content  con1 = makeContent("TA",new entity(" "+compute_TA()));
		 content  con2 = makeContent("out",new entity(count.toString()));
		 content  con3 = makeContent("Thru",new entity(" "+compute_Thru()));
		 m.add(con1);
		 m.add(con2);
		 m.add(con3);
		 return m;
	 }

	 public String computeOneMinPercentOfTA(){
		 if(total_ta == 0)
			 return "0";
		 double percentage = total_1m_ta/total_ta;
		 return new DecimalFormat("#.##").format(percentage * 100);
	 }
	 
	 public String computeFiveMinPercentOfTA(){
		 if(total_ta == 0)
			 return "0";
		 double percentage = total_5m_ta/total_ta;
		 return new DecimalFormat("#.##").format(percentage * 100);
	 }	 
	 
	 
	public double compute_TA(){
		 double avg_ta_time = 0;
		 if(!solved.isEmpty())
			 avg_ta_time = ((double)total_ta)/solved.size();
		 return avg_ta_time;
	}
 
	public double compute_Thru(){
		double thruput = 0;
		if(clock > 0)
			thruput = solved.size()/(double)clock;
		return thruput;
	}

	 public void  show_state(){
		 
		 if (arrived != null && solved != null){
			 
			 System.out.println("clock:\t\t" + clock);
			 System.out.println("jobs arrived:\t\t"  +  arrived.size());
			 System.out.println("jobs solved:\t\t" + solved.size());		 
			 System.out.println("avg ta:\t\t"  +  compute_TA());
			 System.out.println("throughput:\t\t"  +  compute_Thru());
			 System.out.println("1m jobs solved:\t" + count1m);
			 System.out.println("5m jobs solved:\t" + count5m);
			 System.out.println("1m %:\t\t" + computeOneMinPercentOfTA());
			 System.out.println("5m %:\t\t" + computeFiveMinPercentOfTA());
			 System.out.println("On time:\t\t" + total_ta);
			 System.out.println();
		 }
	 }
}

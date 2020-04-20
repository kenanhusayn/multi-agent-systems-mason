package sim.app.firecontrol;

import java.lang.Math;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import sim.util.Double2D;
import sim.util.Double3D;
import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;

// completed!

// communication essential - demo, ok, works !!!
// basically replaces the original idea of DataPacket classes in the original project
// 

// ISSUE: why some bids are skipped when the drones are 15 and more? - - check later!!

// acik atrtirmaya qoydugumuz elementleri sintez edib ayrisdiririq
public class Bidding { // en onemli class, diger partlar bunun uzerine qurulacaq
    
    class Bid { // inside class
        public double bid;
        public double hedefeKadarMesafe; // distance from the accepted task
        public double tempVar; // ...
        public WorldCell cell; // ...
        
        public Bid(double bid, WorldCell cell, double hedefeKadarMesafe) { // sade cons
            this.bid = bid;
            this.cell = cell;
            this.hedefeKadarMesafe = hedefeKadarMesafe;
        }    
		
        public Bid(double bid, WorldCell cell, double hedefeKadarMesafe, double hedefeKadarMesafe2) { // alt alt cons - - DEV!!
            this.bid = bid;
            this.cell = cell;
            this.hedefeKadarMesafe = Math.pow(hedefeKadarMesafe, 2.0) / hedefeKadarMesafe2;
        }
    }    
    
	// credits: copied from stackoverflow... works better than original!
    class TempComparator implements Comparator { // will be used to compare the bids
        @Override
        public int compare(Object o1, Object o2) {
            try {
                Bid bid1 = (Bid) o1; Bid bid2 = (Bid) o2;
                if(bid1.hedefeKadarMesafe == bid2.hedefeKadarMesafe) return 0;
                else if(bid1.hedefeKadarMesafe < bid2.hedefeKadarMesafe) return -1;
                else return 1;
            }
            catch(UnsupportedOperationException e) {
            }
            return -1;
        }
    }

    public UAV teklifci;
    PriorityQueue bids;
    
    public Bidding(UAV teklifci, Task fire) { // sade cons
        this.teklifci = teklifci;
        TempComparator comparator = new TempComparator();
        this.bids = new PriorityQueue(1, comparator); // what is priority right now?
        this.teklifEkle(fire);
    }

    public void teklifEkle(Task fire) { // adds the offer to the memory, offer being task/fire, one at a time
        Double2D varMerk = new Double2D(fire.centroid.x, fire.centroid.y);
        Double2D konum = new Double2D(teklifci.x, teklifci.y);
        
        LinkedList<WorldCell> wcList = new LinkedList<>(fire.cells);
        wcList.removeAll(teklifci.takenWC); // taken cells are extracted from the list
        
        for(WorldCell wcI: wcList) { // inside the remaining tasks
            if(wcI.getType().equals(CellType.FIRE)) { // if the task is burning               
                double varAci = Math.abs(Math.atan2(wcI.y - fire.centroid.y, wcI.x - fire.centroid.x) 
							- Math.atan2(teklifci.y - fire.centroid.y, teklifci.x - fire.centroid.x)); // calculate the angle difference
							// angle between fire center and fire
							// angle between fire center and drone
				
				/*
				
	1) PREFERENCE (casual):
				
				          will die soon------------>|<-----------fire center, save first or last?
													|
													|	
								fire----------->o	o	o<------------ random fire? need extra logic to deal with?
		DRONE --------->							|
								   fire------------>o
							
				
				
	2) HEURISTIC:
	
										```
									  `	    `
				fire cell ---------> `   o   `------
									  `	    `      | utility
									    ``` --------
									  
		
		
	3) PREFERENCE (critical situation):
				
		registered fires in bag = n					registered fires in bag = m
				
				DRONE1 --------->       o               <------------ DRONE2
				|						|								|
				|						|								|
				|<--------------------->|<----------------------------->|				
						dist1							dist2
						
						
						
										LOGICAL ISSUE:		dist2 > dist1 && n >>> m
	
										what if DRONE2 finishes earlier and be closer to the next task (last task?)
										while DRONE1 has accepted it earlier but is busy with previous tasks?
										then DRONE2 will not try to extinguish the task and the whole operation
										will be waiting for the previous drone to come and do its job, this is bad!
										
										PROBLEM: 	with more than 10 drones awarding isn't efficient, needs more control
													perhaps another protocol to consider distance & workload ??? - - DEV!!!


													
				*/
				// here lies the soul of the decision making proccess! 
				
				// below I run some tests and store their results here
				// the formulas I used are stored in a separate file as an archive
				// the variables are indexes, change them to see how it improves!!!
				// evaluate based on mean of steps, store the results and parameters separately for later inspection				
				// test 0: rip!
				// test 1: rip!	
				// test 2: 	randomness is more than expected, not possible to control - mod! curr!
				// mean is: 17025.4
				// test 3: rip!		
				// test 4: rip!	
				// test 5: rip!					
				// test 6: interesting results, can't find the bug??????????????? - - CHECK AGAIN LATER!!!
				// mean is: 38293.6
				// test 7: rip!		
				// test 8: rip!	
				// test 9: can't find the center - - CHECK AGAIN LATER!!!
				// mean is: 36256.86666666667																							
				// test 10: rip!	
			
				// test 11:	stackoverlflow - - works + IMPROVE LATER!!!	
				// works fine with tweaks but sometimes a drone will surpass the fire right
				// in front of it and will go for a far cell, there is a problem with the 
				// heuristic approach, add more control? - - chck later!!!		
				// mean is: 15219.551724137931            				
				//test 12: not bad! add another adjuster?? - - check later!!!
				// mean is: 13023.333333333334							
				// test 13: unrelevant results with more than 6 fires!!! - - check later!!
				// mean is: 12275.9		

				
				// test 14: best one so far!!! - - credits: partially copied from a neural network project
				// problem with more than 20 drones!!! - - improve later!!!
				// mean is: 10418 (2 drones, 3 fires)
				// mean is: 20310 (20 drones, 30 fires) - - accordint to stats, it could have been ~15000
				// add control for crowd behaviour?? - - DEV!!
				// k is really nothing more than an adjuster.
				double k = 3; // patience relay - - might want to add another valve??? - - DEV!!!
				// alfa neuron is responsible for firing when the difference between angles is greater than PI
				double alfa = Math.pow(Math.exp(1.0), -k * (varAci > Math.PI ? Math.abs(varAci - 2.0 * Math.PI) : varAci) / Math.PI) * 10.0;
                // beta neuron is index of adjusted distance between the selected task and the center of the targetted task 
				double betta = Math.pow(Math.exp(1.0), k * (varMerk.distance(new Double2D(wcI.x, wcI.y)) / (fire.radius + 0.01) - 1)) * 10.0;            
                // qamma neuron is index of adjusted distance between the interested agent and the center of the targetted task
				double qamma = Math.pow(Math.exp(1.0), -k * konum.distance(new Double2D(wcI.x, wcI.y)) / (fire.radius * 2 + 0.01));
                // put them in the function e to the power of (a * b * q)
				double delta = Math.pow(Math.exp(1.0), alfa * betta * qamma);
				
				// source: used as an etalon for other measurements
				// source files for heauristic measures are written in c++ & python, import localUtils in py3
				// mean is: 14424.233333333334
				// a better version should work well for both few and many fires variant, test with 3 and 30
				

                Bid bid = new Bid(delta, wcI, konum.distance(new Double2D(wcI.x, wcI.y)));
                bids.offer(bid);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj.getClass().equals(Bidding.class)) {
            Bidding b = (Bidding) obj;
            return b.teklifci == this.teklifci;
        }
        return false;            
    }
}
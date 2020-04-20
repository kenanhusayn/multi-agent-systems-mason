package sim.app.firecontrol;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;
import java.util.Date;
import sim.engine.SimState;
import sim.engine.Steppable;

// completed!
/*
generates a report.txt file in the mason directory and fills it in with some information
example format:
					Timestamp: Mon Sep 03 01:47:32 CEST 2018
					Job: 0  Steps: 5847
					--------------------------------------------------
					... ... ...
*/

public class FireController implements Steppable{
	private static final long serialVersionUID = 1L;
	
	/**
	 * This will check for termination conditions and writes out a file on mason root directory.
	 * TODO: fill the file with information about your simulation according to what you would like to show
	 * in your report
	 */
	@Override
	public void step(SimState state) {
        Ignite ignite = (Ignite) state; // diger versiya islemedi, object ustunden gedirik
		long steps = state.schedule.getSteps();
		long jobs = state.job();
		//create a .txt file where we can store simulation informations
		//if(Ignite.cellsOnFire == 0){//not dynamic, retired
		if(ignite.cellsOnFire() == 0){
			// String fileName = System.getProperty("user.dir") + "/" + System.currentTimeMillis() + ".txt"; // windowsda yavas isleyir, niye?
			String fileName = System.getProperty("user.dir") + "/" + "report.txt"; // eyni fayla yaz, ferqli fayllarda conflict var
			
			try {		
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true))); // same file, no pollution
				out.println("Timestamp: " + (new Date(System.currentTimeMillis()))); // detailed info for c++ statistic		
				out.println("Job: " + jobs + "  Steps: " + Long.toString(steps));				
				out.println("--------------------------------------------------");	
				// out.println(Long.toString(steps)); // for convenience, statistic calc in python								
				out.flush();
				out.close();
			} catch (IOException e) {
				System.err.println("Exception in FireControll.step() " + e.toString());
				e.printStackTrace();
			}
			
			//kill the current job of the simulation
			state.kill();
		}
	}

}

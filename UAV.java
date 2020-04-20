/**
 * A single UAV running over the simulation. 
 * This class implements the class Steppable, the latter requires the implementation 
 * of one crucial method: step(SimState).
 * Please refer to Mason documentation for further details about the step method and how the simulation
 * loop is working.
 * 
 * @author dario albani
 * @mail albani@dis.uniroma1.it
 * @thanks Sean Luke
 */
 
 // completed!
 
 /*
 ** ** ** Ultimate TODO list:
 ** 
 ** ** to be implemented:
 ** * utility step function solve ans calculate										+
 ** * selectTask - get ehe discretelocation and calcualte based on it				+
 ** * bidding is inside function													-
 ** * use flooding strategy - Dario suggested in the original file					?
 ** * consider the conlfict when receiving the information - double check			+
 ** * bilgiGonder will be added, sendPacket - complementary							+
 ** * hucreTransPaket will be added, receiveData  - complementary 					+
 **
 ** ** to be modified:
 ** * important how we get the location, use function				+
 ** * AWARDING: remove all the elements we visited already			+
 ** * ANNOUNCEMENT: double check the added elements					+
 ** * EXPEDITING: receiveData - use a better approach				-
 ** * EXPEDITING: can be merged? - - DEV!!							?
 **
 ** ** Misc:
 ** * clean up the code						-
 ** * better comments						+
 ** * eliminate the useless variables		-
 ** * use static vars						-
 ** * translate the vars					-
 **
 ** ** Bugs:
 ** * agents extinguish the fires nearby (reported by Dario @ 1. check)				+	fixed @ function nextAction and adjustments made in other files as well
 */   
package sim.app.firecontrol;

import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int3D;

public class UAV implements Steppable {
    private static final long serialVersionUID = 1L;

    // Agent's variable 
    public int id; //unique ID
    public double x; //x position in the world
    public double y; //y position in the world
    public double z; //z position in the world
    public Double3D target; //UAV target
    public AgentAction action; //last action executed by the UAV: SELECT_TASK, SELECT_CELL, MOVE, EXTINGUISH
    public static double communicationRange = 30; //communication range for the UAVs
    public static double gorusAlani = 5; // sonradan sahaya hakim olmak uzere kullanilacak - - DEV!!

    // Agent's local knowledge 
    public Set<WorldCell> takenWC;
    public Task myTask;
    public Packet gorevDos; // packet that gives information about the task
    public DataPacket hucreDos; // datapacket that gives information about the cells
	public double hafizaBoyutu; // memory  - - DEV!!
    public static double linearvelocity = 0.02;
    public static int stepToExtinguish = 10;
    private int startedToExtinguishAt = -1;

    public UAV(int id, Double3D myPosition) {
        //set agent's id
        this.id = id;
        //set agent's position
        this.x = myPosition.x;
        this.y = myPosition.y;
        this.z = myPosition.z;
        //at the beginning agents have no action
        this.action = null;
        //at the beginning agents have no known cells 
        this.takenWC = new LinkedHashSet<>();
    }

    // DO NOT REMOVE
    // Getters and setters are used to display information in the inspectors
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    /**
     *  Do one step.
     *  Core of the simulation.   
     */
    public void step(SimState state) {
        Ignite ignite = (Ignite)state; // get the Ignite object to pass in functions later

        //select the next action for the agent
        AgentAction a = nextAction(ignite);
        
        switch(a) {
        case SELECT_TASK:
            selectTask(ignite); // pass Ignite
            
            this.action = a;
            break;

        case SELECT_CELL:
            selectCell(ignite); // pass Ignite
			// alternativeCell(ignite); // want to change the approach how we select the cell - - DEV!!
            break;

        case MOVE:
            move(state); // pass State
			WorldCell dh = (WorldCell)ignite.forest.field[(int) this.target.x][(int) this.target.y]; // where we're going?
			if(!dh.type.equals(CellType.FIRE)) this.target = null; // we are at the target? then don't run to target anymore
			break;

        case EXTINGUISH:
            if(extinguish(ignite)) {
                Int3D dLoc = ignite.air.discretize(new Double3D(this.x, this.y, this.z)); // where am I?
                ((WorldCell)ignite.forest.field[dLoc.x][dLoc.y]).extinguish(ignite); // ~~~
                this.target = null; // ... 
                this.hucreDos = null; // reset the packet, so we can get a new one
            }

            this.action = a; // ~~~
            break;

        default:	
            System.exit(-1);
        }
    }
    
    private AgentAction nextAction(Ignite ignite){ // might wanna improve the logic? - - DEV!!
        //if I do not have a task I need to take one
        if(this.myTask == null) {
            return AgentAction.SELECT_TASK;
        }
        //else, if I have a task but I do not have target I need to take one
        else if(this.target == null) {
            return AgentAction.SELECT_CELL;
        }
        //else, if I have a target and task I need to move toward the target
        //check if I am over the target and in that case execute the right action;
        //if not, continue to move toward the target
		// fix for bug number 1
        // else if(this.target.equals(ignite.air.discretize(new Double3D(x, y, z)))) { // seemingly discretizing the coordinate causes mess
																					   // agents start to extinguish fires they are not over
																					   // however it would be much efficient to use discretelocation
																					   // since it is much faster - memory wise . . .
 
		else if(this.target.equals(new Double3D(x, y, z))){ // find a way to use discretelocation without messing things up - - DEV!!

            //if on fire then extinguish, otherwise move on
            WorldCell cell = (WorldCell)ignite.forest.field[(int) x][(int) y];
			// this.knownCells.add(cell); // added

            if(cell.type.equals(CellType.FIRE)) {
                return AgentAction.EXTINGUISH;
            }
            else {
                this.target = null; // added
                return AgentAction.SELECT_CELL;
            }

        }
        else {
            return AgentAction.MOVE;
        }
    }

    /**
     * Take the centroid of the fire and its expected radius and extract the new
     * task for the agent.
     */
    private void selectTask(Ignite ignite) { // now this one matters the most!!! mod after test 4 && 5! mod after test 10!  - - fine! - - DEV!!
        Task newTask = null; // reset the task, we just/again started
        if(this.gorevDos == null) { // if we are being lazy around ...
            Packet packet = new Packet(this, Protocol.ANNOUNCEMENT, ignite.UAVs, ignite.tasks); // get the packet
            receiveData(packet); // process it
            packet = new Packet(this, Protocol.AWARDING, ignite.UAVs, null); // will be expecting the award
            double sebOlcu = this.gorevDos.payload.bids.size(); // how many bids
            int[] goreBasinaDron = new int[ignite.tasks.size()]; // how many tasks
            for(Bid bid: this.gorevDos.payload.bids) { // ...
                UAV bidder = bid.teklif; // bidder is the new agent
                double pTemp = 0; // temp var for switching/updating the heuristic
                int secilenGor = 0; // how many tasks are to be chosen 
                for(int i = 0; i < ignite.tasks.size(); i++) { // ...
                    Task task = ignite.tasks.get(i); // determine the task 
					gorusAlani++; // probably not a good idea!  DEV!!
					// heuristic mainly depends on: how many offers and candidates do I have given the selected fire
                    double heur = (bid.bids.get(i) + (1 - (goreBasinaDron[i] / sebOlcu))) / 2;

                    if(heur >= pTemp) { // if it is positive, meaning we go forward
                        Set<WorldCell> ii = task.findBurningFires(); // mark the fire
                        ii.removeAll(this.takenWC); // elde olanlari bosalt
                        if(ii.size() > 0) { // if it is still alive
                            pTemp = heur; // will update the heuristic in the next iteration
                            secilenGor = i;
							gorusAlani = 0;
                        }
                    }
                }
                goreBasinaDron[secilenGor]++; // if everything goes well, get more tasks
                Award odul = new Award(bidder, ignite.tasks.get(secilenGor)); // what will be the award
                packet.payload.awards.add(odul); // add it to the bag
            }
            receiveData(packet); // ~~~ 
        }
        for(Award odul: this.gorevDos.payload.awards) // I am seeing my options based on who is paying the best
            if(odul.acikart == this) // ...
                newTask = odul.givenTask; // new task is determined 
        try {
            Set<WorldCell> pakHucreler = newTask.findBurningFires();
            if(pakHucreler.size() > 0) { // if the fire is still alive
                this.myTask = newTask;
                
                Double2D nerdeyim = new Double2D(x, y); // where am I? 
                WorldCell meyyor = null; // what is the most probable world cell to take
                double pTemp = 0.0; // above procedure is repeated again - - DEV!!
                
                for(WorldCell jj: pakHucreler) {
					// heuristic is based on the distance between the agent and the cell - - not the best approach - - improve later!!
					// first would be better? - - DEV!!
                    // double heur = 1 - (nerdeyim.discretize(new Double2D(this.x, this.y)) / Math.sqrt(2.0 * Math.pow(ignite.height, 2.0))); ;
                    double heur = 1 - (nerdeyim.distance(new Double2D(jj.x, jj.y)) / Math.sqrt(2.0 * Math.pow(ignite.height, 2.0))); ;
                    
                    if(heur >= pTemp) {	// if heuristicly better
                        meyyor = jj; //then the most probable cell to choose is this
                        pTemp = heur; // to be updated
                    }
                }
                this.target = new Double3D(meyyor.x, meyyor.y, z);
                this.takenWC.add(meyyor); // add the targe based on who is best 
            }
            else {
                this.myTask = null; // if else . . . 
            }
        } catch(NullPointerException e) {
            System.err.println("Something is null, have you forgetten to implement some part?");
        }
    }

    /**
     * Take the centroid of the fire and its expected radius and select the next 
     * cell that requires closer inspection or/and foam. 
     */
	 // basically we select the cell the same way we selected the tasks
    private void selectCell(Ignite ignite) { // now this one matters alot!!! mod after test 4! mod after test 11!  - - ok! - - DEV!!
        DataPacket packet = new DataPacket(this, this.myTask, Protocol.ANNOUNCEMENT, ignite.UAVs); // get the announcement...
        hucreTransPaket(packet); // send the packet
        packet = new DataPacket(this, this.myTask, Protocol.AWARDING, ignite.UAVs); // process the packet
        packet.tumSatHucEkle(this.takenWC); // add the cells to the bag
        for(Bidding bid: this.hucreDos.payload.bids) { // ...
            UAV bidder = bid.teklifci; // same aproach as above
            double tamHeur = 0; // heuristic is reset
            Set<Bidding.Bid> uygunElem = new LinkedHashSet<>(); // uygun elementler listesi hazirlaniyor    
            int uygunOlcu = 0; // attempts
            while(!bid.bids.isEmpty() && uygunOlcu < 6) { // - - adjust the number of attempts basedon the performance - - DEV!
                Bidding.Bid hucreDos = (Bidding.Bid) bid.bids.poll(); // get the data from the pool
                if(!packet.payload.takenWC.contains(hucreDos.cell)) { // if we don't have it yet
                    uygunElem.add(hucreDos); // we add it here
                    uygunOlcu++; // and update/adjust
					tamHeur += hucreDos.bid; // heuristic update/adjust
                }
            }            
            if(uygunElem.size() > 0) {
                Random rand = new Random();// ~~~
                double cntr = 0; // ~~~
                WorldCell ihtiyariHucre = null; // ~~~
                
                Iterator<Bidding.Bid> ij = uygunElem.iterator();
                while(ij.hasNext()) {
                    Bidding.Bid hucreDos = ij.next();

                    cntr += hucreDos.bid; // count the bids

                    if(cntr >= tamHeur * rand.nextDouble()) {
                        ihtiyariHucre = hucreDos.cell; // if we have a lot to take, add it too
                        break;
                    }
                }
                
                Allocate odul = new Allocate(bidder, ihtiyariHucre); // define the prize
                packet.payload.awards.add(odul); // add it to the bag
                packet.satHucEkle(ihtiyariHucre); // and add the extra one we just took
            }
        }
        
        hucreTransPaket(packet); // and send the packet!!!
		
        if(this.target == null) { // might wanna consider another control statement for accuracy!! - - DEV!!
            this.myTask = null; // we reached, reset everything . . . 
            this.gorevDos = null; // . . . 
            this.hucreDos = null; // . . . 
        }
    }

    /**
     * Move the agent toward the target position
     * The agent moves at a fixed given velocity
     * @see this.linearvelocity
     */
    public void move(SimState state) {
        Ignite ignite = (Ignite) state;

        // retrieve the location of this 
        Double3D location = ignite.air.getObjectLocationAsDouble3D(this);
        double myx = location.x;
        double myy = location.y;
        double myz = location.z;

        // compute the distance w.r.t. the target
        // the z axis is only used when entering or leaving an area
        double xdistance = this.target.x - myx;
        double ydistance = this.target.y - myy;

        if(xdistance < 0)
            myx -= Math.min(Math.abs(xdistance), linearvelocity);
        else
            myx += Math.min(xdistance, linearvelocity);

        if(ydistance < 0) { 
            myy -= Math.min(Math.abs(ydistance), linearvelocity);
        }
        else {	
            myy += Math.min(ydistance, linearvelocity);
        }

        // update position in the simulation
        ignite.air.setObjectLocation(this, new Double3D(myx, myy, myz));
        // update position local position
        this.x = myx;
        this.y = myy;
        this.z = myz;
    }

    /**
     * Start to extinguish the fire at current location.
     * @return true if enough time has passed and the fire is gone, false otherwise
     * @see this.stepToExtinguish
     * @see this.startedToExtinguishAt
     */
    private boolean extinguish(Ignite ignite) {
        if(startedToExtinguishAt==-1) {
            this.startedToExtinguishAt = (int) ignite.schedule.getSteps();
        }
        //enough time has passed, the fire is gone
        if(ignite.schedule.getSteps() - startedToExtinguishAt == stepToExtinguish) {
            startedToExtinguishAt = -1;
            return true;
        }		
        return false;
    }
    
    public boolean isInCommunicationRange(Double3D otherLoc) {
        Double3D nerdeyim = new Double3D(x, y, z);
        return nerdeyim.distance(otherLoc) <= UAV.communicationRange;
    }

    /**
     * COMMUNICATION
     * Send a message to the team
     */
	 // below communication functions use alike algorithms with modificatoons 
	 // for either sending or receiving packets, awards and elements
    public void sendPacket(Packet packet) { // might consider embeeding inside the protocol? - - bad idea!!!
        switch(packet.getType()) {
			// basically we see what is the type of the packet and proceed based on that
            case ANNOUNCEMENT: {
                this.gorevDos = new Packet(this, packet); // ~~~
                this.gorevDos.header.setOfUAVs.add(this); // ~~~
                Set yanYore = civardakiElem(this.gorevDos.payload.UAVs); // who is close to me? / isInCommunicationRange 
																		 // in case nobody is around me or my communication range is 
																		 // too tight, I should consider another logic
                Set ziyarEtdikmi = this.gorevDos.header.setOfUAVs;
                yanYore.removeAll(ziyarEtdikmi); // remove this!! - - test? - ok!
                while(!yanYore.isEmpty()) { // if I am not alone:
                    Iterator<UAV> iij = yanYore.iterator(); // ~~~
                    UAV almayiBekleyen = iij.next(); // 
                    yanYore.remove(almayiBekleyen); // extract from the bag
                    almayiBekleyen.receiveData(this.gorevDos); // and it starts to work too
                    yanYore.removeAll(ziyarEtdikmi); // and extract all the others - - might wanna keep the log at least? - - DEV!!
                }
                this.gorevDos.teklifEkle();
                this.gorevDos.header.protocol = Protocol.BIDDING; // change the protocol
                if(!this.equals(packet.header.talker)) { // ...
                    UAV almayiBekleyen = packet.header.talker; // now I am the talker
                    almayiBekleyen.receiveData(this.gorevDos); // and I receive the task
                }
                break;
            }
            case AWARDING: {
                this.gorevDos = new Packet(this, packet); // same procedure with different protocols
                this.gorevDos.header.setOfUAVs.add(this); // ~~~
                Set yanYore = civardakiElem(this.gorevDos.payload.UAVs);
                Set ziyarEtdikmi = this.gorevDos.header.setOfUAVs;
                yanYore.removeAll(ziyarEtdikmi); 
                while(!yanYore.isEmpty()) { 
                    Iterator<UAV> ijj = yanYore.iterator();
                    UAV almayiBekleyen = ijj.next();
                    yanYore.remove(almayiBekleyen);
                    almayiBekleyen.receiveData(this.gorevDos);
                    yanYore.removeAll(ziyarEtdikmi);
                }   
                this.gorevDos.header.protocol = Protocol.EXPEDITING; // start the expedition
                if(!this.equals(packet.header.talker)) { // same ...
                    UAV almayiBekleyen = packet.header.talker; // same ...
                    almayiBekleyen.receiveData(this.gorevDos); // same ...
                }
                break;
            }
            case EXPEDITING: { // - - DEV!!
                this.hucreDos.tumZiyEkle(packet.header.setOfUAVs); // - - might not wanna add everything? eliminate what we don't need? - - DEV!!
                break;
			}
            default: {
                System.exit(-1);
            }
        }
    }
    
    /**
     * COMMUNICATION
     * Send a message to the team
     */
    public void bilgiGonder(DataPacket packet) { // same approach, few modifications to send the message
        switch(packet.getType()) {
            case ANNOUNCEMENT: { //  same procedure
                this.hucreDos = new DataPacket(this, packet); // ~~~
                this.hucreDos.header.setOfUAVs.add(this); // ~~~
                Set yanYore = civardakiElem(this.hucreDos.payload.UAVs); // ~~~
                Set ziyarEtdikmi = this.hucreDos.header.setOfUAVs; // ~~~
                yanYore.removeAll(ziyarEtdikmi); // ~~~
                while(!yanYore.isEmpty()) { // ~~~
                    Iterator<UAV> iijj = yanYore.iterator();
                    UAV almayiBekleyen = iijj.next(); // . . . 
                    yanYore.remove(almayiBekleyen);
                    almayiBekleyen.hucreTransPaket(this.hucreDos);
                    yanYore.removeAll(ziyarEtdikmi); // . . . 
                }
                if(packet.header.fire == this.myTask && this.target == null) this.hucreDos.teklifEkle(); // I have a task but no target, see the original logic above 
                this.hucreDos.header.protocol = Protocol.BIDDING; // new protocol
                if(!this.equals(packet.header.talker)) { // ~~~
                    UAV almayiBekleyen = packet.header.talker; // ~~~
                    almayiBekleyen.hucreTransPaket(this.hucreDos); // ~~~
                } 
                break;
            }
            case BIDDING: {
                // this.gorevDos.tumTeklEkle(packet.payload.bids);
                this.gorevDos.tumZiyEkle(packet.header.setOfUAVs); // . . . - - DEV!!
                break;
			}
            case AWARDING: { // same procedure
                this.hucreDos = new DataPacket(this, packet);
                this.hucreDos.header.setOfUAVs.add(this);
                Set yanYore = civardakiElem(this.hucreDos.payload.UAVs);
                Set ziyarEtdikmi = this.hucreDos.header.setOfUAVs;
                yanYore.removeAll(ziyarEtdikmi);
                while(!yanYore.isEmpty()) { // ~~~
                    Iterator<UAV> iji = yanYore.iterator(); // ~~~
                    UAV almayiBekleyen = iji.next();
                    yanYore.remove(almayiBekleyen);
                    almayiBekleyen.hucreTransPaket(this.hucreDos);
                    yanYore.removeAll(ziyarEtdikmi);
                }
                this.hucreDos.header.protocol = Protocol.EXPEDITING; // ~~~
                if(!this.equals(packet.header.talker)) { // ~~~
                    UAV almayiBekleyen = packet.header.talker;
                    almayiBekleyen.hucreTransPaket(this.hucreDos); // check status at the other end? - - DEV!!
                }
                break;
            }
			case EXPEDITING: { // ~~~
                this.hucreDos.tumZiyEkle(packet.header.setOfUAVs); // ~~~
                break;
			}
            default: {
                System.exit(-1);
            }
        }
    }

    /**
     * COMMUNICATION
     * Receive a message from the team
     */
	 // same with the previous one with few modifications
    public void hucreTransPaket(DataPacket packet) { // complementary - - needs improvement - - DEV!!
        switch(packet.getType()) {
            case ANNOUNCEMENT: {  // ~~~
                this.bilgiGonder(packet); // . . .
                break;
            }
            case BIDDING: { // ~~~
                this.hucreDos.tumTeklEkle(packet.payload.bids); // . . .
                this.hucreDos.tumZiyEkle(packet.header.setOfUAVs); // . . .
                break;
            }
            case AWARDING: { // ~~~
                if(this.target == null) {
                    for(Allocate odul: packet.payload.awards) { // ~~~
                        if(odul.xerrac == this) { // . . . 
                            if(odul.givenCell != null) { 
                                this.target = new Double3D(odul.givenCell.x, odul.givenCell.y, z); // . . . 
                            }
                            break;
                        }
                    }
                }                
                this.takenWC.addAll(packet.payload.takenWC);
                this.bilgiGonder(packet); // same
                break;
            }
            case EXPEDITING: { // ~~~
                this.hucreDos.tumZiyEkle(packet.header.setOfUAVs); // same
                break;
            }
            default: {
                System.exit(-1);
            }
        }
    }
	
    /**
     * COMMUNICATION
     * Receive a message from the team
     */
    public void receiveData(Packet packet) { // simplified aproach  - - same idea though - - ok!
        switch(packet.getType()) {
			// receiving the packet based on the given protocol, see the Packet class for functions to add the elements 
            case ANNOUNCEMENT: {
                this.sendPacket(packet); //  we are announcing/sending the info/msg
                break;
            }
            case BIDDING: {
                this.gorevDos.tumTeklEkle(packet.payload.bids); // we are bidding/adding bids
                this.gorevDos.tumZiyEkle(packet.header.setOfUAVs); // and agents too
                break;
            }
            case AWARDING: {
                this.sendPacket(packet); // we are awarding - - DEV!!
                break;
            }
            case EXPEDITING: {
                this.gorevDos.tumZiyEkle(packet.header.setOfUAVs); // might not need it - - DEV!!
                break;
            }
            default: {
                System.exit(-1);
            }
        }
    }
    
    public Set civardakiElem(Set UAVs) { // see who is around you
        Set ds = new LinkedHashSet(); // ...
        for(Object object : UAVs) { // iterate
            UAV d = (UAV) object; // ...
            if(isInCommunicationRange(new Double3D(d.x, d.y, d.z)))ds.add(d); // keep a weather eye on the horizon for a new friend
			}
        return ds; // return the friend
    }    
	
	public Set civardakiAskr(Set UAVs) { // the ones who are not - - DEBUG! - - DEV!!!
        Set ds = new LinkedHashSet();
        for(Object object : UAVs) {
            UAV d = (UAV) object;
            if(!isInCommunicationRange(new Double3D(d.x, d.y, d.z)))ds.add(d); // ... !!!
			}
        return ds; // ...
    }
	
    /**
     * COMMUNICATION
     * Retrieve the status of all the agents in the communication range.
     * @return an array of size Ignite.tasks().size+1 where at position i you have 
     * the number of agents enrolled in task i (i.e. Ignite.tasks().get(i)). 
     * 
     * HINT: you can easily assume that the number of uncommitted agents is equal to:
     * Ignite.numUAVs - sum of all i in the returned array
     */
    public int[] retrieveAgents(Ignite ignite) {
        int[] status = new int[ignite.tasks.size()];

        for(Object obj : ignite.UAVs) { //count also this uav
            UAV other = (UAV) obj;
            if(isInCommunicationRange(new Double3D(other.x, other.y, other.z))) {
                Task task = other.myTask;
                if(task != null)
                    status[ignite.tasks.indexOf(task)]++;
            }
        }

        return status;
    }

    @Override
    public boolean equals(Object obj) {
        UAV uav = (UAV) obj;
        return uav.id == this.id;
    }

    @Override
    public String toString() { 
        return id+"UAV-"+x+","+y+","+z+"-"+action;
    } 	
}



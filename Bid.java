package sim.app.firecontrol;

import java.lang.Math;
import java.util.LinkedList;
import sim.util.Double2D;
import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;

// completed!

// communication essential - demo, ok, works !!!
// basically replaces the original idea of DataPacket classes in the original project
// complementary to Bidding class, handles the adding and removing the bids from the memory

public class Bid {
	
	public LinkedList<Double> bids; // to hold the bids in a list
    public UAV teklif; // ...
	public int tempST; // store the ticks - DEV!
	
    public Bid(UAV teklif, LinkedList<Task> tasks) { // sade cons
        this.teklif = teklif;
        this.bids = new LinkedList<>();
        this.teklifEkle(tasks);
    }
	
    public void BidRans(UAV teklif, LinkedList<Task> tasks) { // alt cons
        this.teklif = teklif;
        this.bids = new LinkedList<>();
        this.teklifCikar(tasks);
    }
	
	// given an offer, we add the offer to the list
    public void teklifEkle(LinkedList<Task> tasks) {
        Double2D konum = new Double2D(teklif.x, teklif.y);
        for(Task task: tasks) {
			double tek = konum.distance(task.centroid);
			tek /= Math.sqrt(Math.pow(Ignite.height, 2) + Math.pow(Ignite.width, 2));
			tek = 1 - tek;
            bids.add(tek);
        }
    }
	
	// given an offer, we remove the offer from the list
	// in case of conflict, will be used to remove the bid - - DEV!!
    public void teklifCikar(LinkedList<Task> tasks) {
        Double2D konum = new Double2D(teklif.x, teklif.y);
        for(Task task: tasks) {
			double tek = konum.distance(task.centroid);
			tek /= Math.sqrt(Math.pow(Ignite.height, 2) + Math.pow(Ignite.width, 2));
			tek = 1 - tek;
            bids.remove(tek);
        }
    }
}
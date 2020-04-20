/*
 * Simple structure for a data packet.
 * 
 * @author dario albani
 * @mail dario.albani@istc.cnr.it
 */

 
 // completed!
 
 // basically a copy of DataPacket class with few modifications!! - - merge?? - - DEV!
 
package sim.app.firecontrol;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import sim.util.Bag;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

// communication essential - demo, ok, works !!!

// tasks are processed separately
// previous version considered the offer and award together - failed - this version fixes it - - improve later!!

public class Packet {

    public class Header {
		
        public Protocol protocol; // ...
		public Set<UAV> setOfUAVs;
        public UAV talker; // talker - - > listener
        public int importance;
        
        public Header(UAV talker, Protocol protocol) {
            this.setOfUAVs = new LinkedHashSet();
			this.talker = talker; 
            this.protocol = protocol;
        }
        
        public Header(UAV talker, Header header) {
			this.setOfUAVs = new LinkedHashSet(header.setOfUAVs);
			this.talker = talker;
            this.protocol = header.protocol;
        }   
		
        public Header(UAV talker, Header header, int importance) {
            this.talker = talker;
            this.setOfUAVs = new LinkedHashSet(header.setOfUAVs);
            this.protocol = header.protocol;
			this.importance = importance; // how important is the message? - - DEV!!
        }
    };

    public class Payload {
        
		public LinkedList<Task> tasks;								
        public Set<UAV> UAVs;
        public Set<Award> awards;
        public Set<Bid> bids;
        
        // public Set<Tasks> gorevler; // No? - - not considering the tasks
									   // might consider if use separated classes? - - DEV!!
        
        public Payload(Bag UAVs, LinkedList<Task> tasks) { // ...
            this.UAVs = new LinkedHashSet(UAVs);
            this.awards = new LinkedHashSet<>();
            this.bids = new LinkedHashSet<>();
            
            if(tasks == null) this.tasks = null;
            else this.tasks = new LinkedList(tasks);
        }
        
        public Payload(Payload payload) { // ...
            this.UAVs = new LinkedHashSet(payload.UAVs);
			this.awards = new LinkedHashSet(payload.awards);
            this.bids = new LinkedHashSet(payload.bids);

            if(payload.tasks == null) this.tasks = null;
            else this.tasks = new LinkedList<>(payload.tasks);
        }
    };

    public Header header;
    public Payload payload;
    
    public Packet(UAV talker, Protocol protocol, Bag UAVs, LinkedList<Task> tasks) {
        this.header = new Header(talker, protocol);
        this.payload = new Payload(UAVs, tasks);
    }
    
    public Packet(UAV talker, Packet packet) {
        this.header = new Header(talker, packet.header);
        this.payload = new Payload(packet.payload);
    }
    
	// cloned from the DataPacket class, modified for need
	// mod!!!
    public void teklifEkle() { // teklifi pakete ekle, hazirlama merhalesi
        this.payload.bids.add(new Bid(this.header.talker, this.payload.tasks));
    }
    
    public void tumTeklEkle(Set<Bid> teks) { // prepare ...
        this.payload.bids.addAll(teks);
    }
    
    public void tumZiyEkle(Set ziyt) { 
        this.header.setOfUAVs.addAll(ziyt);
    }    
	
    public void tumExpDisla(Set ted, Set<Bid> a) {
        this.header.setOfUAVs.addAll(ted);
		this.payload.bids.addAll(a);
    }   
	
    public void tekElemDisla(Set ted, int cnt) {
        this.header.setOfUAVs.addAll(ted);
		for (int i=0; i<cnt; i++){
			continue;
		}		
    }  
	
    public void genelElemAl(Set gea) { // complementary
        this.header.setOfUAVs.addAll(gea);
		// this.??? // taski buraya getir!
    }
    
    public Protocol getType() {
        return this.header.protocol;
    }
    
}

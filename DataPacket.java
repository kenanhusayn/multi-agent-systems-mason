package sim.app.firecontrol;

import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;import java.util.LinkedHashSet;
import java.util.Set;
import sim.util.Bag;
import java.util.Date;
import java.util.Iterator;


// completed!
// basically replaces the original idea of DataPacket classes in the original project
// inspired from the example Dario sent and the tutorials
// no need a timestamp, using unique variables? - - DEV!!
// class inside class - stackoverflow...

public class DataPacket {

    public class Header {
        
        public UAV talker; // listener - talker communication, for a continuous comm.
        public Task fire;
        public Set<UAV> setOfUAVs; // store the agents in the memory
        public Protocol protocol; // which protocol are we using? See Protocol class.
		int importance; // variable to handlethe proirity of the operations
        
        public Header(UAV talker, Task fire, Protocol protocol) { // sade h cons
            this.talker = talker;
            this.fire = fire;
            this.setOfUAVs = new LinkedHashSet<>();
            this.protocol = protocol;
        }
        
        public Header(UAV talker, Header header) { // alt h -> h cons
            this.talker = talker;
            this.fire = header.fire;
            this.setOfUAVs = new LinkedHashSet(header.setOfUAVs);
            this.protocol = header.protocol;
        } 
		
        public Header(UAV talker, Header header, int importance) { // alt h -> h cons
            this.talker = talker;
            this.fire = header.fire;
            this.setOfUAVs = new LinkedHashSet(header.setOfUAVs);
            this.protocol = header.protocol;
			this.importance = importance;
        }
    };

    public class Payload { // try with separate classes - - internally bounded classes? - - DEV!
        
        public Set<UAV> UAVs; // ...
        
        public Set<Bidding> bids; // total bids
        
        public Set<Allocate> awards; // total awards
        public Set<WorldCell> takenWC; // given world cell? - - what about taking one from the total?  - - DEV!
        
        
        public Payload(Bag UAVs) { // loading ...
            this.UAVs = new LinkedHashSet(UAVs);
			this.awards = new LinkedHashSet<>();
            this.bids = new LinkedHashSet<>();
            this.takenWC = new LinkedHashSet<>();
        }
        
        public Payload(Payload payload) { // another approach - - DEV!!
            this.UAVs = new LinkedHashSet(payload.UAVs);
			this.awards = new LinkedHashSet(payload.awards);
            this.bids = new LinkedHashSet(payload.bids);
            this.takenWC = new LinkedHashSet(payload.takenWC);
        }
    };
	
	// from tuts, works with few tweaks!
    public Header header;
    public Payload payload;
    
    public DataPacket(UAV talker, Task fire, Protocol protocol, Bag UAVs) { // h
        this.header = new Header(talker, fire, protocol);
        this.payload = new Payload(UAVs);
    }
    
    public DataPacket(UAV talker, DataPacket packet) { // h -> h
        this.header = new Header(talker, packet.header);
        this.payload = new Payload(packet.payload);
    }
    
	// set of setters
	
    public void teklifEkle() { // add single  offer
        this.payload.bids.add(new Bidding(this.header.talker, this.header.fire));
    }
    
    public void tumTeklEkle(Set<Bidding> tte) { // add multiple offers
        this.payload.bids.addAll(tte);
    }
    
    public void tumZiyEkle(Set<UAV> tze) { // add multiple agents - - DEV!!
        this.header.setOfUAVs.addAll(tze);
    }
    
    public void satHucEkle(WorldCell she) { // add single task - - DEV!!
        this.payload.takenWC.add(she);
    }
    
    public void tumSatHucEkle(Set<WorldCell> tshe) { // add multiple tasks
        this.payload.takenWC.addAll(tshe);
    }

		
    public void tumExpDisla(Set ted, Set<Bidding> a) { // complementary to tumSatHucEkle() - - try!
        this.header.setOfUAVs.addAll(ted);
		this.payload.bids.addAll(a);
    }   
	
    public void tekElemDisla(Set ted, int cnt) { // brute force add the agents? - DEV!
        this.header.setOfUAVs.addAll(ted);
		for (int i=0; i<cnt; i++){
			// this.payload.bids.addAll(???);
		}		
    }  
	
    public void genelElemAl(Set gea) { // - - DEV!!
        this.header.setOfUAVs.addAll(gea);
		// this.??? // taski buraya getir!
    }
    
    public Protocol getType() {
        return this.header.protocol;
    }
    
}
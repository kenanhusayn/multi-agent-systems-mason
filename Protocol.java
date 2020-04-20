package sim.app.firecontrol;

// nothing to do here...

// as requested in the homework

/*
A working implementation of CNP requires to implement the four main
stages (remember that they take place all in one step of the simulation):
*/

public enum Protocol {
	// a. Announcement: Manager sends a task description to all possible suppliers
	ANNOUNCEMENT, 
	
	// b. Bidding: Suppliers evaluate the offer and send a proposal to the Manager
	BIDDING, 
	
	// c. Awarding: Manager allocates the contract to the best supplier
	AWARDING, 
	
	// d. Expediting: The chosen supplier replies positively or negatively to the Manager 
	EXPEDITING
}

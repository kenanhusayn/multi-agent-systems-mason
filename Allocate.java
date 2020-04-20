package sim.app.firecontrol;

// completed!

// communication essential - demo, ok, works !!!
// basically replaces the original idea of DataPacket classes in the original project
// given a cell, we will see which drone is assigned to that cell
public class Allocate {
	
	public WorldCell givenCell; // ...
    public UAV xerrac; // ...
	public Task verilenGor; // ...

    public Allocate(UAV xerrac, WorldCell givenCell) { // sade cons
		this.xerrac = xerrac;
        this.givenCell = givenCell;
    }
	// alternativ olaraq taski ve celli nezere alaraq vezife bolgusu aparilir
    public void AllocateRans(UAV xerrac, WorldCell givenCell, Task verilenGor) { // alt cons
		this.xerrac = xerrac;
        this.givenCell = givenCell;
		this.verilenGor = verilenGor;
    }
}
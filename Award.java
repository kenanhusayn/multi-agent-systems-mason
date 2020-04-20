package sim.app.firecontrol;

//completed!

// communication essential - demo, ok, works !!!
// basically replaces the original idea of DataPacket classes in the original project
// complementary to Allocate class
public class Award {
	
	public WorldCell verilenElem; // ...
    public UAV acikart; // ...
    public Task givenTask; // ...
	
    public Award(UAV acikart, Task givenTask) { // sade cons
        this.acikart = acikart;
        this.givenTask = givenTask;
    }
	
	// tapsirig elementle muzakire edilir ve gonderilir
    public void AwardRans(UAV acikart, Task givenTask, WorldCell verilenElem) { // alt cons
        this.acikart = acikart;
        this.givenTask = givenTask;
		this.verilenElem = verilenElem;
    }
}
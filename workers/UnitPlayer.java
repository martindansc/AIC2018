package workers;
import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {
        MemoryManager memoryManager = new MemoryManager(uc);

        Utils utils = new Utils();
        Collect collect = new Collect(memoryManager);
        Attack attack = new Attack(memoryManager);
        Barracks barracks = new Barracks(memoryManager);

        //all directions
        Direction[] dirs = Direction.values();

	    //Random number between 0 and 2
	    int typeIndex = (int)(Math.random()*3);

        while (true) {

            memoryManager.update();

            utils.buyPointsIfNeeded(uc);
            utils.pickVictoryPoints(uc);
            Location myLocation = uc.getLocation();

            if (uc.getType() == UnitType.WORKER){
                collect.play();
            }
            else if (uc.getType() == UnitType.BARRACKS){
                barracks.play();
            } else{
                attack.play();
            }

            uc.yield(); //End of turn
        }

    }
}

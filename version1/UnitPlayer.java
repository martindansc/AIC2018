package version1;

import aic2018.UnitController;
import aic2018.UnitType;

public class UnitPlayer {

    public void run(UnitController uc) {
        MemoryManager memoryManager = new MemoryManager(uc);

        Collect collect = new Collect(memoryManager);
        Attack attack = new Attack(memoryManager);
        Barracks barracks = new Barracks(memoryManager);

        while (true) {

            memoryManager.update();

            memoryManager.buyPointsIfNeeded();
            memoryManager.pickVictoryPoints();

            if (uc.getType() == UnitType.WORKER){
                collect.play();
            } else if (uc.getType() == UnitType.BARRACKS){
                barracks.play();
            } else{
                attack.play();
            }

            uc.yield();
        }

    }
}

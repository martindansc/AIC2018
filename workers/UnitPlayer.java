package workers;
import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {
        MemoryManager memoryManager = new MemoryManager(uc);

        Utils utils = new Utils();
        Collect collect = new Collect(memoryManager);
        Attack attack = new Attack(memoryManager);
        Barracks barracks = new Barracks(memoryManager);

        while (true) {

            memoryManager.update();

            utils.buyPointsIfNeeded(uc);
            utils.pickVictoryPoints(uc);

            boolean actAsSoldier = true;

            if (uc.getType() == UnitType.WORKER){
                if (memoryManager.getAtLeastOneEnemy() == 1) {
                    Location myMoves[] = utils.getLocations(uc, memoryManager.myLocation);
                    for (Location myLoc : myMoves) {
                        if (uc.senseTree(myLoc) != null) {
                            actAsSoldier = false;
                            break;
                        }
                    }
                    if (actAsSoldier) {
                        attack.play();
                    } else {
                        collect.play();
                    }
                } else {
                    collect.play();
                }
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

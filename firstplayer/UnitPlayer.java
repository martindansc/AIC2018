package firstplayer;
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

            if(memoryManager.root && uc.getRound()%100 == 0) uc.println("Units " + memoryManager.getUnitNum() + ", "
                   + memoryManager.getWorkersNum() + "," + memoryManager.getBarraksNum() +  " at round " + uc.getRound());

            utils.buyPointsIfNeeded(uc);
            utils.pickVictoryPoints(uc);

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

package oldWorkers;
import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {
        Utils utils = new Utils();
        Collect collect = new Collect();

	    /*Insert here the code that should be executed only at the beginning of the unit's lifespan*/

	    //opponent team
	    Team opponent = uc.getOpponent();
	    Team allies = uc.getTeam();

	    //all directions
	    Direction[] dirs = Direction.values();

	    //Random number between 0 and 2
	    int typeIndex = (int)(Math.random()*3);

        while (true) {
            utils.buyPointsIfNeeded(uc);
            utils.pickVictoryPoints(uc);

            Location myLocation = uc.getLocation();

            if (uc.getType() == UnitType.WORKER){
                Location locs[] = utils.getLocations(uc, myLocation);
                int resources = uc.getResources();

                for (int i = 0; i < locs.length; i++) {
                    if (uc.canUseActiveAbility(locs[i])) {
                        uc.useActiveAbility(locs[i]);
                    }
                }
                int treeCount = 0;
                for (int i = 0; i < locs.length; i++) {
                    TreeInfo newTree = uc.senseTree(locs[i]);
                    UnitInfo newUnit = uc.senseUnit(locs[i]);
                    if (newTree != null || !uc.isAccessible(locs[i])) {
                        treeCount++;
                    }
                    if (newTree != null && newTree.remainingGrowthTurns == 0 && (newTree.oak || newTree.health > 12)) {
                        if (uc.canAttack(newTree) && (newUnit == null || newUnit.getTeam() == opponent)) {
                            uc.attack(newTree);
                        }
                    }
                }
                for (int i = 0; i < locs.length; i++) {
                    int workerCount = 0;
                    UnitInfo[] units = uc.senseUnits(allies);
                    for (int j = 0; j < units.length; j++) {
                        if (units[j].getType() == UnitType.WORKER) {
                            workerCount++;
                        }
                    }
                    if (uc.canSpawn(myLocation.directionTo(locs[i]), UnitType.WORKER)) {
                        if (((treeCount == 8 && workerCount < 4) || (treeCount < 8 && workerCount < 5)) && resources > 199) {
                            uc.spawn(myLocation.directionTo(locs[i]), UnitType.WORKER);
                            break;
                        }
                    }
                }

                //for (int i = 0; i < 8; ++i) if (uc.canSpawn(dirs[i], UnitType.BARRACKS)) uc.spawn(dirs[i], UnitType.BARRACKS);
            }

            //If barracks do a random unit between warrior, archer and knight
            else if (uc.getType() == UnitType.BARRACKS){
			    //Getting the type associated to typeIndex
			    UnitType type = UnitType.values()[2+typeIndex];

			    //try to spawn a unit of the given type, if successful reset type.
			    for (int i = 0; i < 8; ++i) if (uc.canSpawn(dirs[i], type)){
			        uc.spawn(dirs[i], type);
			        typeIndex = (int)(Math.random()*3);
                }
            } else{
			    //If not a barracks or worker move in a random direction and attack the first thing you see

                //Generate a random number between 0 and 7 and move in the associated direction
                int dirIndex = (int)(Math.random()*8);
                if (uc.canMove(dirs[dirIndex])) uc.move(dirs[dirIndex]);

                //Attack the first target you see
                UnitInfo[] enemies = uc.senseUnits(opponent);
                for (UnitInfo unit : enemies){
                    if (uc.canAttack(unit)) uc.attack(unit);
                }
            }

            // Move
            Location newLoc = collect.evalLocation(uc, myLocation);
            if (newLoc != myLocation) {
                uc.move(myLocation.directionTo(newLoc));
            }

            uc.yield(); //End of turn
        }

    }
}
package test;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {

        MemoryManager memoryManager = new MemoryManager(uc);

        Utils utils = new Utils();
        Collect collect = new Collect(memoryManager);

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

            Location[] locs = new Location[8];

			if (uc.getType() == UnitType.WORKER){
                int treeCount = 0;
                int workerCount = 0;
                int index = 0;
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        if (i != 0 || j != 0) {
                            locs[index] = new Location(myLocation.x + i, myLocation.y + j);
                            TreeInfo newTree = uc.senseTree(locs[index]);
                            UnitInfo newUnit = uc.senseUnit(locs[index]);
                            if (uc.isOutOfMap(locs[index]) || (newTree != null && newTree.remainingGrowthTurns == 0 && (newTree.oak || newTree.health > 12))) {
                                treeCount++;
                            }
                            if (newUnit != null && newUnit.getTeam() != opponent && newUnit.getType() == UnitType.WORKER) {
                                workerCount++;
                            }
                            index++;
                        }
                    }
                }

                for (int i = 0; i < 8; i++) {
                    TreeInfo newTree = uc.senseTree(locs[i]);
                    UnitInfo newUnit = uc.senseUnit((locs[i]));
                    if (newTree != null && newTree.remainingGrowthTurns == 0 && (newTree.oak || newTree.health > 12)) {
                        if (uc.canAttack(newTree) && (newUnit == null || newUnit.getTeam() == opponent)) {
                            uc.attack(newTree);
                            if(uc.isAccessible(locs[i])) {
                                uc.move(myLocation.directionTo(locs[i]));
                            }
                        }
                    }
                    if (uc.canSpawn(dirs[i], UnitType.WORKER) && workerCount == 0 && treeCount > 5) {
                        uc.spawn(dirs[i], UnitType.WORKER);
                        break;
                    }
                    if (uc.canUseActiveAbility(locs[i])) {
                        uc.useActiveAbility(locs[i]);
                        break;
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

            uc.yield(); //End of turn
        }

    }
}

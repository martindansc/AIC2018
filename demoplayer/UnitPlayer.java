package demoplayer;

import aic2018.*;

public class UnitPlayer {

    public void run(UnitController uc) {
	/*Insert here the code that should be executed only at the beginning of the unit's lifespan*/

	    //opponent team
	    Team opponent = uc.getOpponent();

	    //all directions
	    Direction[] dirs = Direction.values();

	    //Random number between 0 and 2
	    int typeIndex = (int)(Math.random()*3);

        while (true) {
			/*Insert here the code that should be executed every round*/

			//If worker do a barracks
			if (uc.getType() == UnitType.WORKER){
                for (int i = 0; i < 8; ++i) if (uc.canSpawn(dirs[i], UnitType.BARRACKS)) uc.spawn(dirs[i], UnitType.BARRACKS);
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

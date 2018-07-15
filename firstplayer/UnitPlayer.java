package firstplayer;

import aic2018.*;
import aic2018.TreeAnalysis;

public class UnitPlayer {

    // Run function
    public void run(UnitController uc) {

        TreeAnalysis tAnalysys = new TreeAnalysis();

	    //opponent team
	    Team opponent = uc.getOpponent();

	    //all directions
	    Direction[] dirs = Direction.values();

        tAnalysys.resetTree(uc);

	    //build root node if we are the first unit
        int root = tAnalysys.getRootNode();
        if(tAnalysys.getLeftNode(uc, root) == 0 &&
                tAnalysys.getRightNode(uc, root) == 0) tAnalysys.resetTree(uc);

        //Random number between 0 and 2
        int typeIndex = (int)(Math.random()*3);

        while (true) {

            // maybe we should reset the tree, when? unknown at the moment

            // insert the node if needed
            if(tAnalysys.node == tAnalysys.NULL_NODE) {
                tAnalysys.insertNode(uc);
            }

            // update the node
            tAnalysys.updateNode(uc);

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

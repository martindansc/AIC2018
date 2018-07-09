package firstplayer;

import aic2018.*;

public class UnitPlayer {

    int NODE_SPACE = 5;
    int NODE_MAX_SPACE = 1000 * NODE_SPACE;
    int NULL_NODE = -1;

    int node = NULL_NODE;

    // Tree functions for mapping units
    private int getRootNode(UnitController uc) {
        return uc.read(0);
    }

    private void resetTree(UnitController uc) {

        for(int i = 1; i < NODE_MAX_SPACE; i += NODE_SPACE) {
            uc.write(i, NULL_NODE);
        }

    }

    private int getRightNode(UnitController uc, int n) {
        return uc.read(n + 1);
    }

    private void setRightNode(UnitController uc, int n, int val) {
        uc.write(n + 1, val);
    }

    private int getLeftNode(UnitController uc, int n) {
        return uc.read(n + 2);
    }

    private void setLeftNode(UnitController uc, int n, int val) {
        uc.write(n + 2, val);
    }

    // This and the next could be optimized to use only one int
    private int getAxis(UnitController uc, int n) {
        return uc.read(n + 3);
    }

    private int getDiscriminantNum(UnitController uc, int n) {
        return uc.read(n + 4);
    }

    private void setAxisAndDicriminator(UnitController uc, int n, Location l) {
        double rand = Math.random();
        if(rand < 0.5) {
            uc.write(node + 3, -1);
            uc.write(node + 4, l.x);
        }
        else {
            uc.write(node + 3, 1);
            uc.write(node + 4, l.y);
        }
    }

    // End getters and setters

    // Higher level functions
    private void buildNode(UnitController uc) {
        // set children to null
        setRightNode(uc, node, NULL_NODE);
        setLeftNode(uc, node, NULL_NODE);

        setAxisAndDicriminator(uc, node, uc.getLocation());
    }

    private boolean isNextRight(UnitController uc, int n, Location l) {
        int discr = getAxis(uc, n);
        int num = getDiscriminantNum(uc, n);

        return ((discr < 0 && num < l.x) || (discr > 0 && num < l.y));
    }

    private void insertNode(UnitController uc) {

        if(node == NULL_NODE) {
            //System.out.println("Trying to insert a node that is already inserted!");
            return;
        }

        for(int i = 1; i < NODE_MAX_SPACE; i += NODE_SPACE) {
            if(uc.read(i) == NULL_NODE) {
                node = i;
                break;
            }
        }

        if(node == NULL_NODE) {
            //System.out.println("We are screwed, there is no space left for inserting the node");
            return;
        }

        // search for a place and build node
        Location l = uc.getLocation();
        int current_node = getRootNode(uc);
        while(true) {
            boolean is_next_right = isNextRight(uc, current_node, l);
            int next_node;
            if(is_next_right) {
                next_node = getRightNode(uc, current_node);
            }
            else {
                next_node = getLeftNode(uc, current_node);
            }

            if(next_node == NULL_NODE) {
                if(is_next_right) {
                    setRightNode(uc, current_node, node);
                }
                else {
                    setLeftNode(uc, current_node, node);
                }

                buildNode(uc);
                break;
            }

            current_node = next_node;
        }

    }

    // Run function
    public void run(UnitController uc) {
	    //opponent team
	    Team opponent = uc.getOpponent();

	    //all directions
	    Direction[] dirs = Direction.values();

	    //build root node if we are the first unit
        // yeh, I know, the second unit will rebuild the tree too
        int root = getRootNode(uc);
        if(getLeftNode(uc, root) == NULL_NODE &&
                getRightNode(uc, root) == NULL_NODE) resetTree(uc);


        //Random number between 0 and 2
        int typeIndex = (int)(Math.random()*3);

        while (true) {

            // maybe we should reset the tree, when? unknown at the moment

            // insert the node if needed
            if(node == NULL_NODE) {
                insertNode(uc);
            }

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

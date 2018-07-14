package player;

import aic2018.Location;
import aic2018.UnitController;

public class TreeAnalysis {

    public int NODE_SPACE = 5;
    public int NODE_MAX_SPACE = 1000 * NODE_SPACE;
    public int NULL_NODE = -1;

    public int node = NULL_NODE;

    // Tree functions for mapping units
    public int getRootNode() {
        return 0;
    }

    public void resetTree(UnitController uc) {

        for(int i = 0; i < NODE_MAX_SPACE; i += 1) {
            uc.write(i, NULL_NODE);
        }

    }

    public int getRightNode(UnitController uc, int n) {
        return uc.read(n + 1);
    }

    public void setRightNode(UnitController uc, int n, int val) {
        uc.write(n + 1, val);
    }

    public int getLeftNode(UnitController uc, int n) {
        return uc.read(n + 2);
    }

    public void setLeftNode(UnitController uc, int n, int val) {
        uc.write(n + 2, val);
    }

    // This and the next could be optimized to use only one int
    public int getAxis(UnitController uc, int n) {
        return uc.read(n + 3);
    }

    public int getDiscriminantNum(UnitController uc, int n) {
        return uc.read(n + 4);
    }

    public void setAxisAndDicriminator(UnitController uc, int n, Location l) {
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

    public int getUnitsNode(UnitController uc, int n) {
        if(n == -1) return 0;
        int ret = uc.read(n);
        return ret;
    }

    public void setUnitsNode(UnitController uc, int n, int val) {
        uc.write(n, val);
    }

    // End getters and setters


    public void buildNode(UnitController uc) {
        setUnitsNode(uc, node, 1);

        // set children to null
        setRightNode(uc, node, NULL_NODE);
        setLeftNode(uc, node, NULL_NODE);

        setAxisAndDicriminator(uc, node, uc.getLocation());
    }

    // Higher level functions for the map tree
    public boolean isNextRight(UnitController uc, int n, Location l) {
        int discr = getAxis(uc, n);
        int num = getDiscriminantNum(uc, n);

        return ((discr < 0 && num < l.x) || (discr > 0 && num < l.y));
    }

    public void insertNode(UnitController uc) {

        for(int i = 0; i < NODE_MAX_SPACE; i += NODE_SPACE) {
            if(uc.read(i) == NULL_NODE) {
                node = i;
                break;
            }
        }

        if(node == NULL_NODE) {
            return;
        }

        // search for a place and build node
        Location l = uc.getLocation();
        int current_node = getRootNode();
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

    public void updateNode(UnitController uc) {

        // update the number of units that you have, this is just an example
        int num = getUnitsNode(uc, getLeftNode(uc, node)) +
                getUnitsNode(uc, getRightNode(uc, node));
        setUnitsNode(uc, node, num);
    }
}

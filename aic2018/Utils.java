package aic2018;

import aic2018.Direction;
import aic2018.GameConstants;
import aic2018.Location;
import aic2018.UnitController;

public class Utils {
    public void buyPointsIfNeeded(UnitController uc) {
        if (uc.canBuyVP( GameConstants.VICTORY_POINTS_MILESTONE - uc.getTeam().getVictoryPoints())){
            uc.buyVP(GameConstants.VICTORY_POINTS_MILESTONE - uc.getTeam().getVictoryPoints());
        }

        if (uc.getRound() == GameConstants.MAX_TURNS) {
            uc.buyVP( uc.getResources()/(GameConstants.BASE_VICTORY_POINTS_COST +
                    GameConstants.MAX_TURNS/GameConstants.VICTORY_POINTS_INFLATION_ROUNDS));
        }
    }

    public void pickVictoryPoints(UnitController uc) {
        Direction[] dirs = Direction.values();
        for(int i = 0; i < 8; i++) {
            if(uc.canGatherVPs(dirs[i])) {
                uc.gatherVPs(dirs[i]);
            }
        }
    }

    public Location[] getPosibleMoves(UnitController uc) {

        if (!uc.canMove()) {
            Location locs[] = new Location[0];
            return locs;
        }

        Location locs[] = new Location[8];
        Location myLocation = uc.getLocation();
        Direction[] dirs = Direction.values();
        int possibilities = 0;

        for(int i = 0; i < 8; i++) {
            Location next = myLocation.add(dirs[i]);
            if(uc.isAccessible(next)) {
                locs[i] = next;
                possibilities++;
            }
        }

        Location realLocs[] = new Location[possibilities];
        int counter = 0;
        for (int i = 0; i < 8; i++) {
            if (locs[i] != null) {
                realLocs[i - counter] = locs[i];
            } else {
                counter++;
            }
        }

        return realLocs;
    }

    public Location[] getLocations(UnitController uc, Location loc) {
        Location locs[] = new Location[8];

        Direction[] dirs = Direction.values();
        for(int i = 0; i < 8; i++) {
            locs[i] = (loc.add(dirs[i]));
        }

        return locs;
    }
}

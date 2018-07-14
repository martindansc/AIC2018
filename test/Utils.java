package test;

import aic2018.Direction;
import aic2018.GameConstants;
import aic2018.Location;
import aic2018.UnitController;

import java.util.ArrayList;
import java.util.List;

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
        for(int i = 0; i < dirs.length; i++) {
            if(uc.canGatherVPs(dirs[i])) {
                uc.gatherVPs(dirs[i]);
            }
        }
    }

    public List<Location> getPosibleMoves(UnitController uc) {
        List<Location> locs = new ArrayList<>();

        Location myLocation = uc.getLocation();
        Direction[] dirs = Direction.values();
        for(int i = 0; i < dirs.length; i++) {
            Location next = myLocation.add(dirs[i]);
            if(uc.isAccessible(next)) {
                locs.add(next);
            }
        }

        return locs;
    }
}

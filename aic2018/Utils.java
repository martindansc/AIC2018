package aic2018;

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

        Location locs[] = new Location[9];
        locs[8] = uc.getLocation();
        Direction[] dirs = Direction.values();
        int possibilities = 0;

        for(int i = 0; i < 8; i++) {
            Location next = locs[8].add(dirs[i]);
            if(uc.isAccessible(next)) {
                locs[i] = next;
                possibilities++;
            }
        }

        Location realLocs[] = new Location[possibilities + 1];
        int counter = 0;
        for (int i = 0; i < 9; i++) {
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

    public Boolean isExtreme(UnitController uc, Location loc) {
        Location[] newPlaces = getLocations(uc, loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.canSenseLocation(newPlaces[i]) && uc.isOutOfMap(newPlaces[i])) {
                return true;
            }
        }
        return false;
    }

    public Boolean isWater(UnitController uc, Location loc) {
        Location[] newPlaces = getLocations(uc, loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.canSenseLocation(newPlaces[i]) && uc.senseWaterAtLocation(newPlaces[i])) {
                return true;
            }
        }
        return false;
    }

    public Boolean canPlantTree(int round, int resources) {
        return ((round < 100) || (resources > 699 && round > 99));
    }

    public Boolean canSpawnWorker(int round, int resources) {
        return ((resources > 199 && round < 100) || (resources > 699 && round > 99));
    }

    public Boolean canSpawnBarraks(MemoryManager manager) {
        return (manager.resources > 499 && manager.round > 99 && manager.getBarraksNum() < 3);
    }

    public Boolean isWalkable(UnitController uc, Location from, Location to) {
        VisibleCells var3 = VisibleCells.getInstance();
        int var4 = to.x - from.x + var3.center;
        int var5 = to.y - from.y + var3.center;

        for(int var6 = 0; var6 < var3.offsetX[var4][var5].length; ++var6) {
            Location var7 = from.add(var3.offsetX[var4][var5][var6], var3.offsetY[var4][var5][var6]);
            if (!var7.isEqual(to) && !var7.isEqual(from) && uc.senseWaterAtLocation(var7)) {
                return false;
            }
        }

        return true;
    }

}

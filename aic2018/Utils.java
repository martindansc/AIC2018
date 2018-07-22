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

    public int isExtreme(UnitController uc, Location loc) {
        Location[] newPlaces = getLocations(uc, loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.canSenseLocation(newPlaces[i]) && uc.isOutOfMap(newPlaces[i])) {
                return 5;
            }

            if (uc.canSenseLocation(newPlaces[i]) && uc.senseWaterAtLocation(newPlaces[i])) {
                return 1;
            }
        }
        return 0;
    }

    public boolean isObstructedWater(UnitController uc, Location var1, Location var2) {
        int x1 = var1.x;
        int x2 = var2.x;
        int y1 = var1.y;
        int y2 = var2.y;
        int rise = y2 - y1;
        int run = x2 - x1;
        if (run == 0) {
            if(y2 < y1) {
                y1 = var2.y;
                y2 = var1.y;
                for (int y = y1 + 1; y < y2; y++) {
                    Location newLoc = new Location(x1, y);
                    if (uc.canSenseLocation(newLoc) && uc.senseWaterAtLocation(newLoc)) {
                        return true;
                    }
                }
            }
        } else {
            int adjust = -1;
            int offset = 0;
            double threshold = 0.5;
            float slope = rise / run;
            if (slope >= 0) {
                adjust = 1;
            }
            if (slope <= 1 && slope >= -1) {
                double delta = Math.abs(slope);
                int y = var1.y;
                if (x2 < x1) {
                    x1 = var2.x;
                    x2 = var1.x;
                    y = y2;
                }
                for (int x = x1; x < x2; x++) {
                    Location newLoc = new Location(x, y);
                    if (uc.canSenseLocation(newLoc) && uc.senseWaterAtLocation(newLoc)) {
                        return true;
                    }
                    offset += delta;
                    if (offset >= threshold) {
                        y += adjust;
                        threshold += 1;
                    }
                }
            } else {
                float invSlope = run / rise;
                double delta = Math.abs(invSlope);
                int x = x1;
                if (y2 < y1) {
                    y1 = var2.y;
                    y2 = var1.y;
                    x = x2;
                }
                for (int y = y1; y < y2; y++) {
                    Location newLoc = new Location(x, y);
                    if (uc.canSenseLocation(newLoc) && uc.senseWaterAtLocation(newLoc)) {
                        return true;
                    }
                    offset += delta;
                    if (offset >= threshold) {
                        x += adjust;
                        threshold += 1;
                    }
                }
            }
        }
        return false;
    }

    public Boolean canPlantTree(MemoryManager manager, int numSmalls) {
        return(((manager.round < manager.getBarracksRound() && manager.resources > 220) ||
                (manager.resources > 699 && manager.round >= manager.getBarracksRound()))
                && (manager.objective == UnitType.WORKER ||
                (manager.getTotalTroops() > manager.getEnemiesSeenLastRound() * 5) && numSmalls < 4) &&
                Math.sqrt(manager.myLocation.distanceSquared(manager.closestStarterEnemey)) > manager.distanceBetweenStarters*0.4);
    }

    public Boolean canSpawnWorker(MemoryManager manager) {
        return (((manager.resources > 199 && manager.round < manager.getBarracksRound()) ||
                (manager.resources > 699 && manager.round >= manager.getBarracksRound())) &&
                ((manager.getOAKS() == 1 || (manager.getNOT_FULL() == 0 && manager.getOAKS() == 0)) &&
                (manager.getWorkersInactive() == 0 ||
                (manager.getWorkersNum() / manager.getWorkersInactive() > 2))) &&
                manager.objective == UnitType.WORKER);
    }

    public Boolean canSpawnBarracks(UnitInfo unit, MemoryManager manager) {
        int numberEnemies = manager.getEnemiesSeenLastRound();
        int numberBarracks = manager.getBarracksNum();
        int maxBarracks;
        if (numberEnemies == 0) {
            maxBarracks = 0;
        } else if (numberEnemies == 1) {
            maxBarracks = 1;
        } else if (numberEnemies < 4) {
            maxBarracks = 2;
        } else if (numberEnemies < 7) {
            maxBarracks = 3;
        } else {
            double roundCap = Math.sqrt(manager.round) / 2;
            int Cap = (int) roundCap;
            maxBarracks = numberEnemies / 3;
            if (Cap > maxBarracks) {
                maxBarracks = Cap;
            }
            if (manager.resources < numberBarracks * 30 + 500) {
                maxBarracks = numberBarracks;
            }
        }

        return (unit.getTeam() == manager.opponent && numberBarracks < maxBarracks && manager.resources > 499 && manager.round >= manager.getBarracksRound());
    }

    public Boolean canSpawnBarraks(MemoryManager manager) {
        boolean closeEnough = false;
        for(Location enemyStart : manager.startEnemies) {
            if(manager.distanceBetweenStarters * 0.5 > manager.myLocation.distanceSquared(enemyStart)) {
                closeEnough = true;
                break;
            }
        }
        return manager.getBarracksNum() < 1 && manager.resources > 499 && closeEnough;
    }

    public TreeInfo[] getFirstElements(TreeInfo[] trees, int num) {

        if(trees.length <= num) return trees;

        TreeInfo[] nearTrees = new TreeInfo[9];
        for(int i = 0; i < num; i++) {
            nearTrees[i] = trees[i];
        }

        return trees;
    }

    public UnitInfo[] getFirstElements(UnitInfo[] units, int num) {

        if(units.length <= num) return units;

        UnitInfo[] nearUnits = new UnitInfo[9];
        for(int i = 0; i < num; i++) {
            nearUnits[i] = units[i];
        }

        return units;
    }



}

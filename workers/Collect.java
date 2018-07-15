package workers;

import aic2018.*;

public class Collect {

    Utils utils = new Utils();

    public float evalLocation(UnitController uc, Location loc) {
        Team allies = uc.getTeam();
        float value = 0;

        Location locs[] = utils.getLocations(uc, loc);
        for (int i = 0; i < locs.length; i++) {
            if (uc.isOutOfMap(locs[i])) {
                return Float.NEGATIVE_INFINITY;
            }
        }

        TreeInfo[] trees = uc.senseTrees();
        for(int i = 0; i < trees.length; i++) {
            TreeInfo currentTree = trees[i];
            int distance = loc.distanceSquared(currentTree.getLocation());
            if(currentTree.isOak()) {
                value += 20/(10 + distance);
            }
            else {
                value += 50/(10 + distance);
            }
        }

        UnitInfo[] units = uc.senseUnits();
        for(int i = 0; i < units.length; i++) {
            UnitInfo currentUnit = units[i];
            int distance = loc.distanceSquared(currentUnit.getLocation());
            Team unitTeam = currentUnit.getTeam();
            UnitType unitType = currentUnit.getType();

            if(unitTeam == allies && unitType == UnitType.WORKER) {
                value -= 1000/(1 + distance);
            }

            if(unitTeam != allies && unitType != UnitType.WORKER) {
                value -= 60000/(10 + distance);
            }
        }
        return value;

    }

}

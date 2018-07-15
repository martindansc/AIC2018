package aic2018;

public class Collect {

    Utils utils = new Utils();

    public Boolean isExtreme(UnitController uc, Location loc) {
        Location[] newPlaces = utils.getLocations(uc, loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.isOutOfMap(newPlaces[i])) {
                return true;
            }
        }
        return false;
    }

    public Boolean isWater(UnitController uc, Location loc) {
        Location[] newPlaces = utils.getLocations(uc, loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.senseWaterAtLocation(newPlaces[i])) {
                return true;
            }
        }
        return false;
    }

    public Location evalLocation(UnitController uc, Location loc) {

        Location locs[] = utils.getPosibleMoves(uc);
        TreeInfo[] trees = uc.senseTrees();
        UnitInfo[] units = uc.senseUnits();
        VictoryPointsInfo[] points = uc.senseVPs();
        Team allies = uc.getTeam();

        float highestValue = -100000;
        Location bestLocation = loc;

        for (int j = 0; j < locs.length; j++) {
            float value = 0;
            if (isExtreme(uc, locs[j])) {
                value -= 10000;
            }

            if (isWater(uc, locs[j])) {
                value -= 5000;
            }

            for (int i = 0; i < trees.length; i++) {
                TreeInfo currentTree = trees[i];
                float distance = locs[j].distanceSquared(currentTree.getLocation());
                if (currentTree.isOak() && distance != 0) {
                    value += 200 / (100 + distance);
                } else if (distance != 0) {
                    value += 400 / (100 + distance);
                }
            }

            for (int i = 0; i < units.length; i++) {
                UnitInfo currentUnit = units[i];
                float distance = locs[j].distanceSquared(currentUnit.getLocation());
                Team unitTeam = currentUnit.getTeam();
                UnitType unitType = currentUnit.getType();

                if (unitTeam == allies && unitType == UnitType.WORKER) {
                    if (distance <= 2) {
                        value -= 10;
                    } else if (distance < 10) {
                        value -= 5;
                    }
                }

                if (unitTeam != allies && unitType != UnitType.WORKER) {
                    value -= 60000 / (10 + distance);
                }
            }

            for (int i = 0; i < points.length; i++) {
                VictoryPointsInfo currentVP = points[i];
                float distance = locs[j].distanceSquared(currentVP.getLocation());
                value += 2 / (1 + distance);
            }
            if (highestValue < value) {
                highestValue = value;
                bestLocation = locs[j];
            }
        }
        return bestLocation;

    }
}

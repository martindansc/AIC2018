package aic2018;

public class Attack {

    Utils utils = new Utils();

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

            for (int i = 0; i < units.length; i++) {
                UnitInfo currentUnit = units[i];
                float distance = locs[j].distanceSquared(currentUnit.getLocation());
                Team unitTeam = currentUnit.getTeam();
                UnitType unitType = currentUnit.getType();

                if(unitTeam != allies) {
                    if (unitType == UnitType.BARRACKS) {
                        value += 8 / (1 + distance) - currentUnit.getHealth()/6;
                    }
                    else if(unitType == UnitType.WORKER) {
                        value += 16 / (1 + distance) - currentUnit.getHealth()/6;
                    }
                    else {
                        value += 100 / (1 + distance) - currentUnit.getHealth()/6;
                    }
                }
                else if(unitType != UnitType.WORKER && unitType != UnitType.BARRACKS){
                    if (distance <= 2) {
                        value -= 5;
                    } else if (distance < 10) {
                        value -= 2;
                    }
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

    public void attackBestUnit(UnitController uc) {
        UnitInfo[] enemies = uc.senseUnits(uc.getOpponent());
        if(enemies.length == 0) return;

        UnitInfo enemy = enemies[0];
        int maxHealth = 10001;

        for (UnitInfo unit : enemies){
            int enemyHealth = unit.getHealth();
            if (maxHealth > enemyHealth && uc.canAttack(unit)) {
                maxHealth = enemyHealth;
                enemy = unit;
            }
        }
        if(maxHealth != 10001) uc.attack(enemy);
    }
}

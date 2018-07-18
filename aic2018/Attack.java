package aic2018;

public class Attack {

    private MemoryManager manager;
    private UnitController uc;
    private Utils utils = new Utils();

    public Attack(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
    }

    private Location myLocation;
    private Location locs[];
    private int resources;

    public void play() {

        myLocation = manager.myLocation;
        locs = utils.getLocations(uc, myLocation);
        resources = manager.resources;

        tryAttackBestUnit();
        move();
        tryAttackBestUnit();
        tryAttackTree();
    }

    public void move() {
        Location newLoc = evalLocation();
        if (!newLoc.equals(myLocation)) {
            uc.move(myLocation.directionTo(newLoc));
            manager.myLocation = newLoc;
            myLocation = manager.myLocation;
        }
    }

    public Location evalLocation() {

        Location plocs[] = utils.getPosibleMoves(uc);
        UnitInfo[] units = uc.senseUnits();
        VictoryPointsInfo[] points = uc.senseVPs();
        Team allies = uc.getTeam();

        float highestValue = -100000;
        Location bestLocation = myLocation;

        for (int j = 0; j < plocs.length; j++) {
            float value = 0;

            for (int i = 0; i < units.length; i++) {
                UnitInfo currentUnit = units[i];
                float distance = plocs[j].distanceSquared(currentUnit.getLocation());
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
                    if (distance <= 4) {
                        value -= 4;
                    } else if (distance < 10) {
                        value -= 1;
                    }
                }

            }

            for (int i = 0; i < points.length; i++) {
                VictoryPointsInfo currentVP = points[i];
                float distance = plocs[j].distanceSquared(currentVP.getLocation());
                value += 2 / (1 + distance);
            }
            if (highestValue < value) {
                highestValue = value;
                bestLocation = plocs[j];
            }
        }
        return bestLocation;

    }

    public boolean tryAttackBestUnit() {
        UnitInfo[] enemies = manager.enemies;
        if(enemies.length == 0 || !uc.canAttack()) return false;

        UnitInfo enemy = enemies[0];
        int maxHealth = 10001;

        for (UnitInfo unit : enemies){
            int enemyHealth = unit.getHealth();
            if (maxHealth > enemyHealth && uc.canAttack(unit)) {
                maxHealth = enemyHealth;
                enemy = unit;
            }
        }

        if(maxHealth != 10001) {
            uc.attack(enemy);
            return true;
        }

        return false;
    }

    public boolean tryAttackTree() {
        TreeInfo[] trees = manager.trees;
        if(trees.length == 0 || !uc.canAttack()) return false;

        for (TreeInfo tree : trees){
            if(tree.isOak() && uc.canAttack(tree)) {
                uc.attack(tree);
            }
        }

        return false;
    }



}

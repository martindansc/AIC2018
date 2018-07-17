package aic2018;

public class Collect {

    private MemoryManager manager;
    private UnitController uc;
    private Utils utils = new Utils();

    public Collect(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
    }

    private int round;
    private Location myLocation;
    private Location locs[];
    private int resources;
    private UnitInfo[] units;
    private TreeInfo[] trees;

    private int numAdjacentTrees = 0;
    private boolean attackedThisTurn = false;

    public void play() {

        attackedThisTurn = false;
        numAdjacentTrees = 0;

        round = uc.getRound();
        myLocation = uc.getLocation();
        locs = utils.getLocations(uc, myLocation);
        resources = uc.getResources();
        units = uc.senseUnits();
        trees = uc.senseTrees();

        tryToHarvest();
        move();
        tryToHarvest();
        plantIfNeeded();
        spawnIfNeeded(numAdjacentTrees);

    }

    public Location move() {
        Location newLoc = evalLocation(uc, myLocation);
        if (newLoc != myLocation) {
            uc.move(myLocation.directionTo(newLoc));

            myLocation = newLoc;
            locs = utils.getLocations(uc, myLocation);
        }

        return newLoc;
    }

    public void tryToHarvest() {
        for (int i = 0; i < locs.length; i++) {
            TreeInfo newTree = uc.senseTree(locs[i]);
            UnitInfo newUnit = uc.senseUnit(locs[i]);
            if (newTree != null || !uc.isAccessible(locs[i])) {
                numAdjacentTrees++;
            }
            if (newTree != null && newTree.remainingGrowthTurns == 0 && (newTree.oak || newTree.health > 12)) {
                if (uc.canAttack(newTree) && (newUnit == null || newUnit.getTeam() == manager.opponent)) {
                    uc.attack(newTree);
                }
            }
        }
    }

    public void plantIfNeeded() {
        for (int i = 0; i < locs.length; i++) {
            if (uc.canUseActiveAbility(locs[i]) && !attackedThisTurn && utils.canPlantTree(round, resources)) {
                uc.useActiveAbility(locs[i]);
            }
        }
    }

    public void spawnIfNeeded(int treeCount) {

        for (int i = 0; i < locs.length; i++) {
            int workerCount = 0;
            for (int j = 0; j < units.length; j++) {
                if (units[j].getType() == UnitType.WORKER && units[j].getTeam() == manager.allies) {
                    workerCount++;
                }
                if (units[j].getTeam() == manager.opponent) {
                    for (int k = 0; k < 8; ++k) {
                        if (uc.canSpawn(manager.dirs[i], UnitType.BARRACKS)){
                            uc.spawn(manager.dirs[i], UnitType.BARRACKS);
                        }
                    }
                }
            }
            if (uc.canSpawn(myLocation.directionTo(locs[i]), UnitType.WORKER)) {
                if ((treeCount >= 6 && workerCount < 4)
                        && utils.canSpawnWorker(round, resources)) {
                    uc.spawn(myLocation.directionTo(locs[i]), UnitType.WORKER);
                    break;
                }
            }
        }
    }

    public Location evalLocation(UnitController uc, Location loc) {

        Location locs[] = utils.getPosibleMoves(uc);
        VictoryPointsInfo[] points = uc.senseVPs();
        Team allies = uc.getTeam();

        float highestValue = -100000;
        Location bestLocation = loc;

        for (int j = 0; j < locs.length; j++) {
            float value = 0;
            if (utils.isExtreme(uc, locs[j])) {
                value -= 250;
            }

            if (utils.isWater(uc, locs[j])) {
                value -= 100;
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
                        value -= 2;
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

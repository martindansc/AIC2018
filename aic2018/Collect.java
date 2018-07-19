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
    private int numOaks = 0;
    private int numSmalls = 0;
    private int workerCount = 0;
    private boolean attackedThisTurn = false;

    public void play() {

        attackedThisTurn = false;
        numAdjacentTrees = 0;
        numOaks = 0;
        numSmalls = 0;
        workerCount = 0;

        round = manager.round;
        myLocation = manager.myLocation;
        locs = utils.getLocations(uc, myLocation);
        units = uc.senseUnits();
        trees = manager.trees;

        tryToHarvest();
        move();

        if (!attackedThisTurn) {
            tryToHarvest();
        }

        manager.resources = uc.getResources();
        resources = manager.resources;

        countTrees();
        senseOaks();
        senseSmalls();
        spawnIfNeeded(numAdjacentTrees);
        plantIfNeeded();
    }

    public Location move() {
        Location newLoc = evalLocation(uc, myLocation);
        if (newLoc != myLocation) {
            uc.move(myLocation.directionTo(newLoc));
            myLocation = newLoc;
            locs = utils.getLocations(uc, myLocation);
            manager.units = uc.senseUnits();
            manager.trees = uc.senseTrees();
        }

        return newLoc;
    }

    public void countTrees() {
        for (int i = 0; i < locs.length; i++) {
            TreeInfo newTree = uc.senseTree(locs[i]);
            if (newTree != null) {
                numAdjacentTrees++;
            }
        }
    }

    public void senseOaks() {
        for (int i = 0; i < trees.length; i++) {
            boolean water = utils.isObstructedWater(uc, myLocation, trees[i].location);
            if (water) {
                break;
            }
            if (trees[i].oak == true && !water) {
                numOaks++;
            }
        }
    }

    public void senseSmalls() {
        for (int i = 0; i < trees.length; i++) {
            if (trees[i].oak == false) {
                numSmalls++;
            }
        }
    }

    public void tryToHarvest() {
        for (int i = 0; i < locs.length; i++) {
            TreeInfo newTree = uc.senseTree(locs[i]);
            UnitInfo newUnit = uc.senseUnit(locs[i]);
            if (newTree != null && newTree.remainingGrowthTurns == 0 && (newTree.oak || newTree.health > 12)) {
                if (uc.canAttack(newTree) && (newUnit == null || newUnit.getTeam() == manager.opponent)) {
                    uc.attack(newTree);
                    attackedThisTurn = true;
                    break;
                }
            }
        }
    }

    public void plantIfNeeded() {
        if(utils.canPlantTree(manager) && (workerCount + 1 >= numOaks || numAdjacentTrees < 3)) {
            for (int i = 0; i < locs.length; i++) {
                if (uc.canUseActiveAbility(locs[i]) ) {
                    manager.objectiveCompleted();
                    uc.useActiveAbility(locs[i]);
                }
            }
        }
    }

    private void spwanBarracks(Direction dir) {
        uc.spawn(dir, UnitType.BARRACKS);

        manager.objectiveCompleted();

        // Updates barracks in construction
        uc.write(6, uc.read(6) + 1);
        for (int i = 20; i < 40; i = i + 2) {
            if (uc.read(i) == 0) {
                uc.write(i, uc.senseUnit(myLocation.add(dir)).getID());
                break;
            }
        }
    }

    public void spawnIfNeeded(int treeCount) {

        for (int j = 0; j < units.length; j++) {
            if (units[j].getType() == UnitType.WORKER && units[j].getTeam() == manager.allies) {
                workerCount++;
            }
            if (utils.canSpawnBarraks(units[j], manager)) {
                for (int k = 0; k < 8; k++) {
                    if (uc.canSpawn(manager.dirs[k], UnitType.BARRACKS)){
                        spwanBarracks(manager.dirs[k]);
                        break;
                    }
                }
            }
        }

        if (utils.canSpawnBarraks(manager)) {
            for (int k = 0; k < 8; k++) {
                if (uc.canSpawn(manager.dirs[k], UnitType.BARRACKS)){
                    spwanBarracks(manager.dirs[k]);
                    break;
                }
            }
        }

        if (((treeCount == 8 && workerCount < 4) || (numSmalls > (workerCount + 1) * 6) || (numOaks > (workerCount + 1) * 1.3))
                && utils.canSpawnWorker(manager)) {
            for (int i = 0; i < locs.length; i++) {
                if (uc.canSpawn(myLocation.directionTo(locs[i]), UnitType.WORKER)) {
                        uc.spawn(myLocation.directionTo(locs[i]), UnitType.WORKER);
                        manager.objectiveCompleted();
                        break;
                }
            }
        }
    }


    public Location evalLocation(UnitController uc, Location loc) {

        Location locs[] = utils.getPosibleMoves(uc);
        VictoryPointsInfo[] points = uc.senseVPs();
        Team allies = manager.allies;

        float highestValue = Integer.MIN_VALUE;
        Location bestLocation = loc;

        for (int j = 0; j < locs.length; j++) {
            int value = 0;

            if(!attackedThisTurn && locs[j].isEqual(myLocation)) {
                value -= 50000;
            }

            if (utils.isExtreme(uc, locs[j])) {
                value -= 50000;
            }

            if (utils.isWater(uc, locs[j])) {
                value -= 10000;
            }

            for (int i = 0; i < trees.length; i++) {
                TreeInfo currentTree = trees[i];
                int distance = locs[j].distanceSquared(currentTree.location);
                if (currentTree.oak && distance != 0) {
                    value += 32000 / (distance * distance);
                } else if (distance != 0) {
                    value += 2000 / (distance * distance);
                }
            }


            for (int i = 0; i < units.length; i++) {
                UnitInfo currentUnit = units[i];
                int distance = locs[j].distanceSquared(currentUnit.getLocation());
                Team unitTeam = currentUnit.getTeam();
                UnitType unitType = currentUnit.getType();

                if (unitTeam == allies && unitType == UnitType.WORKER) {
                    if (distance <= 2) {
                        value -= 10000;
                    }
                    else if (distance < 10) {
                        value -= 2000;
                    }
                    
                    if(!attackedThisTurn) {
                        value -= 5000;
                    }
                }

                if (unitTeam != allies && unitType != UnitType.WORKER) {
                    value -= 60000 / (10 + distance);
                }
            }

            for (int i = 0; i < points.length; i++) {
                VictoryPointsInfo currentVP = points[i];
                int distance = locs[j].distanceSquared(currentVP.getLocation());
                if(distance != 0) {
                    value += 200 / (1 + distance);
                }
                else {
                    value -= 200;
                }

            }

            if (highestValue < value) {
                highestValue = value;
                bestLocation = locs[j];
            }
        }
        return bestLocation;

    }
}

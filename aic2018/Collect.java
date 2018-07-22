package workers;

import aic2018.*;

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
    private int unaccessible = 0;
    private int numOaks = 0;
    private int oakHealth = 0;
    private int numSmalls = 0;
    private int workerCount = 0;
    private boolean attackedThisTurn = false;

    public void play() {

        attackedThisTurn = false;
        numAdjacentTrees = 0;
        unaccessible = 0;
        numOaks = 0;
        oakHealth = 0;
        numSmalls = 0;
        workerCount = 0;

        round = manager.round;
        myLocation = manager.myLocation;
        locs = utils.getLocations(uc, myLocation);
        units = manager.units;
        trees = manager.trees;

        tryToHarvest();
        move();

        manager.resources = uc.getResources();
        resources = manager.resources;

        countTrees();
        senseOaks();
        countWorkers();
        if (numOaks > (workerCount + 1) * 1.3 && oakHealth / ((workerCount + 1) * 4) > 400) {
            uc.write(manager.OAKS, 1);
        }
        if (unaccessible + numAdjacentTrees < 7) {
            uc.write(manager.NOT_FULL, 1);
        }
        spawnIfNeeded(numAdjacentTrees);
        plantIfNeeded();
        tryToHarvest();
    }

    public void move() {
        if(uc.canMove()) {
            Location newLoc = evalLocation(uc, myLocation);
            if (newLoc != myLocation) {
                uc.move(myLocation.directionTo(newLoc));
                myLocation = newLoc;
                locs = utils.getLocations(uc, myLocation);
            }
        }
    }

    public void countTrees() {
        for (int i = 0; i < locs.length; i++) {
            if (uc.senseWaterAtLocation(locs[i])) {
                unaccessible++;
            }
            TreeInfo newTree = uc.senseTree(locs[i]);
            if (newTree != null) {
                numAdjacentTrees++;
            }
        }
        if (numAdjacentTrees < 1) {
            uc.write(manager.WORKER_INACTIVE_CURRENT, uc.read(manager.WORKER_INACTIVE_CURRENT) + 1);
        }
    }

    public void senseOaks() {

        for (int i = 0; i < trees.length; i++) {
            if (trees[i].oak == false) {
                numSmalls++;
            } else {

                if (uc.getEnergyLeft() < 3000) {
                    continue;
                }

                boolean water = utils.isObstructedWater(uc, myLocation, trees[i].location);
                if (!water) {
                    numOaks++;
                    oakHealth += trees[i].health;
                }
            }
        }
    }

    public void countWorkers() {
        for (int j = 0; j < units.length; j++) {
            if (units[j].getType() == UnitType.WORKER && units[j].getTeam() == manager.allies) {
                workerCount++;
            }
        }
    }

    public void tryToHarvest() {
        if(uc.canAttack()) {
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
    }

    public void plantIfNeeded() {
        if(utils.canPlantTree(manager)) {
            for (int i = 0; i < locs.length; i++) {
                if (uc.canUseActiveAbility(locs[i]) ) {
                    uc.useActiveAbility(locs[i]);
                    manager.decideNextUnitType();
                }
            }
        }
    }

    private void spawnBarracks(Direction dir) {
        uc.spawn(dir, UnitType.BARRACKS);

        // Updates barracks in construction
        uc.write(manager.BARRACKS_CONSTRUCTION, uc.read(manager.BARRACKS_CONSTRUCTION) + 1);
        for (int i = 60; i < 100; i = i + 2) {
            if (uc.read(i) == 0) {
                uc.write(i, uc.senseUnit(myLocation.add(dir)).getID());
                break;
            }
        }
    }

    public void checkForSpawn() {
        for (int j = 0; j < units.length; j++) {
            if (utils.canSpawnBarracks(units[j], manager)) {
                for (int k = 0; k < 8; k++) {
                    if (uc.canSpawn(manager.dirs[k], UnitType.BARRACKS)){
                        Location newLoc = myLocation.add(manager.dirs[k]);
                        Location allLocs[] = utils.getLocations(uc, newLoc);
                        int countLocs = 0;
                        int countWater = 0;
                        for (Location myLoc : allLocs) {
                            if (!uc.isAccessible(myLoc)) {
                                countLocs++;
                                if (uc.senseWaterAtLocation(myLoc)) {
                                    countWater++;
                                }
                            }
                        }
                        if (countLocs < 4 || (countWater == 4 && countLocs == 4)) {
                            spawnBarracks(manager.dirs[k]);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void spawnIfNeeded(int treeCount) {
        checkForSpawn();
        if (((treeCount == 8 && workerCount < 4) || (numSmalls > (workerCount + 1) * 6) || (numOaks > (workerCount + 1) * 1.3 && oakHealth / ((workerCount + 1) * 4) > 400)) && utils.canSpawnWorker(manager)) {
            for (int i = 0; i < locs.length; i++) {
                if (uc.canSpawn(myLocation.directionTo(locs[i]), UnitType.WORKER)) {
                    uc.spawn(myLocation.directionTo(locs[i]), UnitType.WORKER);
                    manager.decideNextUnitType();
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

        TreeInfo ctrees[] = utils.getFirstElements(trees, 6);
        UnitInfo cunits[] = utils.getFirstElements(units, 4);

        for (int j = 0; j < locs.length; j++) {
            int value = 0;

            if(!attackedThisTurn && locs[j].isEqual(myLocation)) {
                value -= 50000;
            }

            value -= 10000 * utils.isExtreme(uc, locs[j]);

            for (int i = 0; i < ctrees.length; i++) {
                TreeInfo currentTree = ctrees[i];
                int distance = locs[j].distanceSquared(currentTree.location);
                if (currentTree.oak && distance != 0) {
                    value += 64000 / (distance * distance);
                } else if (distance != 0) {
                    value += 2000 / (distance * distance);
                }
            }


            for (int i = 0; i < cunits.length; i++) {
                UnitInfo currentUnit = cunits[i];
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

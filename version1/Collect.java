package version1;

import aic2018.*;

public class Collect {

    private MemoryManager manager;
    private UnitController uc;

    Direction lastDirectionMoved;
    int numOaks;
    boolean[] reachableTrees;

    public Collect(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;

        lastDirectionMoved = manager.dirs[0];

        manager.state.setState("farming");
    }

    public void play() {

        updateInfo();
        tryToHarvest();
        tryToMove();
        tryToConstructBarrakcs();
        tryToSpawnWorker();
        tryToSpawnTree();
        tryToHarvest();
        tryToAttack();
    }

    private void tryToHarvest() {

        if(uc.canAttack()) {

            int bestTarget = -1;
            int currentScore = -1;

            TreeInfo[] newTrees = manager.nearTrees;

            for (int i = 0; i < newTrees.length; i++) {
                if(!uc.canAttack(newTrees[i])) continue;

                TreeInfo newTree = newTrees[i];
                UnitInfo newUnit = uc.senseUnit(newTree.location);

                int score = getPointsHarvest(newTree, newUnit);
                if(currentScore < score) {
                    bestTarget = i;
                    currentScore = score;
                }
            }

            if(bestTarget != -1) {
                uc.attack(newTrees[bestTarget]);
            }
        }
    }

    private Boolean canPlantTree() {
        return manager.state.canPlantTree() &&
                (manager.round < manager.getBarracksRound() && manager.resources > 200) ||
                (manager.resources > 699 && manager.round >= manager.getBarracksRound());

    }

    private void tryToConstructBarrakcs() {

    }

    private void tryToSpawnWorker() {
        // there are 2 main reasons

        for(Direction direction : manager.dirs) {
            if (manager.trees.length / (manager.units.length  - manager.enemies.length + 1) > 5 && uc.canSpawn(direction, UnitType.WORKER)) {
                uc.spawn(direction, UnitType.WORKER);
            }
            break;
        }
    }

    private void tryToMove() {
        if(!uc.canMove()) return;

        int[] scoreDirections = new int[8];

        updatePointsTrees(scoreDirections);
        updatePointsUnits(scoreDirections);

        filterNonMovableDirections(scoreDirections);
        moveToBestDirection(scoreDirections);
    }

    private void filterNonMovableDirections(int[] scores) {
        for(int i = 0; i < 8; i++) {
            if(!uc.canMove(manager.dirs[i])) scores[i] = Integer.MIN_VALUE;
        }
    }

    private void moveToBestDirection(int[] scores) {
        int bestScore = Integer.MIN_VALUE;
        int directionIndex = 0;

        for(int i = 0; i < scores.length; i++) {
            if(bestScore < scores[i]) {
                bestScore = scores[i];
                directionIndex = i;
            }
        }

        if(bestScore > Integer.MIN_VALUE) {
            uc.move(manager.dirs[directionIndex]);
            lastDirectionMoved = manager.dirs[directionIndex];
            manager.myLocation = uc.getLocation();
        }
    }

    private void tryToAttack() {

    }

    private void updateInfo() {
        // get num oaks / small
        numOaks = 0;
        reachableTrees = new boolean[manager.trees.length];
        for(int i = 0; i < manager.trees.length; i++) {
            TreeInfo tree = manager.trees[i];
            if(tree.isOak() && manager.waterRaycast.fastRay(manager.myLocation, tree.location)) {
                numOaks++;
                reachableTrees[i] = true;
            }
            else if(tree.isSmall()){
                reachableTrees[i] = true;
            }
            else {
                reachableTrees[i] = false;
            }
        }

    }

    private void tryToSpawnTree() {
        if(uc.canUseActiveAbility() && canPlantTree()) {

            int bestScore = Integer.MIN_VALUE;
            Direction bestDirection = null;

            for(Direction currentDirection : manager.dirs) {
                int score = getPointsPlantTree(currentDirection);
                if(score > bestScore) {
                    bestScore = score;
                    bestDirection = currentDirection;
                }
            }

            if(bestScore > Integer.MIN_VALUE) {
                Location bestLocation = manager.myLocation.add(bestDirection);
                uc.useActiveAbility(bestLocation);
            }

        }
    }

    // ------ HEURISTIC FUNCTIONS -------

    private int getPointsPlantTree(Direction direction) {
        int points = Integer.MIN_VALUE;

        Location location = manager.myLocation.add(direction);
        if(uc.canUseActiveAbility(location)) {
            points = 0;

            if(direction.isEqual(lastDirectionMoved)) points += 1;

            if(uc.senseTree(manager.myLocation.add(direction.rotateLeft())) != null) {
                points += 5;
            }

        }
        return  points;
    }

    private int getPointsHarvest(TreeInfo newTree, UnitInfo newUnit) {
        int points = Integer.MIN_VALUE;

        if (newTree.remainingGrowthTurns > 0) return points;

        if (newTree.oak || newTree.health > 12) {

            points = 0;

            if (newUnit == null) {
                points += 100 + Math.max(newTree.health, 99);
            }
            else if(newUnit.getTeam() == manager.opponent){
                points += 200;
            }
            else {
                points -= 10000;
            }

            if(newTree.oak) {
                points += 30;
            }
        }
        else if(newUnit != null && newUnit.getTeam() != manager.allies
                && newUnit.getType() == UnitType.WORKER) {
            points = 200;
        }

        return points;
    }

    private void updatePointsTrees(int[] scores) {
        TreeInfo[] trees = manager.trees;
        int maxIter = Math.min(trees.length, 8);

        for(int i = 0; i < maxIter; i++) {
            TreeInfo newTree = trees[i];

            if(!reachableTrees[i]) continue;

            double distance = Math.sqrt(newTree.location.distanceSquared(manager.myLocation));
            Direction directionTo = manager.myLocation.directionTo(newTree.location);

            if(directionTo != Direction.ZERO) {

                if(distance >= 2) {
                    // add that we will find a tree in this direction
                    scores[directionTo.ordinal()] += 1000 / (5 + distance);

                    // we can rotate right and left the direction and add some
                    Direction directionRight = directionTo.rotateRight();
                    scores[directionRight.ordinal()] += 800 / (5 + distance);

                    Direction directionLeft = directionTo.rotateLeft();
                    scores[directionLeft.ordinal()] += 800 / (5 + distance);
                }
            }
        }
    }

    private void updatePointsUnits(int[] scores) {
        UnitInfo[] units = manager.units;

        int maxIter = Math.min(units.length, 8);

        for(int i = 0; i < maxIter; i++) {
            UnitInfo unit = units[i];
            double distance = Math.sqrt(unit.getLocation().distanceSquared(manager.myLocation));
            Direction directionTo = manager.myLocation.directionTo(unit.getLocation());

            if(directionTo != Direction.ZERO) {

                if(unit.getTeam() != manager.allies) {

                }
                else if(unit.getType() == UnitType.WORKER) {

                    double value = 0;

                    if (distance < 2) {
                        value = 1500;
                    }
                    else if (distance < 10) {
                        value = 1000/distance;
                    }

                    // add that we will find a tree in this direction
                    scores[directionTo.ordinal()] -= value;

                    // we can rotate right and left the direction and add some
                    Direction directionRight = directionTo.rotateRight();
                    scores[directionRight.ordinal()] -= value*0.7;

                    Direction directionLeft = directionTo.rotateLeft();
                    scores[directionLeft.ordinal()] -= value*0.7;

                    Direction oposite = directionTo.opposite();
                    scores[oposite.ordinal()] += value/10;
                }
            }

        }
    }

}

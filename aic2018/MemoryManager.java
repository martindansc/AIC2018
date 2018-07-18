package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    private int BARRACKS = 6;

    private int ENEMIES_SEEN_LAST_ROUND = 15;

    private int AT_LEAST_ONE_ENEMY = 20;

    public UnitController uc;
    private int counterMod2;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Location[] startEnemies;

    public int round;
    public int resources;
    public UnitInfo[] units;
    public UnitInfo[] enemies;
    public TreeInfo[] trees;
    public Location myLocation;

    int distanceBetweenStarters;

    UnitType objective;

    int roundBarracks;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        round = uc.getRound();

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();

        myLocation = uc.getLocation();

        distanceBetweenStarters = Integer.MAX_VALUE;

        startEnemies = uc.getTeam().getOpponent().getInitialLocations();
        for(Location startEnemy : startEnemies) {
            int distance = myLocation.distanceSquared(startEnemy);
            if(distanceBetweenStarters > distance) {
                distanceBetweenStarters = distance;
            }
        }

        objective = UnitType.WORKER;
        roundBarracks = Math.min(100, distanceBetweenStarters);
    }

    public void update() {

        myLocation = uc.getLocation();
        round = uc.getRound();
        counterMod2 = round%2;
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        resources = uc.getResources();
        units = uc.senseUnits();

        // update if I'm root
        uc.write(AMIROOT + counterMod2, 0);
        if(uc.read(AMIROOT + 1 - counterMod2) == 0) {
            root = true;
            uc.write(AMIROOT + 1 - counterMod2, 1);
        }
        else {
            root = false;
        }

        if(root) rootUpdate();

        int roundMod3 = round%3;
        int nextRound3 = (roundMod3+1)%3;

        // update num units
        uc.write(roundMod3, uc.read(roundMod3) + 1);
        uc.write(nextRound3, 0);

        if(uc.getType() == UnitType.WORKER) {
            uc.write(3 + roundMod3, uc.read(3 + roundMod3) + 1);
            uc.write(3 + nextRound3, 0);
        }

        if(enemies.length > 0) {
            uc.write(ENEMIES_SEEN_LAST_ROUND + roundMod3, uc.read(ENEMIES_SEEN_LAST_ROUND + roundMod3));
            uc.write(ENEMIES_SEEN_LAST_ROUND + nextRound3, 0);

            uc.write(AT_LEAST_ONE_ENEMY, 1);
        }

    }

    public int getUnitNum() {
        return  uc.read((round + 2)%3);
    }

    public int getWorkersNum() {
        return uc.read(3 + (round + 2)%3);
    }

    public int getBarraksNum() {
        return uc.read(6);
    }

    public int getTroopsNum() {
        return getUnitNum() - getWorkersNum();
    }

    public void barracksConstructed() {
        uc.write(BARRACKS, uc.read(BARRACKS) + 1);
    }

    public int getEnemiesSeenLastRound() {
        return uc.read((ENEMIES_SEEN_LAST_ROUND + 2)%3);
    }

    public int getAtLeastOneEnemy() {
        return uc.read(AT_LEAST_ONE_ENEMY);
    }

    // PRIVATE

    private void rootUpdate() {

    }

}

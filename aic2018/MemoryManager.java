package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    private int GRAVITY_STARTS = 20;

    private int ENEMIES_SEEN_LAST_ROUND = 15;

    public UnitController uc;
    private int counterMod2;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;

    public int round;
    public int resources;
    public UnitInfo[] enemies;
    public TreeInfo[] trees;
    public Location myLocation;

    int distanceBetweenStarters;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        round = uc.getRound();

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();

        myLocation = uc.getLocation();

        distanceBetweenStarters = Integer.MAX_VALUE;

        Location[] startEnemies = uc.getTeam().getOpponent().getInitialLocations();
        for(Location startEnemy : startEnemies) {
            int distance = myLocation.distanceSquared(startEnemy);
            if(distanceBetweenStarters > distance) {
                distanceBetweenStarters = distance;
            }
        }
    }

    public void update() {

        round = uc.getRound();
        counterMod2 = round%2;
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        resources = uc.getResources();

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

        if(uc.getType() == UnitType.BARRACKS) {
            uc.write(6 + roundMod3, uc.read(6 + roundMod3) + 1);
            uc.write(6 + nextRound3, 0);
        }

        if(enemies.length > 0) {
            uc.write(ENEMIES_SEEN_LAST_ROUND + roundMod3, uc.read(ENEMIES_SEEN_LAST_ROUND + roundMod3));
            uc.write(ENEMIES_SEEN_LAST_ROUND + nextRound3, 0);
        }

    }

    public int getUnitNum() {
        return  uc.read((round + 2)%3);
    }

    public int getWorkersNum() {
        return uc.read(3 + (round + 2)%3);
    }

    public int getBarraksNum() {
        return uc.read(6 + (round + 2)%3);
    }

    public void barracksConstructed() {
        int previousRound = (round + 2)%3;
        uc.write(6 + previousRound, uc.read(previousRound) + 1);
    }

    public int getEnemiesSeenLastRound() {
        return uc.read((ENEMIES_SEEN_LAST_ROUND + 2)%3);
    }

    // PRIVATE

    private void rootUpdate() {

    }

}

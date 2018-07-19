package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    private int BARRACKS = 6;

    private int ENEMIES_SEEN_LAST_ROUND = 15;

    private int AT_LEAST_ONE_ENEMY = 16;

    private int OBJECTIVE = 17;
    private int OBJECTIVE_COMPLETED = 18;

    private int DISTANCE_STARTERS = 19;

    public UnitController uc;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Location[] startEnemies;

    public Location starter;

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

        starter = allies.getInitialLocations()[0];

        startEnemies = uc.getTeam().getOpponent().getInitialLocations();
        for(Location startEnemy : startEnemies) {
            int distance = starter.distanceSquared(startEnemy);
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
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        resources = uc.getResources();
        units = uc.senseUnits();

        uc.write(1, round);

        if (round != 0 && uc.read(0) != uc.read(1)) {
            uc.write(0, round);
            root = true;
        } else {
            root = false;
        }

        if (round == 0 && uc.read(0) == 0) {
            uc.write(0, round);
            root = true;
        }


        if(root) rootUpdate();

        if(enemies.length> 0) uc.write(AT_LEAST_ONE_ENEMY, 1);

        if(uc.getType() == UnitType.WORKER) {
            uc.write(3, uc.read(3) + 1);
        }

        if(uc.getType() == UnitType.BARRACKS) {
            uc.write(5, uc.read(5) + 1);
        }

        int objective_val = uc.read(OBJECTIVE);
        objective = UnitType.values()[objective_val];
    }

    public int getWorkersNum() {
        return uc.read(2);
    }

    public int getBarracksNum() {
        return uc.read(4) + uc.read(6);
    }

    public int getWarriorsNum() {
        return uc.read(7) + uc.read(9);
    }

    public int getBarracksConsNum() {
        return uc.read(6);
    }

    public int getEnemiesSeenLastRound() {
        return uc.read(ENEMIES_SEEN_LAST_ROUND);
    }

    public int getAtLeastOneEnemy() {
        return uc.read(AT_LEAST_ONE_ENEMY);
    }

    // PRIVATE

    private void rootUpdate() {
        // Updates workers
        uc.write(2, uc.read(3));
        uc.write(3, 0);

        // Updates barracks
        uc.write(4, uc.read(5));
        uc.write(5, 0);

        // Updates warriors
        uc.write(7, uc.read(8));
        uc.write(8, 0);

        uc.write(AT_LEAST_ONE_ENEMY, 0);

        // Updates barracks in construction
        for (int i = 20; i < 40; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 25) {
                    uc.write(6, uc.read(6) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        // Updates warriors in construction
        for (int i = 40; i < 100; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(9, uc.read(9) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }


        //update objective
        if(uc.read(OBJECTIVE_COMPLETED) == 1 && getAtLeastOneEnemy() == 0) {
            if(round >= roundBarracks && getBarracksNum() + getBarracksConsNum() < 1) {
                uc.write(OBJECTIVE, 1);
            }
            else if(round < roundBarracks || Math.random()*1600 > round*2) {
                uc.write(OBJECTIVE, 0);
            }
            else {
                uc.write(OBJECTIVE, randomPonderedUnit());
            }
            uc.write(OBJECTIVE_COMPLETED, 0);
        }
        else if(getAtLeastOneEnemy() > 0){
            uc.write(OBJECTIVE, randomPonderedUnit());
            uc.write(OBJECTIVE_COMPLETED, 0);
        }

    }

    public void objectiveCompleted() {
        uc.write(OBJECTIVE_COMPLETED, 1);
    }

    private int randomPonderedUnit() {
        int num = (int)(Math.random()*100);
        if(num < 100) return 2;
        else return 3;
    }

}

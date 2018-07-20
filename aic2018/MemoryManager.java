package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    private int BARRACKS = 6;

    private int ENEMIES_SEEN_LAST_ROUND = 15;

    private int AT_LEAST_ONE_ENEMY = 16;

    private int OBJECTIVE = 17;
    private int OBJECTIVE_COMPLETED = 18;

    private int LIMIT_GOLD_WORKERS = 19;

    private int OAKS = 20;
    private int NOT_FULL = 21;

    private int CLEARED1 = 22;
    private int CLEARED2 = 23;
    private int CLEARED3 = 24;
    private int ENEMY_BASES = 25;

    private int ENEMY_XLOC = 26;
    private int ENEMY_YLOC = 27;

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

    public int limitGoldWorkers = 0;

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
        roundBarracks = 100;
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


        limitGoldWorkers = uc.read(LIMIT_GOLD_WORKERS);

        if(root) rootUpdate();

        if(enemies.length > 0) {
            uc.write(AT_LEAST_ONE_ENEMY, 1);
            if (uc.read(ENEMY_XLOC) == 0 && uc.read(ENEMY_YLOC) == 0) {
                uc.write(ENEMY_XLOC, enemies[0].getLocation().x);
                uc.write(ENEMY_YLOC, enemies[0].getLocation().y);
            }
        }

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

    public int getOAKS() {
        return uc.read(OAKS);
    }
    public int getNOT_FULL() {
        return uc.read(NOT_FULL);
    }

    // PRIVATE

    private void updateObjective() {
        //update objective
        //if(uc.read(OBJECTIVE_COMPLETED) == 1) {
            if(round >= roundBarracks && getBarracksNum() + getBarracksConsNum() < 1) {
                uc.write(OBJECTIVE, 1);
            }
            else if(round < roundBarracks || getEnemiesSeenLastRound() > 0) {
                uc.write(OBJECTIVE, 0);
                uc.write(LIMIT_GOLD_WORKERS, Math.max(500, limitGoldWorkers));
            }
            else {
                uc.write(OBJECTIVE, randomPonderedUnit());
            }
            uc.write(OBJECTIVE_COMPLETED, 0);
        //}
    }

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

        uc.write(20, 0);
        uc.write(21, 1);

        if (uc.read(ENEMY_BASES) == 0) {
            uc.write(ENEMY_BASES, startEnemies.length);
            if (startEnemies.length == 2) {
                uc.write(CLEARED3,1);
            }
            if (startEnemies.length == 1) {
                uc.write(CLEARED2,1);
            }
        }

        // Updates barracks in construction
        for (int i = 60; i < 100; i = i + 2) {
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
        for (int i = 100; i < 200; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(9, uc.read(9) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        updateObjective();


    }

    public void objectiveCompleted() {
        uc.write(OBJECTIVE_COMPLETED, 1);
    }

    public boolean checkIfObjectiveCompleted() {
        return uc.read(OBJECTIVE_COMPLETED) == 1;
    }

    private int randomPonderedUnit() {
        int num = (int)(Math.random()*100);
        if(num < 100) return 2;
        else return 3;
    }

}

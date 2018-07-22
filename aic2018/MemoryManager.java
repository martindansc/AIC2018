package aic2018;

public class MemoryManager {

    public int PREVIOUS_ROUND = 0;
    public int CURRENT_ROUND = 1;
    public int WORKERS_PREVIOUS = 2;
    public int WORKERS_CURRENT = 3;
    public int BARRACKS_PREVIOUS = 4;
    public int BARRACKS_CURRENT = 5;
    public int BARRACKS_CONSTRUCTION = 6;
    public int WARRIORS_PREVIOUS = 7;
    public int WARRIORS_CURRENT = 8;
    public int WARRIORS_CONSTRUCTION = 9;
    public int BARRACKS_ROUND = 10;
    public int ARCHERS_PREVIOUS = 11;
    public int ARCHERS_CURRENT = 12;
    public int ARCHERS_CONSTRUCTION = 13;
    public int ENEMIES = 14;
    public int ENEMY_ID = 15;
    public int AT_LEAST_ONE_ENEMY = 16;
    public int OBJECTIVE = 17;
    public int OBJECTIVE_COMPLETED = 18;
    public int LIMIT_GOLD_WORKERS = 19;
    public int OAKS = 20;
    public int NOT_FULL = 21;
    public int CLEARED1 = 22;
    public int CLEARED2 = 23;
    public int CLEARED3 = 24;
    public int ENEMY_BASES = 25;
    public int ENEMY_XLOC = 26;
    public int ENEMY_YLOC = 27;
    public int KNIGHTS_PREVIOUS = 28;
    public int KNIGHTS_CURRENT = 29;
    public int KNIGHTS_CONSTRUCTION = 30;
    public int BALLISTAS_PREVIOUS = 31;
    public int BALLISTAS_CURRENT = 32;
    public int BALLISTAS_CONSTRUCTION = 33;
    public int RETARGET = 34;
    public int WORKER_INACTIVE_PREVIOUS = 35;
    public int WORKER_INACTIVE_CURRENT = 36;

    public UnitController uc;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Location[] startEnemies;

    public Location[] starters;

    public int round;
    public int resources;
    public UnitInfo[] units;
    public UnitInfo[] enemies;
    public TreeInfo[] trees;
    public Location myLocation;
    public UnitType type;

    public int limitGoldWorkers = 0;

    public int distanceBetweenStarters;
    public Location closestStarterEnemey;
    public int forceBarracksRound;

    UnitType objective;

    int roundBarracks = 100;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        round = uc.getRound();

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();

        myLocation = uc.getLocation();
        type = uc.getType();

        distanceBetweenStarters = Integer.MAX_VALUE;

        starters = allies.getInitialLocations();

        startEnemies = uc.getTeam().getOpponent().getInitialLocations();
        for(Location starter : starters) {
            for (Location startEnemy : startEnemies) {
                int distance = (int) Math.sqrt(starter.distanceSquared(startEnemy));
                if (distanceBetweenStarters > distance) {
                    distanceBetweenStarters = distance;
                    closestStarterEnemey = startEnemy;
                }
            }
        }

        objective = UnitType.WORKER;
    }

    public void update() {

        myLocation = uc.getLocation();
        round = uc.getRound();
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        resources = uc.getResources();
        units = uc.senseUnits();

        uc.write(CURRENT_ROUND, round);

        if (round != 0 && uc.read(PREVIOUS_ROUND) != uc.read(CURRENT_ROUND)) {
            uc.write(PREVIOUS_ROUND, round);
            root = true;
        } else {
            root = false;
        }

        if (round == 0 && uc.read(PREVIOUS_ROUND) == 0) {
            uc.write(PREVIOUS_ROUND, round);
            root = true;
        }

        limitGoldWorkers = uc.read(LIMIT_GOLD_WORKERS);

        if(root) rootUpdate();

        if(enemies.length > 0) {
            uc.write(AT_LEAST_ONE_ENEMY, 1);
            if (uc.read(ENEMY_ID) == 0) {
                uc.write(ENEMY_XLOC, enemies[0].getLocation().x);
                uc.write(ENEMY_YLOC, enemies[0].getLocation().y);
                uc.write(ENEMY_ID, enemies[0].getID());
                uc.write(RETARGET, 0);
            }

            // offense mode
            if(objective == UnitType.WORKER) decideNextUnitType();
        }

        if (getAtLeastOneEnemy() == 0) {
            objective = UnitType.WORKER;
        }

        if (uc.read(AT_LEAST_ONE_ENEMY) == 1 && uc.read(BARRACKS_ROUND) == 0) {
            uc.write(BARRACKS_ROUND, round);
        }

        if(uc.getType() == UnitType.WORKER) {
            uc.write(WORKERS_CURRENT, uc.read(WORKERS_CURRENT) + 1);
        }

        if(uc.getType() == UnitType.BARRACKS) {
            uc.write(BARRACKS_CURRENT, uc.read(BARRACKS_CURRENT) + 1);
        }

        if(uc.getType() == UnitType.WARRIOR) {
            uc.write(WARRIORS_CURRENT, uc.read(WARRIORS_CURRENT) + 1);
        }

        if(uc.getType() == UnitType.ARCHER) {
            uc.write(ARCHERS_CURRENT, uc.read(ARCHERS_CURRENT) + 1);
        }

        if(uc.getType() == UnitType.KNIGHT) {
            uc.write(KNIGHTS_CURRENT, uc.read(KNIGHTS_CURRENT) + 1);
        }

        if(uc.getType() == UnitType.BALLISTA) {
            uc.write(BALLISTAS_CURRENT, uc.read(BALLISTAS_CURRENT) + 1);
        }

        // Update unique enemies
        for (int i = 0; i < enemies.length; i++) {
            for (int j = 1000; j < 1500; j++) {
                int ID = uc.read(j);
                if (ID == 0) {
                    uc.write(j, enemies[i].getID());
                    break;
                } else if (ID == enemies[i].getID()) {
                    break;
                }
            }
        }
    }

    public int getWorkersNum() {
        return uc.read(WORKERS_PREVIOUS);
    }

    public int getWorkersInactive() {
        return uc.read(WORKER_INACTIVE_PREVIOUS);
    }

    public int getBarracksNum() {
        return uc.read(BARRACKS_PREVIOUS) + getBarracksConsNum();
    }

    public int getWarriorsNum() {
        return uc.read(WARRIORS_PREVIOUS) + uc.read(WARRIORS_CONSTRUCTION);
    }

    public int getArchersNum() {
        return uc.read(ARCHERS_PREVIOUS) + uc.read(ARCHERS_CONSTRUCTION);
    }

    public int getKnightsNum() {
        return uc.read(KNIGHTS_PREVIOUS) + uc.read(KNIGHTS_CONSTRUCTION);
    }

    public int getBallistasNum() {
        return uc.read(BALLISTAS_PREVIOUS) + uc.read(BALLISTAS_CONSTRUCTION);
    }

    public int getTotalTroops() {
        return getWarriorsNum() + getArchersNum() + getKnightsNum() + getBallistasNum();
    }

    public int getBarracksConsNum() {
        return uc.read(BARRACKS_CONSTRUCTION);
    }

    public int getEnemiesSeenLastRound() {
        return uc.read(ENEMIES);
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

    public int getBarracksRound() {
        int barracksRound = uc.read(BARRACKS_ROUND);
        if (barracksRound == 0) {
            return roundBarracks;
        } else {
            return barracksRound;
        }
    }

    // PRIVATE

    private void rootUpdate() {
        // Updates workers
        uc.write(WORKERS_PREVIOUS, uc.read(WORKERS_CURRENT));
        uc.write(WORKERS_CURRENT, 0);
        uc.write(WORKER_INACTIVE_PREVIOUS, uc.read(WORKER_INACTIVE_CURRENT));
        uc.write(WORKER_INACTIVE_CURRENT, 0);

        // Updates barracks
        uc.write(BARRACKS_PREVIOUS, uc.read(BARRACKS_CURRENT));
        uc.write(BARRACKS_CURRENT, 0);

        // Updates warriors
        uc.write(WARRIORS_PREVIOUS, uc.read(WARRIORS_CURRENT));
        uc.write(WARRIORS_CURRENT, 0);

        // Updates archers
        uc.write(ARCHERS_PREVIOUS, uc.read(ARCHERS_CURRENT));
        uc.write(ARCHERS_CURRENT, 0);

        // Updates knights
        uc.write(KNIGHTS_PREVIOUS, uc.read(KNIGHTS_CURRENT));
        uc.write(KNIGHTS_CURRENT, 0);

        // Updates ballistas
        uc.write(BALLISTAS_PREVIOUS, uc.read(BALLISTAS_CURRENT));
        uc.write(BALLISTAS_CURRENT, 0);

        // Update enemies
        boolean foundID = false;
        int enemyID = uc.read(ENEMY_ID);
        for (int j = 1000; j < 1500; j++) {
            int ID = uc.read(j);
            if (ID == 0) {
                uc.write(ENEMIES, j - 1000);
                break;
            } else {
                uc.write(j, 0);
            }
            if (ID == enemyID) {
                foundID = true;
            }
        }
        if (!foundID) {
            uc.write(ENEMY_ID, 0);
        }

        uc.write(AT_LEAST_ONE_ENEMY, 0);

        uc.write(OAKS, 0);
        uc.write(NOT_FULL, 0);

        if (uc.read(ENEMY_BASES) == 0) {
            uc.write(ENEMY_BASES, startEnemies.length);
            if (startEnemies.length < 3) {
                uc.write(CLEARED3,1);
            }
            if (startEnemies.length < 2) {
                uc.write(CLEARED2,1);
            }
        }

        // Updates barracks in construction
        for (int i = 60; i < 100; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 25) {
                    uc.write(BARRACKS_CONSTRUCTION, uc.read(BARRACKS_CONSTRUCTION) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        // Updates warriors in construction
        for (int i = 100; i < 140; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(WARRIORS_CONSTRUCTION, uc.read(WARRIORS_CONSTRUCTION) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        // Updates archers in construction
        for (int i = 200; i < 240; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(ARCHERS_CONSTRUCTION, uc.read(ARCHERS_CONSTRUCTION) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        // Updates knights in construction
        for (int i = 300; i < 340; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(KNIGHTS_CONSTRUCTION, uc.read(KNIGHTS_CONSTRUCTION) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

        // Updates ballistas in construction
        for (int i = 400; i < 440; i = i + 2) {
            if (uc.read(i) != 0) {
                if (uc.read(i + 1) == 5) {
                    uc.write(BALLISTAS_CONSTRUCTION, uc.read(BALLISTAS_CONSTRUCTION) - 1);
                    uc.write(i, 0);
                    uc.write(i + 1, 0);
                }
                uc.write(i + 1, uc.read(i + 1) + 1);
            }
        }

    }

    public void objectiveCompleted() {
        uc.write(OBJECTIVE_COMPLETED, 1);
    }

    public boolean checkIfObjectiveCompleted() {
        return uc.read(OBJECTIVE_COMPLETED) == 1;
    }

    public void decideNextUnitType() {
        if(getTotalTroops() > getEnemiesSeenLastRound() * 5) {
            objective = UnitType.WORKER;
        } else {
            objective = UnitType.WARRIOR;
        }
    }

}

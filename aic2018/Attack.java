package aic2018;

public class Attack {

    private MemoryManager manager;
    private UnitController uc;
    private Utils utils = new Utils();
    private Pathfind pathfind;
    Location target;

    public Attack(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
        pathfind = new Pathfind(manager);
        aggressive = false;
        exploring = false;
    }

    private Location myLocation;
    private boolean aggressive;
    private boolean exploring;
    private Location nextForTarget;

    public void play() {
        myLocation = manager.myLocation;

        if (manager.getAtLeastOneEnemy() == 1) {
            aggressive = true;
        }

        if (manager.enemies.length != 0) {
            aggressive = false;
        }

        /*
        // choose one base as objective (can be implemented for when no enemies are seen)
        if (uc.read(manager.CLEARED1) == 0) {
            target = manager.startEnemies[0];
        } else if (uc.read(manager.CLEARED2) == 0) {
            target = manager.startEnemies[1];
        } else if (uc.read(manager.CLEARED3) == 0) {
            target = manager.startEnemies[2];
        }
        */

        Location newTarget = new Location(uc.read(manager.ENEMY_XLOC), uc.read(manager.ENEMY_YLOC));

        if ((myLocation.isEqual(newTarget) && uc.read(manager.ENEMY_ID) == 0) || uc.read(manager.RETARGET) == 1) {
            uc.write(manager.RETARGET, 1);
            target = null;
        } else {
            target = newTarget;
        }

        for (int i = 0; i < manager.startEnemies.length; i++) {
            if (myLocation.isEqual(manager.startEnemies[i]) && manager.enemies.length == 0) {
                if (i == 0) {
                    uc.write(manager.CLEARED1,1);
                } else if (i == 1) {
                    uc.write(manager.CLEARED2,1);
                } else if (i == 2) {
                    uc.write(manager.CLEARED3,1);
                }
            }
        }

        if (uc.read(manager.ENEMY_ID) == 0) {
            if (manager.enemies.length != 0) {
                uc.write(manager.ENEMY_ID, manager.enemies[0].getID());
                uc.write(manager.ENEMY_XLOC, manager.enemies[0].getLocation().x);
                uc.write(manager.ENEMY_YLOC, manager.enemies[0].getLocation().y);
                uc.write(manager.RETARGET, 0);
            }
        }

        tryAttackBestUnit();
        move();
        tryAttackBestUnit();
        tryAttackTree();
    }

    public void move() {
        if(aggressive) {
            Direction dir = pathfind.getNextLocationTarget(target);
            if(dir != null) nextForTarget = myLocation.add(dir);
        }
        else {
            nextForTarget = null;
        }

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

            int numWorkers = 0;

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
                else {
                    numWorkers++;
                }

                // stay at the front line
                if(numWorkers > 3) {
                    value -= 1;
                }
                else {
                    value += 1;
                }

                if(nextForTarget != null && nextForTarget.isEqual(plocs[j])) {
                    value += 3;
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
        int myLevel = uc.getLevel();
        int myAttack = 0;
        int warriorPassive = 0;
        boolean passive = false;
        boolean active = false;

        if (myLevel >= 2) {
            active = true;
        }
        if (myLevel >= 1) {
            passive = true;
        }

        if (manager.type == UnitType.WARRIOR) {
            if (myLevel == 3) {
                myAttack = GameConstants.WARRIOR_ATTACK_LEVEL3;
            } else {
                myAttack = GameConstants.WARRIOR_ATTACK;
            }
            if (passive) {
                warriorPassive = 2 * myAttack;
            }
        } else if (manager.type == UnitType.ARCHER) {
            if (myLevel == 3) {
                myAttack = GameConstants.ARCHER_ATTACK_LEVEL3;
            } else {
                myAttack = GameConstants.ARCHER_ATTACK;
            }
        } else if (manager.type == UnitType.KNIGHT) {
            if (myLevel == 3) {
                myAttack = GameConstants.KNIGHT_ATTACK_LEVEL3;
            } else {
                myAttack = GameConstants.KNIGHT_ATTACK;
            }
        } else if (manager.type == UnitType.BALLISTA) {
            if (myLevel == 3) {
                myAttack = GameConstants.BALLISTA_ATTACK_LEVEL3;
            } else {
                myAttack = GameConstants.BALLISTA_ATTACK;
            }
        }

        UnitInfo[] enemies = manager.enemies;
        if(enemies.length == 0 || !uc.canAttack()) return false;

        UnitInfo enemy = enemies[0];
        int maxHealth = 10001;

        if (manager.type == UnitType.WARRIOR && passive) {
            Location myMoves[] = utils.getPosibleMoves(uc);
        }

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

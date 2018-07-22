package workers;

import aic2018.*;

public class Barracks {

    private MemoryManager manager;
    private UnitController uc;

    public Barracks(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;

        if(manager.objective == UnitType.WORKER) {
            manager.decideNextUnitType();
        }
    }

    private Location myLocation;

    public UnitType decideNext() {
        int totalTroops = manager.getTotalTroops() + 1;
        int warriors = manager.getWarriorsNum();
        int knights = manager.getKnightsNum();
        int knightCounter = 0;
        int warriorCouner = 0;
        int archerCounter = 0;
        int enemyCounterArcher = 0;
        int enemyCounterKnight = 0;
        int enemyCounterWarrior = 0;
        UnitInfo units[] = manager.units;
        for (UnitInfo currentUnit : units) {
            UnitType type = currentUnit.getType();
            if (currentUnit.getTeam() == manager.allies) {
                if (type == UnitType.KNIGHT) {
                    knightCounter++;
                } else if (type == UnitType.WARRIOR) {
                    warriorCouner++;
                } else if (type == UnitType.ARCHER) {
                    archerCounter++;
                }
            } else {
                if (type == UnitType.KNIGHT) {
                    enemyCounterKnight++;
                } else if (type == UnitType.WARRIOR) {
                    enemyCounterWarrior++;
                } else if (type == UnitType.ARCHER) {
                    enemyCounterArcher++;
                }
            }
        }

        UnitType next = UnitType.KNIGHT;
        if (uc.senseWater(36).length > 20) {
            next = UnitType.ARCHER;
        } else if (manager.enemies.length == 0) {
            next = UnitType.ARCHER;
        } else if (knightCounter < 6) {
            next = UnitType.KNIGHT;
        } else {
            if (warriorCouner < 10) next = UnitType.WARRIOR;
            else { next = UnitType.ARCHER;}
        }
        return next;
    }

    public void play() {

        myLocation = manager.myLocation;

        if (manager.objective == UnitType.WORKER) {
            manager.decideNextUnitType();
        } else {
            UnitType next = decideNext();

            for (int i = 0; i < 8; ++i) {
                if (uc.canSpawn(manager.dirs[i], next)) {
                    uc.spawn(manager.dirs[i], next);

                    // Updates warriors in construction
                    if (next == UnitType.WARRIOR) {
                        uc.write(manager.WARRIORS_CONSTRUCTION, uc.read(manager.WARRIORS_CONSTRUCTION) + 1);
                        for (int j = 100; j < 140; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates archers in construction
                    if (next == UnitType.ARCHER) {
                        uc.write(manager.ARCHERS_CONSTRUCTION, uc.read(manager.ARCHERS_CONSTRUCTION) + 1);
                        for (int j = 200; j < 240; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (next == UnitType.KNIGHT) {
                        uc.write(manager.KNIGHTS_CONSTRUCTION, uc.read(manager.KNIGHTS_CONSTRUCTION) + 1);
                        for (int j = 300; j < 340; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (next == UnitType.BALLISTA) {
                        uc.write(manager.BALLISTAS_CONSTRUCTION, uc.read(manager.BALLISTAS_CONSTRUCTION) + 1);
                        for (int j = 400; j < 440; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
}

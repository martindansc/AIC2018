package finalBoss;

import aic2018.Location;
import aic2018.UnitController;
import aic2018.UnitType;

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

        UnitType next = UnitType.WARRIOR;
        if (warriors < 10) {
            next = UnitType.WARRIOR;
        } else {
            if (warriors * 15 / totalTroops <= 5) next = UnitType.WARRIOR;
            else if (manager.getArchersNum() * 10 / totalTroops <= 2) next = UnitType.ARCHER;
            else if (manager.getKnightsNum() * 10 / totalTroops <= 2) next = UnitType.KNIGHT;
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
                if (manager.enemies.length > 0) {
                    next = UnitType.WARRIOR;
                }
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

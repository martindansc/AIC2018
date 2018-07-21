package workers;

import aic2018.*;

public class Barracks {

    private MemoryManager manager;
    private UnitController uc;
    private Utils utils = new Utils();

    public Barracks(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;

        typeIndex = (int)(Math.random()*1);
    }

    int typeIndex;
    private Location myLocation;

    public void play() {

        myLocation = manager.myLocation;
        int totalTroops = manager.getTotalTroops();
        if (totalTroops < manager.getEnemiesSeenLastRound() * 5) {
            for (int i = 0; i < 8; ++i) {
                if (uc.canSpawn(manager.dirs[i], manager.objective)) {
                    uc.spawn(manager.dirs[i], manager.objective);

                    // Updates warriors in construction
                    if (manager.objective == UnitType.WARRIOR) {
                        uc.write(manager.WARRIORS_CONSTRUCTION, uc.read(manager.WARRIORS_CONSTRUCTION) + 1);
                        for (int j = 100; j < 200; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates archers in construction
                    if (manager.objective == UnitType.ARCHER) {
                        uc.write(manager.ARCHERS_CONSTRUCTION, uc.read(manager.ARCHERS_CONSTRUCTION) + 1);
                        for (int j = 200; j < 300; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.KNIGHT) {
                        uc.write(manager.KNIGHTS_CONSTRUCTION, uc.read(manager.KNIGHTS_CONSTRUCTION) + 1);
                        for (int j = 300; j < 400; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.BALLISTA) {
                        uc.write(manager.BALLISTAS_CONSTRUCTION, uc.read(manager.BALLISTAS_CONSTRUCTION) + 1);
                        for (int j = 400; j < 500; j = j + 2) {
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

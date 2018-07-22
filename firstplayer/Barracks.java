package firstplayer;

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

    public void play() {

        myLocation = manager.myLocation;

        if (manager.objective != UnitType.WORKER) {
            for (int i = 0; i < 8; ++i) {
                if (manager.enemies.length > 0) {
                    manager.objective = UnitType.WARRIOR;
                }
                if (uc.canSpawn(manager.dirs[i], manager.objective)) {
                    uc.spawn(manager.dirs[i], manager.objective);

                    // Updates warriors in construction
                    if (manager.objective == UnitType.WARRIOR) {
                        uc.write(manager.WARRIORS_CONSTRUCTION, uc.read(manager.WARRIORS_CONSTRUCTION) + 1);
                        for (int j = 100; j < 140; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates archers in construction
                    if (manager.objective == UnitType.ARCHER) {
                        uc.write(manager.ARCHERS_CONSTRUCTION, uc.read(manager.ARCHERS_CONSTRUCTION) + 1);
                        for (int j = 200; j < 240; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.KNIGHT) {
                        uc.write(manager.KNIGHTS_CONSTRUCTION, uc.read(manager.KNIGHTS_CONSTRUCTION) + 1);
                        for (int j = 300; j < 340; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.BALLISTA) {
                        uc.write(manager.BALLISTAS_CONSTRUCTION, uc.read(manager.BALLISTAS_CONSTRUCTION) + 1);
                        for (int j = 400; j < 440; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    manager.decideNextUnitType();
                }
            }
        }
    }
}

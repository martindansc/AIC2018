package aic2018;

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
                        uc.write(9, uc.read(9) + 1);
                        for (int j = 100; j < 200; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates archers in construction
                    if (manager.objective == UnitType.ARCHER) {
                        uc.write(13, uc.read(13) + 1);
                        for (int j = 200; j < 300; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.KNIGHT) {
                        uc.write(30, uc.read(30) + 1);
                        for (int j = 300; j < 400; j = j + 2) {
                            if (uc.read(j) == 0) {
                                uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                                break;
                            }
                        }
                    }

                    // Updates knights in construction
                    if (manager.objective == UnitType.BALLISTA) {
                        uc.write(33, uc.read(33) + 1);
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

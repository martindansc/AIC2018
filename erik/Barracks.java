package erik;

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

        //If barracks do a random unit between warrior, archer and knight
        //Getting the type associated to typeIndex
        UnitType type = UnitType.values()[2+typeIndex];
        myLocation = manager.myLocation;
        //try to spawn a unit of the given type, if successful reset type.
        for (int i = 0; i < 8; ++i) {
            if (uc.canSpawn(manager.dirs[i], type)) {
                uc.spawn(manager.dirs[i], type);
                typeIndex = (int) (Math.random() * 1);

                // Updates warriors in construction
                uc.write(9, uc.read(9) + 1);
                for (int j = 40; j < 100; j = j + 2) {
                    if (uc.read(j) == 0) {
                        uc.write(j, uc.senseUnit(myLocation.add(manager.dirs[i])).getID());
                        break;
                    }
                }
            }
        }
    }
}

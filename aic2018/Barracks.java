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

    public void play() {

        //If barracks do a random unit between warrior, archer and knight
        //Getting the type associated to typeIndex
        UnitType type = UnitType.values()[2+typeIndex];

        //try to spawn a unit of the given type, if successful reset type.
        for (int i = 0; i < 8; ++i) if (uc.canSpawn(manager.dirs[i], type)){
            uc.spawn(manager.dirs[i], type);
            typeIndex = (int)(Math.random()*1);
        }
    }

}

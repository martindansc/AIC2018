package version1;

import aic2018.Location;
import aic2018.UnitController;
import aic2018.UnitType;

public class Barracks {

    private MemoryManager manager;
    private UnitController uc;

    public Barracks(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
    }

    public void play() {
    }
}

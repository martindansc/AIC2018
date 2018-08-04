package version1;

import aic2018.*;

public class Attack {

    private MemoryManager manager;
    private UnitController uc;

    public Attack(MemoryManager memoryManager) {
        this.manager = memoryManager;
        uc = memoryManager.uc;
    }

    public void play() {
    }
}

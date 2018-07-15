package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    private int startLocationsArray = 12;
    private int endLocationsArray = 1012;

    public UnitController uc;
    private int counterMod2;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        counterMod2 = 0;

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();
    }

    public void update() {

        // update if I'm root
        uc.write(AMIROOT + counterMod2, 0);
        if(uc.read(AMIROOT + 1 - counterMod2) == 0) {
            root = true;
            uc.write(AMIROOT + 1 - counterMod2, 1);
        }
        else {
            root = false;
        }

        if(root) rootUpdate();

        // update location
        int locationsArray = startLocationsArray;
        if(counterMod2 == 1) locationsArray = endLocationsArray + 1;
        for(int i = locationsArray; i <
                locationsArray + endLocationsArray - startLocationsArray; i += 2) {
            if(uc.read(i) == 0) {
                Location loc = uc.getLocation();
                uc.write(i, loc.x);
                uc.write(i + 1, loc.y);
                break;
            }
        }

        // update num units
        uc.write(counterMod2, uc.read(counterMod2%2) + 1);

        if(uc.getType() == UnitType.WORKER) {
            uc.write(counterMod2 + 2, uc.read(counterMod2 + 2) + 1);
        }

        counterMod2 = (counterMod2 + 1)%2;
    }

    public int getUnitNum() {
        return uc.read(counterMod2 + 1);
    }

    public int getWorkersNum() {
        return uc.read(counterMod2 + 2);
    }

    // PRIVATE

    private void rootUpdate() {
        int locationsArray = startLocationsArray;
        if(counterMod2 == 0) locationsArray = endLocationsArray + 1;
        for(int i = locationsArray; i <
                locationsArray + endLocationsArray - startLocationsArray; i += 2) {
            uc.write(i, 0);
        }
    }

}

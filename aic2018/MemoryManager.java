package aic2018;

public class MemoryManager {

    public UnitController uc;
    private int counterMod2;

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

        // check for root

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


}

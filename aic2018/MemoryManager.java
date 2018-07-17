package aic2018;

public class MemoryManager {

    private int AMIROOT = 10;

    public UnitController uc;
    private int counterMod2;

    public boolean root;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;

    public int round;
    public int resources;
    public UnitInfo[] enemies;
    public TreeInfo[] trees;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        round = uc.getRound();

        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();
    }

    public void update() {

        round = uc.getRound();
        counterMod2 = round%2;
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        resources = uc.getResources();

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

        // update num units
        uc.write(counterMod2, uc.read(counterMod2) + 1);

        if(uc.getType() == UnitType.WORKER) {
            uc.write(counterMod2 + 2, uc.read(counterMod2 + 2) + 1);
        }

    }

    public int getUnitNum() {
        return uc.read(counterMod2 + 1 - counterMod2);
    }

    public int getWorkersNum() {
        return uc.read(counterMod2 + 2);
    }

    // PRIVATE

    private void rootUpdate() {
        uc.write(counterMod2, 0);
    }

}

package version1;

import aic2018.*;

public class MemoryManager {

    int BARRACKS_ROUND = 0;

    public UnitController uc;
    public Raycast waterRaycast;

    public Team opponent;
    public Team allies;
    public Direction[] dirs;
    public Location[] startEnemies;

    public int round;
    public int resources;
    public UnitInfo[] units;
    public UnitInfo[] enemies;
    public TreeInfo[] trees;
    public TreeInfo[] nearTrees;
    public Location myLocation;
    public UnitType type;

    State state;

    public MemoryManager(UnitController uc) {
        this.uc = uc;
        opponent = uc.getOpponent();
        allies = uc.getTeam();
        dirs = Direction.values();
        type = uc.getType();

        state = new State();
        waterRaycast = new Raycast(uc, uc::senseWaterAtLocation);

        // set initial barracks round
        if(getBarracksRound() == 0) {
            setBarracksRound(100);
        }
    }

    public void update() {
        myLocation = uc.getLocation();
        round = uc.getRound();
        enemies = uc.senseUnits(uc.getOpponent());
        trees = uc.senseTrees();
        nearTrees = uc.senseTrees(2);
        resources = uc.getResources();
        units = uc.senseUnits();
    }

    public void buyPointsIfNeeded() {
        if (uc.canBuyVP( GameConstants.VICTORY_POINTS_MILESTONE - allies.getVictoryPoints())){
            uc.buyVP(GameConstants.VICTORY_POINTS_MILESTONE - allies.getVictoryPoints());
        }

        if (uc.getRound() == GameConstants.MAX_TURNS) {
            uc.buyVP( uc.getResources()/(GameConstants.BASE_VICTORY_POINTS_COST +
                    GameConstants.MAX_TURNS/GameConstants.VICTORY_POINTS_INFLATION_ROUNDS));
        }
    }

    public void pickVictoryPoints() {
        Direction[] dirs = Direction.values();
        for(int i = 0; i < 8; i++) {
            if(uc.canGatherVPs(dirs[i])) {
                uc.gatherVPs(dirs[i]);
            }
        }
    }

    public Location[] getAdjacentLocations(Location loc) {
        Location locs[] = new Location[8];

        Direction[] dirs = Direction.values();
        for(int i = 0; i < 8; i++) {
            locs[i] = (loc.add(dirs[i]));
        }

        return locs;
    }

    public int isExtreme(Location loc) {
        Location[] newPlaces = getAdjacentLocations(loc);
        for (int i = 0; i < newPlaces.length; i++) {
            if (uc.canSenseLocation(newPlaces[i]) && uc.isOutOfMap(newPlaces[i])) {
                return 5;
            }

            if (uc.canSenseLocation(newPlaces[i]) && uc.senseWaterAtLocation(newPlaces[i])) {
                return 1;
            }
        }
        return 0;
    }

    public int getBarracksRound() {
        return uc.read(BARRACKS_ROUND);
    }

    public void setBarracksRound(int round) {
        uc.write(BARRACKS_ROUND, round);
    }
}

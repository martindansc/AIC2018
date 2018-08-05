package raycast;

import aic2018.Direction;
import aic2018.GameConstants;
import aic2018.Location;
import aic2018.UnitController;

public class UnitPlayer {

    public void run(UnitController uc) {

        Raycast raycast = new Raycast(uc, uc::senseWaterAtLocation);

        while (true) {

            if(uc.canMove(Direction.values()[0])) uc.move(Direction.values()[0]);

            Location[] locs = uc.getVisibleLocations(GameConstants.WORKER_SIGHT_RANGE_SQUARED);

            Location myLocation = uc.getLocation();
            for(int i = 0; i < locs.length; i++) {
                raycast.fastRay(myLocation, locs[i]);
            }

            if(uc.getRound() > 3) return;

            uc.yield();
        }

    }
}

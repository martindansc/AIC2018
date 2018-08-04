package nullplayer;

import aic2018.UnitController;

public class UnitPlayer {

    public void run(UnitController uc) {
        while (true) {
            uc.yield();
        }

    }
}

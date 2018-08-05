package raycast;

import aic2018.Direction;
import aic2018.Location;
import aic2018.UnitController;
import java.util.function.Function;

public class Raycast {

    private UnitController uc;
    private Function function;

    public Raycast(UnitController unitController, Function<Location, Boolean> function) {
        uc = unitController;
        this.function = function;
    }

    public boolean ray(Location var1, Location var2) {
        double distance = var1.distanceSquared(var2);

        if(distance >= 100) {
            uc.println("Bad usage array, distance is too large");
            return false;
        }

        if(distance > 4*4 && uc.getEnergyLeft() > 10000) {
            return slowRay(var1, var2);
        }
        else {
            return fastRay(var1, var2);
        }
    }

    public boolean fastRay(Location var1, Location var2) {

        Direction dir = var1.directionTo(var2);
        if(dir.isEqual(Direction.ZERO)) return false;

        Location location = var1.add(dir);

        do {

            if(callFunction(location)) {
                return true;
            }

            location = location.add(location.directionTo(var2));

        } while (!location.isEqual(var2));


        return false;
    }

    public boolean slowRay(Location var1, Location var2) {
        int x0 = var1.x;
        int x1 = var2.x;
        int y0 = var1.y;
        int y1 = var2.y;

        if (Math.abs(y1 - y0) < Math.abs(x1 - x0)) {
            return x0 > x1 ? lineLow(x1, y1, x0, y0) : lineLow(x0,y0, x1, y1);
        }

        return y0 > y1 ? lineHigh(x1, y1, x0, y0) : lineHigh(x0,y0, x1, y1);

    }

    private boolean lineLow(int x0, int y0, int x1, int y1) {
        // init params
        int dx = x1 - x0;
        int dy = y1 - y0;
        int yi = 1;
        if (dy < 0) {
            yi = -1;
            dy = -dy;
        }

        int D = 2*dy - dx;
        int y = y0;

        // go through all the locations
        for(int i = x0; i <= x1; i++) {

            if(!((i == x0 && y == y0) || (i == x1 && y == y1))) {
                Location location = new Location(i, y);
                if(callFunction(location)) {
                    return true;
                }
            }

            if(D > 0) {
                y = y + yi;
                D = D - 2*dx;
            }

            D = D + 2*dy;
        }

        return false;
    }

    private boolean callFunction(Location location) {
        return (Boolean) function.apply(location);
    }

    private boolean lineHigh(int x0, int y0, int x1, int y1) {
        // init params
        int dx = x1 - x0;
        int dy = y1 - y0;
        int xi = 1;
        if (dx < 0) {
            xi = -1;
            dx = -dx;
        }

        int D = 2*dx - dy;
        int x = x0;

        // go through all the locations
        for(int i = y0; i <= y1; i++) {

            if(!((x == x0 && i == y0) || (x == x1 && i == y1))) {
                Location location = new Location(x, i);
                if(callFunction(location)) {
                    return true;
                }
            }

            if (D > 0) {
                x = x + xi;
                D = D - 2*dy;
            }
            D = D + 2*dx;
        }

        return false;
    }





}

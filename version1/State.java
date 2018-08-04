package version1;

public class State {
    String state;

    public State() {
        state = "default";
    }

    public boolean canPlantTree() {
        return "farming".equals(state);
    }

    public void setState(String state) {
        this.state = state;
    }
}

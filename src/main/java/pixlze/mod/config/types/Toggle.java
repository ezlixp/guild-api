package pixlze.mod.config.types;

import java.util.ArrayList;

public class Toggle extends Option {
    private boolean value = false;
    private ArrayList<Option> children; // render if toggled on

    public Toggle(String name) {
        super(name, "Toggle");
        super.name = name;
    }

    public Toggle(String name, ArrayList<Option> children) {
        super(name, "Toggle");
        this.name = name;
        this.children = children;
    }

    public boolean getState() {
        return value;
    }

    public void setState(boolean state) {
        this.value = state;
    }

    public ArrayList<Option> getChildren() {
        return children;
    }

}

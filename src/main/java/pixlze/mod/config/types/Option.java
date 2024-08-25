package pixlze.mod.config.types;

public abstract class Option {
    public String type;
    public String name;


    public Option(String name, String type) {
        this.name = name;
        this.type = type;
    }


}

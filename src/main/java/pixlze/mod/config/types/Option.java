package pixlze.mod.config.types;

public abstract class Option {
    public String type;
    public String id;
    public String name;


    public Option(String name, String id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }


}

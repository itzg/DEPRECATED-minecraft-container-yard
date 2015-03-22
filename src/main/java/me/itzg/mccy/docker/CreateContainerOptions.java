package me.itzg.mccy.docker;

/**
 * @author Geoff Bourne
 * @since 3/21/2015
 */
public class CreateContainerOptions {
    private String name;

    public CreateContainerOptions withName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

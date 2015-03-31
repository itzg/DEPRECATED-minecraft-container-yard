package me.itzg.mccy.docker;

/**
 * @author Geoff Bourne
 * @since 3/21/2015
 */
public class CreateContainerOptions {
    private String name;

    private boolean pullLatestRequested;

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

    public boolean isPullLatestRequested() {
        return pullLatestRequested;
    }

    /**
     *
     * @param pullLatestRequested
     */
    public void setPullLatestRequested(boolean pullLatestRequested) {
        this.pullLatestRequested = pullLatestRequested;
    }

    public CreateContainerOptions withPullLatest(boolean value) {
        this.pullLatestRequested = value;
        return this;
    }
}

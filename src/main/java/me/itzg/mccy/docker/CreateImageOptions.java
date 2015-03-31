package me.itzg.mccy.docker;

import org.springframework.core.style.ToStringCreator;

/**
 * @author Geoff Bourne
 * @since 3/29/2015
 */
public class CreateImageOptions {
    private String fromImage;

    private String fromSrc;

    private String repo;

    private String tag;

    private String registry;

    public CreateImageOptions() {
    }

    public CreateImageOptions(String fromImage) {
        this.fromImage = fromImage;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("fromImage", fromImage)
                .append("fromSrc", fromSrc)
                .append("repo", repo)
                .append("tag", tag)
                .append("registry", registry)
                .toString();
    }

    public String getFromImage() {
        return fromImage;
    }

    public void setFromImage(String fromImage) {
        this.fromImage = fromImage;
    }

    public String getFromSrc() {
        return fromSrc;
    }

    public void setFromSrc(String fromSrc) {
        this.fromSrc = fromSrc;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }
}

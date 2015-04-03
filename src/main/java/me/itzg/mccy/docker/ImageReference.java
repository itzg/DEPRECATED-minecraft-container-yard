package me.itzg.mccy.docker;

/**
 * Identifies a Docker image by its parts.
 *
 * @author Geoff Bourne
 * @since 3/13/2015
 */
public class ImageReference {

    private String image;
    private String tag;

    public static ImageReference fromIdentifier(String identifier) {
        final String[] parts = identifier.split(":");

        final ImageReference imageReference = new ImageReference();
        imageReference.image = parts[0];
        if (parts.length > 1) {
            imageReference.tag = parts[1];
        }
        return imageReference;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean hasTag() {
        return tag != null;
    }
}

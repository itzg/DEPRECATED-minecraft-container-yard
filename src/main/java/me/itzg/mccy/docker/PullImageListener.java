package me.itzg.mccy.docker;

import me.itzg.docker.types.images.ImagesCreateResponse;

/**
 * @author Geoff Bourne
 * @since 3/29/2015
 */
public interface PullImageListener {
    void handlePullImageProgress(ImagesCreateResponse progress);

    void handlePullException(Exception e);

    void handlePullFinished(CreateImageOptions options);
}

package me.itzg.mccy.docker;

/**
 * @author Geoff Bourne
 * @since 3/22/2015
 */
public class DockerClientException extends Exception {
    private boolean serverSideError;

    public DockerClientException() {
    }

    public DockerClientException(String message) {
        super(message);
    }

    public DockerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerClientException(Throwable cause) {
        super(cause);
    }

    public DockerClientException setServerSideError(boolean serverSideError) {
        this.serverSideError = serverSideError;
        return this;
    }

    public boolean isServerSideError() {
        return serverSideError;
    }
}

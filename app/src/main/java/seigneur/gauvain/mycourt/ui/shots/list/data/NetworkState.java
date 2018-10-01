package seigneur.gauvain.mycourt.ui.shots.list.data;

public class NetworkState {

    private Status status;

    private String message;

    private NetworkState(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    private NetworkState(Status status) {
        this.status = status;
    }

    public static NetworkState LOADED = new NetworkState(Status.SUCCESS);

    public static NetworkState LOADING = new NetworkState(Status.RUNNING);

    public static NetworkState error(String message) {
        return new NetworkState(Status.FAILED, message == null ? "unknown error" : message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}

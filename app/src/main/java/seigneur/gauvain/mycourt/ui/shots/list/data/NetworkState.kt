package seigneur.gauvain.mycourt.ui.shots.list.data

class NetworkState {

    var status: Status? = null
        private set

    var message: String =""

    private constructor(status: Status, message: String) {
        this.status = status
        this.message = message
    }

    private constructor(status: Status) {
        this.status = status
    }

    companion object {

        var LOADED = NetworkState(Status.SUCCESS)

        var LOADING = NetworkState(Status.RUNNING)

        fun error(message: String?): NetworkState {
            return NetworkState(Status.FAILED, message ?: "unknown error")
        }
    }

}

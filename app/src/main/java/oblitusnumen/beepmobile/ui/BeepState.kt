package oblitusnumen.beepmobile.ui

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

object BeepState {
    val connected = MutableStateFlow(false)
    val receivedMessages = MutableStateFlow(listOf<String>())
    val users = MutableStateFlow(mapOf<UUID, String>())

    fun setConnected(value: Boolean) {
        connected.value = value
    }

    fun setReceivedMessages(value: List<String>) {
        receivedMessages.value = value
    }

    fun setUsers(value: Map<UUID, String>) {
        users.value = value
    }
}

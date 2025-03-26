package oblitusnumen.beepmobile.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class BeepViewModel : ViewModel() {
    val connected: StateFlow<Boolean> = BeepState.connected
    val receivedMessages: StateFlow<List<String>> = BeepState.receivedMessages
    val users: StateFlow<Map<UUID, String>> = BeepState.users
}
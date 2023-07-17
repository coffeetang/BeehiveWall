import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Toolkit

val screensize = Toolkit.getDefaultToolkit().screenSize

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication, state = WindowState(
            size = DpSize(screensize.width.dp, screensize.height.dp),
            position = WindowPosition(Alignment.Center)
        ), transparent = true, undecorated = true
    ) {
        val switch = remember { mutableStateOf(false) }
        BeehiveWall(switch.value) {
            exitApplication()
        }
        MenuBar {
            Menu("设置") {
                Item("打开屏保", shortcut = KeyShortcut(Key.V, ctrl = true)) {
                    switch.value = true
                }
                Item("关闭屏保", shortcut = KeyShortcut(Key.J, ctrl = true)) {
                    switch.value = false
                }
            }
        }
    }
}

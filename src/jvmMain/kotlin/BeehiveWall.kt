import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun BeehiveWall(switch: Boolean, callback: () -> Unit) {
    val width = remember { mutableStateOf(800f) }
    val height = remember { mutableStateOf(800f) }
    val anglist = Array(6) { 60f * it }
    val radius = 30f
    val xUnit = radius * 3
    val xLength = (width.value / xUnit).toInt() + 1
    val xList = Array(xLength) { radius + it * xUnit }
    val xList2 = Array(xLength) { -radius / 2 + it * xUnit }
    val yUnit = sqrt(radius * radius - radius * radius / 4) * 2
    val yLength = (height.value / yUnit).toInt() + 1
    val yList = Array(yLength) { yUnit * it }
    val yList2 = Array(yLength) { yUnit / 2 + yUnit * it }
    val pointList = remember { mutableStateOf(mutableListOf<Offset>()) }
    val pathList = remember { mutableStateOf(mutableListOf<Path>()) }
    val gridSwitch = remember { mutableStateOf(0) }
    val dashState = remember { mutableStateOf(false) }
    val colorList = listOf(
        Color(0xFFFF8723), Color(0xFFFF966B),
        Color(0xFFFFBA23), Color(0xFFF2C371),
    )

    val gradientColorState = remember { mutableStateOf(false) }
    val pathState = remember { mutableStateOf(false) }
    if (switch) {
        gridSwitch.value = 1
    } else if (gridSwitch.value == 1) {
        gradientColorState.value = false
        pathState.value = false
    }
    val gridColor = animateColorAsState(
        if (gridSwitch.value == 0) Color.Transparent else Color.White, finishedListener = {
            if (gridSwitch.value == 1) {
                dashState.value = true
            } else {
                callback()
            }
        })

    val dash = animateFloatAsState(if (dashState.value) 0f else radius,
        animationSpec = TweenSpec(1500), finishedListener = {
            if (dashState.value) {
                pathState.value = true
            } else {
                gridSwitch.value = 0
            }
        })
    val pathIndex = animateIntAsState(
        if (pathState.value) pathList.value.size else 0,
        animationSpec = TweenSpec(1000), finishedListener = {
            if (pathState.value) {
                gradientColorState.value = true
            } else {
                dashState.value = false
            }
        })

    if (pointList.value.isNotEmpty()) {
        LaunchedEffect(gradientColorState.value) {
            flow {
                while (true) {
                    emit(1)
                    delay(1000)
                }
            }.collect {
                if (pathList.value.size != pointList.value.size) {
                    for (pp in pointList.value) {
                        val path = Path()
                        path.moveTo(pointX(radius, pp.x, anglist[0]), pointY(radius, pp.y, anglist[0]))
                        for (index in anglist.indices) {
                            if (index < anglist.lastIndex) {
                                path.lineTo(
                                    pointX(radius, pp.x, anglist[index + 1]),
                                    pointY(radius, pp.y, anglist[index + 1])
                                )
                            }
                        }
                        pathList.value.add(path)
                    }
                }
                pathList.value.shuffle()
            }
        }

    }
    val transition = rememberInfiniteTransition()
    val gradientAngle = transition.animateFloat(
        0f, 360f, animationSpec = InfiniteRepeatableSpec(
            tween(durationMillis = 3000)
        )
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        width.value = size.width
        height.value = size.height
        pointList.value.clear()
        for (path in 0..pathList.value.lastIndex) {
            if (path < pathIndex.value) {
                if (gradientColorState.value) {
                    drawPath(
                        pathList.value[path],
                        brush = Brush.linearGradient(
                            colors = colorList,
                            start = Offset(
                                pointX(height.value / 2, width.value / 2, gradientAngle.value),
                                pointY(height.value / 2, height.value / 2, gradientAngle.value)
                            ),
                            end = Offset(
                                pointX(height.value / 2, width.value / 2, gradientAngle.value + 180f),
                                pointY(height.value / 2, height.value / 2, gradientAngle.value + 180f)
                            ),
                            tileMode = TileMode.Mirror
                        )
                    )
                } else {
                    drawPath(pathList.value[path], Color(0xFFF2C371))
                }
            }
        }
        for (x in xList) {
            for (y in yList) {
                pointList.value.add(Offset(x, y))
                for (ang in anglist) {
                    drawLine(
                        gridColor.value, strokeWidth = 5f,
                        start = Offset(pointX(radius, x, ang), pointY(radius, y, ang)),
                        end = Offset(pointX(radius, x, ang + 60f), pointY(radius, y, ang + 60f)),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf((radius - dash.value) / 2, dash.value))
                    )
                }
            }
        }
        for (x in xList2) {
            for (y in yList2) {
                pointList.value.add(Offset(x, y))
                for (ang in anglist) {
                    drawLine(
                        gridColor.value, strokeWidth = 5f,
                        start = Offset(pointX(radius, x, ang), pointY(radius, y, ang)),
                        end = Offset(pointX(radius, x, ang + 60f), pointY(radius, y, ang + 60f)),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf((radius - dash.value) / 2, dash.value))
                    )
                }
            }
        }
    }
}

fun pointX(radius: Float, centerX: Float, fl: Float): Float {
    val angle = Math.toRadians(fl.toDouble())
    return centerX - cos(angle).toFloat() * (radius)
}

fun pointY(radius: Float, centerY: Float, fl: Float): Float {
    val angle = Math.toRadians(fl.toDouble())
    return centerY - sin(angle).toFloat() * (radius)
}
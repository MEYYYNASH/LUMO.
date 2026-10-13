package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object LumoIcons {

    private fun materialVector(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()

    val MusicNote: ImageVector by lazy {
        materialVector("MusicNote") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f)
                verticalLineToRelative(10.55f)
                curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2f, -0.55f)
                curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f)
                reflectiveCurveToRelative(1.79f, 4f, 4f, 4f)
                reflectiveCurveToRelative(4f, -1.79f, 4f, -4f)
                verticalLineTo(7f)
                horizontalLineToRelative(4f)
                verticalLineTo(3f)
                horizontalLineToRelative(-6f)
                close()
            }
        }
    }

    val GraphicEq: ImageVector by lazy {
        materialVector("GraphicEq") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 18f)
                horizontalLineToRelative(2f)
                verticalLineTo(6f)
                horizontalLineTo(7f)
                verticalLineToRelative(12f)
                close()
                moveTo(11f, 22f)
                horizontalLineToRelative(2f)
                verticalLineTo(2f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(20f)
                close()
                moveTo(3f, 14f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-4f)
                horizontalLineTo(3f)
                verticalLineToRelative(4f)
                close()
                moveTo(15f, 18f)
                horizontalLineToRelative(2f)
                verticalLineTo(6f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(12f)
                close()
                moveTo(19f, 10f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-4f)
                horizontalLineToRelative(-2f)
                close()
            }
        }
    }

    val Download: ImageVector by lazy {
        materialVector("Download") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f)
                horizontalLineToRelative(-4f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineToRelative(6f)
                horizontalLineTo(5f)
                lineToRelative(7f, 7f)
                lineToRelative(7f, -7f)
                close()
                moveTo(5f, 18f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(14f)
                verticalLineToRelative(-2f)
                horizontalLineTo(5f)
                close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        materialVector("Pause") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 19f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineTo(6f)
                verticalLineToRelative(14f)
                close()
                moveTo(14f, 5f)
                verticalLineToRelative(14f)
                horizontalLineToRelative(4f)
                verticalLineTo(5f)
                horizontalLineToRelative(-4f)
                close()
            }
        }
    }

    val SkipPrevious: ImageVector by lazy {
        materialVector("SkipPrevious") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 6f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(12f)
                horizontalLineTo(6f)
                close()
                moveTo(9.5f, 12f)
                lineToRelative(8.5f, 6f)
                verticalLineTo(6f)
                close()
            }
        }
    }

    val SkipNext: ImageVector by lazy {
        materialVector("SkipNext") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 18f)
                lineToRelative(8.5f, -6f)
                lineTo(6f, 6f)
                verticalLineToRelative(12f)
                close()
                moveTo(16f, 6f)
                verticalLineToRelative(12f)
                horizontalLineToRelative(2f)
                verticalLineTo(6f)
                horizontalLineToRelative(-2f)
                close()
            }
        }
    }

    val Shuffle: ImageVector by lazy {
        materialVector("Shuffle") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.59f, 9.17f)
                lineTo(5.41f, 4f)
                lineTo(4f, 5.41f)
                lineToRelative(5.17f, 5.17f)
                lineToRelative(1.42f, -1.41f)
                close()
                moveTo(14.5f, 4f)
                lineToRelative(2.04f, 2.04f)
                lineTo(4f, 18.59f)
                lineTo(5.41f, 20f)
                lineTo(17.96f, 7.46f)
                lineTo(20f, 9.5f)
                verticalLineTo(4f)
                horizontalLineToRelative(-5.5f)
                close()
                moveTo(14.83f, 13.41f)
                lineToRelative(-1.41f, 1.41f)
                lineToRelative(3.13f, 3.13f)
                lineTo(14.5f, 20f)
                horizontalLineTo(20f)
                verticalLineToRelative(-5.5f)
                lineToRelative(-2.04f, 2.04f)
                lineToRelative(-3.13f, -3.13f)
                close()
            }
        }
    }

    val Repeat: ImageVector by lazy {
        materialVector("Repeat") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 7f)
                horizontalLineToRelative(10f)
                verticalLineToRelative(3f)
                lineToRelative(4f, -4f)
                lineToRelative(-4f, -4f)
                verticalLineToRelative(3f)
                horizontalLineTo(5f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(2f)
                verticalLineTo(7f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(7f)
                verticalLineToRelative(-3f)
                lineToRelative(-4f, 4f)
                lineToRelative(4f, 4f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(4f)
                close()
            }
        }
    }

    val RepeatOne: ImageVector by lazy {
        materialVector("RepeatOne") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 7f)
                horizontalLineToRelative(10f)
                verticalLineToRelative(3f)
                lineToRelative(4f, -4f)
                lineToRelative(-4f, -4f)
                verticalLineToRelative(3f)
                horizontalLineTo(5f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(2f)
                verticalLineTo(7f)
                close()
                moveTo(17f, 17f)
                horizontalLineTo(7f)
                verticalLineToRelative(-3f)
                lineToRelative(-4f, 4f)
                lineToRelative(4f, 4f)
                verticalLineToRelative(-3f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(-6f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(4f)
                close()
                moveTo(13f, 15f)
                verticalLineTo(9f)
                horizontalLineToRelative(-1f)
                lineToRelative(-2f, 1f)
                verticalLineToRelative(1f)
                horizontalLineToRelative(1.5f)
                verticalLineToRelative(4f)
                horizontalLineTo(13f)
                close()
            }
        }
    }

    val Videocam: ImageVector by lazy {
        materialVector("Videocam") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 10.5f)
                verticalLineTo(7f)
                curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f)
                horizontalLineTo(4f)
                curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
                horizontalLineToRelative(12f)
                curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
                verticalLineToRelative(-3.5f)
                lineToRelative(4f, 4f)
                verticalLineToRelative(-11f)
                lineToRelative(-4f, 4f)
                close()
            }
        }
    }

    val QueueMusic: ImageVector by lazy {
        materialVector("QueueMusic") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 6f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(12f)
                verticalLineTo(6f)
                close()
                moveTo(15f, 10f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(-2f)
                close()
                moveTo(3f, 16f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-2f)
                horizontalLineTo(3f)
                verticalLineToRelative(2f)
                close()
                moveTo(17f, 6f)
                verticalLineToRelative(8.18f)
                curveTo(16.69f, 14.07f, 16.35f, 14f, 16f, 14f)
                curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f)
                reflectiveCurveToRelative(1.34f, 3f, 3f, 3f)
                reflectiveCurveToRelative(3f, -1.34f, 3f, -3f)
                verticalLineTo(8f)
                horizontalLineToRelative(3f)
                verticalLineTo(6f)
                horizontalLineToRelative(-5f)
                close()
            }
        }
    }

    val History: ImageVector by lazy {
        materialVector("History") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f)
                curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f)
                horizontalLineTo(1f)
                lineToRelative(3.89f, 3.89f)
                lineToRelative(0.07f, 0.14f)
                lineTo(9f, 12f)
                horizontalLineTo(6f)
                curveToRelative(0f, -3.87f, 3.13f, -7f, 7f, -7f)
                reflectiveCurveToRelative(7f, 3.13f, 7f, 7f)
                reflectiveCurveToRelative(-3.13f, 7f, -7f, 7f)
                curveToRelative(-1.93f, 0f, -3.68f, -0.79f, -4.94f, -2.06f)
                lineToRelative(-1.42f, 1.42f)
                curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
                curveToRelative(4.97f, 0f, 9f, -4.03f, 9f, -9f)
                reflectiveCurveToRelative(-4.03f, -9f, -9f, -9f)
                close()
                moveTo(12f, 8f)
                verticalLineToRelative(5f)
                lineToRelative(4.28f, 2.54f)
                lineToRelative(0.72f, -1.21f)
                lineToRelative(-3.5f, -2.08f)
                verticalLineTo(8f)
                horizontalLineTo(12f)
                close()
            }
        }
    }

    val PlaylistAdd: ImageVector by lazy {
        materialVector("PlaylistAdd") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14f, 10f)
                horizontalLineTo(2f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(12f)
                verticalLineToRelative(-2f)
                close()
                moveTo(14f, 6f)
                horizontalLineTo(2f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(12f)
                verticalLineTo(6f)
                close()
                moveTo(18f, 14f)
                verticalLineToRelative(-4f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(-4f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-2f)
                horizontalLineToRelative(-4f)
                close()
                moveTo(2f, 16f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-2f)
                horizontalLineTo(2f)
                verticalLineToRelative(2f)
                close()
            }
        }
    }

    val VerifiedUser: ImageVector by lazy {
        materialVector("VerifiedUser") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f)
                lineTo(3f, 5f)
                verticalLineToRelative(6f)
                curveToRelative(0f, 5.55f, 3.84f, 10.74f, 9f, 12f)
                curveToRelative(5.16f, -1.26f, 9f, -6.45f, 9f, -12f)
                verticalLineTo(5f)
                lineToRelative(-9f, -4f)
                close()
                moveTo(10f, 17f)
                lineToRelative(-4f, -4f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10f, 14.17f)
                lineToRelative(6.59f, -6.59f)
                lineTo(18f, 9f)
                lineToRelative(-8f, 8f)
                close()
            }
        }
    }

    val RadioButtonChecked: ImageVector by lazy {
        materialVector("RadioButtonChecked") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
                reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
                reflectiveCurveToRelative(5f, -2.24f, 5f, -5f)
                reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
                close()
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
                reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
                reflectiveCurveTo(17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f)
                reflectiveCurveToRelative(3.58f, -8f, 8f, -8f)
                reflectiveCurveToRelative(8f, 3.58f, 8f, 8f)
                reflectiveCurveToRelative(-3.58f, 8f, -8f, 8f)
                close()
            }
        }
    }

    val RadioButtonUnchecked: ImageVector by lazy {
        materialVector("RadioButtonUnchecked") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
                reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
                reflectiveCurveTo(17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f)
                reflectiveCurveToRelative(3.58f, -8f, 8f, -8f)
                reflectiveCurveToRelative(8f, 3.58f, 8f, 8f)
                reflectiveCurveToRelative(-3.58f, 8f, -8f, 8f)
                close()
            }
        }
    }

    val ArrowUpward: ImageVector by lazy {
        materialVector("ArrowUpward") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 12f)
                lineToRelative(1.41f, 1.41f)
                lineTo(11f, 7.83f)
                verticalLineTo(20f)
                horizontalLineToRelative(2f)
                verticalLineTo(7.83f)
                lineToRelative(5.58f, 5.59f)
                lineTo(20f, 12f)
                lineToRelative(-8f, -8f)
                lineToRelative(-8f, 8f)
                close()
            }
        }
    }

    val ArrowDownward: ImageVector by lazy {
        materialVector("ArrowDownward") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 12f)
                lineToRelative(-1.41f, -1.41f)
                lineTo(13f, 16.17f)
                verticalLineTo(4f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(12.17f)
                lineToRelative(-5.58f, -5.59f)
                lineTo(4f, 12f)
                lineToRelative(8f, 8f)
                lineToRelative(8f, -8f)
                close()
            }
        }
    }

    val RemoveCircleOutline: ImageVector by lazy {
        materialVector("RemoveCircleOutline") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 11f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(10f)
                verticalLineToRelative(-2f)
                horizontalLineTo(7f)
                close()
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                reflectiveCurveToRelative(4.48f, 10f, 10f, 10f)
                reflectiveCurveToRelative(10f, -4.48f, 10f, -10f)
                reflectiveCurveTo(17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 20f)
                curveToRelative(-4.41f, 0f, -8f, -3.59f, -8f, -8f)
                reflectiveCurveToRelative(3.59f, -8f, 8f, -8f)
                reflectiveCurveToRelative(8f, 3.59f, 8f, 8f)
                reflectiveCurveToRelative(-3.59f, 8f, -8f, 8f)
                close()
            }
        }
    }

    val OpenInFull: ImageVector by lazy {
        materialVector("OpenInFull") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 11f)
                verticalLineTo(3f)
                horizontalLineToRelative(-8f)
                lineToRelative(3.29f, 3.29f)
                lineToRelative(-10f, 10f)
                lineTo(3f, 13f)
                verticalLineToRelative(8f)
                horizontalLineToRelative(8f)
                lineToRelative(-3.29f, -3.29f)
                lineToRelative(10f, -10f)
                close()
            }
        }
    }

    val Image: ImageVector by lazy {
        materialVector("Image") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 19f)
                verticalLineTo(5f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                horizontalLineTo(5f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(14f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                close()
                moveTo(8.5f, 13.5f)
                lineToRelative(2.5f, 3.01f)
                lineTo(14.5f, 12f)
                lineToRelative(4.5f, 6f)
                horizontalLineTo(5f)
                lineToRelative(3.5f, -4.5f)
                close()
            }
        }
    }

    val MoreHoriz: ImageVector by lazy {
        materialVector("MoreHoriz") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
                moveTo(18f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
                moveTo(12f, 10f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                reflectiveCurveToRelative(0.9f, 2f, 2f, 2f)
                reflectiveCurveToRelative(2f, -0.9f, 2f, -2f)
                reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f)
                close()
            }
        }
    }

    val BluetoothAudio: ImageVector by lazy {
        materialVector("BluetoothAudio") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14.24f, 12.01f)
                lineToRelative(2.32f, 2.32f)
                curveToRelative(0.28f, -0.72f, 0.44f, -1.51f, 0.44f, -2.33f)
                curveToRelative(0f, -0.82f, -0.16f, -1.59f, -0.43f, -2.31f)
                lineToRelative(-2.33f, 2.32f)
                close()
                moveTo(19.53f, 6.71f)
                lineToRelative(-1.26f, 1.26f)
                curveToRelative(0.63f, 1.21f, 0.98f, 2.57f, 0.98f, 4.02f)
                reflectiveCurveToRelative(-0.36f, 2.82f, -0.98f, 4.02f)
                lineToRelative(1.2f, 1.2f)
                curveToRelative(0.97f, -1.54f, 1.54f, -3.36f, 1.54f, -5.31f)
                curveToRelative(-0.01f, -1.89f, -0.55f, -3.67f, -1.48f, -5.19f)
                close()
                moveTo(15.71f, 7.71f)
                lineTo(10f, 2f)
                horizontalLineTo(9f)
                verticalLineToRelative(7.59f)
                lineTo(4.41f, 5f)
                lineTo(3f, 6.41f)
                lineTo(8.59f, 12f)
                lineTo(3f, 17.59f)
                lineTo(4.41f, 19f)
                lineTo(9f, 14.41f)
                verticalLineTo(22f)
                horizontalLineToRelative(1f)
                lineToRelative(5.71f, -5.71f)
                lineToRelative(-4.3f, -4.29f)
                lineToRelative(4.3f, -4.29f)
                close()
            }
        }
    }

    val BrightnessMedium: ImageVector by lazy {
        materialVector("BrightnessMedium") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 15.31f)
                lineTo(23.31f, 12f)
                lineTo(20f, 8.69f)
                verticalLineTo(4f)
                horizontalLineToRelative(-4.69f)
                lineTo(12f, 0.69f)
                lineTo(8.69f, 4f)
                horizontalLineTo(4f)
                verticalLineToRelative(4.69f)
                lineTo(0.69f, 12f)
                lineTo(4f, 15.31f)
                verticalLineTo(20f)
                horizontalLineToRelative(4.69f)
                lineTo(12f, 23.31f)
                lineTo(15.31f, 20f)
                horizontalLineTo(20f)
                verticalLineToRelative(-4.69f)
                close()
                moveTo(12f, 18f)
                verticalLineTo(6f)
                curveToRelative(3.31f, 0f, 6f, 2.69f, 6f, 6f)
                reflectiveCurveToRelative(-2.69f, 6f, -6f, 6f)
                close()
            }
        }
    }

    val VolumeUp: ImageVector by lazy {
        materialVector("VolumeUp") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(4f)
                lineToRelative(5f, 5f)
                verticalLineTo(4f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                close()
                moveTo(16.5f, 12f)
                curveToRelative(0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                verticalLineToRelative(8.05f)
                curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                close()
                moveTo(14f, 3.23f)
                verticalLineToRelative(2.06f)
                curveToRelative(2.89f, 0.86f, 5f, 3.54f, 5f, 6.71f)
                reflectiveCurveToRelative(-2.11f, 5.85f, -5f, 6.71f)
                verticalLineToRelative(2.06f)
                curveToRelative(4.01f, -0.91f, 7f, -4.49f, 7f, -8.77f)
                reflectiveCurveToRelative(-2.99f, -7.86f, -7f, -8.77f)
                close()
            }
        }
    }

    val Speed: ImageVector by lazy {
        materialVector("Speed") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.38f, 8.57f)
                lineToRelative(-1.23f, 1.85f)
                curveToRelative(0.55f, 1.07f, 0.85f, 2.27f, 0.85f, 3.58f)
                horizontalLineTo(4f)
                curveToRelative(0f, -4.42f, 3.58f, -8f, 8f, -8f)
                curveToRelative(1.31f, 0f, 2.51f, 0.3f, 3.58f, 0.85f)
                lineToRelative(1.85f, -1.23f)
                curveTo(15.86f, 4.62f, 13.99f, 4f, 12f, 4f)
                curveTo(6.48f, 4f, 2f, 8.48f, 2f, 14f)
                curveToRelative(0f, 2f, 0.9f, 4f, 2f, 5f)
                horizontalLineToRelative(16f)
                curveToRelative(1.1f, -1f, 2f, -3f, 2f, -5f)
                curveToRelative(0f, -2.01f, -0.62f, -3.86f, -1.62f, -5.43f)
                close()
                moveTo(10.59f, 15.41f)
                curveToRelative(0.78f, 0.78f, 2.05f, 0.78f, 2.83f, 0f)
                lineToRelative(5.66f, -8.49f)
                lineToRelative(-8.49f, 5.66f)
                curveToRelative(-0.78f, 0.78f, -0.78f, 2.05f, 0f, 2.83f)
                close()
            }
        }
    }

    val ClosedCaption: ImageVector by lazy {
        materialVector("ClosedCaption") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 4f)
                horizontalLineTo(5f)
                curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(12f)
                curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(6f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(11f, 11f)
                horizontalLineTo(9.5f)
                verticalLineToRelative(-0.5f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2f)
                verticalLineTo(13f)
                horizontalLineTo(11f)
                verticalLineToRelative(1f)
                curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
                horizontalLineTo(7f)
                curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
                verticalLineToRelative(-4f)
                curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                verticalLineToRelative(1f)
                close()
                moveTo(18f, 11f)
                horizontalLineToRelative(-1.5f)
                verticalLineToRelative(-0.5f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2f)
                verticalLineTo(13f)
                horizontalLineTo(18f)
                verticalLineToRelative(1f)
                curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f)
                horizontalLineToRelative(-3f)
                curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f)
                verticalLineToRelative(-4f)
                curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f)
                horizontalLineToRelative(3f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                verticalLineToRelative(1f)
                close()
            }
        }
    }

    val Fullscreen: ImageVector by lazy {
        materialVector("Fullscreen") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7f, 14f)
                horizontalLineTo(5f)
                verticalLineToRelative(5f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(-2f)
                horizontalLineTo(7f)
                verticalLineToRelative(-3f)
                close()
                moveTo(5f, 10f)
                horizontalLineToRelative(2f)
                verticalLineTo(7f)
                horizontalLineToRelative(3f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineToRelative(5f)
                close()
                moveTo(17f, 17f)
                horizontalLineToRelative(-3f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(5f)
                verticalLineToRelative(-5f)
                horizontalLineToRelative(-2f)
                verticalLineToRelative(3f)
                close()
                moveTo(14f, 5f)
                verticalLineToRelative(2f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(2f)
                verticalLineTo(5f)
                horizontalLineToRelative(-5f)
                close()
            }
        }
    }

    val ScreenRotation: ImageVector by lazy {
        materialVector("ScreenRotation") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.48f, 2.52f)
                curveToRelative(3.27f, 1.55f, 5.61f, 4.72f, 5.97f, 8.48f)
                horizontalLineToRelative(1.5f)
                curveTo(23.44f, 4.84f, 18.29f, 0f, 12f, 0f)
                lineToRelative(-0.66f, 0.03f)
                lineToRelative(3.81f, 3.81f)
                lineToRelative(1.33f, -1.32f)
                close()
                moveTo(10.23f, 1.75f)
                curveToRelative(-0.59f, -0.59f, -1.54f, -0.59f, -2.12f, 0f)
                lineTo(1.75f, 8.11f)
                curveToRelative(-0.59f, 0.59f, -0.59f, 1.54f, 0f, 2.12f)
                lineToRelative(12.02f, 12.02f)
                curveToRelative(0.59f, 0.59f, 1.54f, 0.59f, 2.12f, 0f)
                lineToRelative(6.36f, -6.36f)
                curveToRelative(0.59f, -0.59f, 0.59f, -1.54f, 0f, -2.12f)
                lineTo(10.23f, 1.75f)
                close()
                moveTo(14.83f, 21.19f)
                lineTo(2.81f, 9.17f)
                lineToRelative(6.36f, -6.36f)
                lineToRelative(12.02f, 12.02f)
                lineToRelative(-6.36f, 6.36f)
                close()
            }
        }
    }

    val PictureInPictureAlt: ImageVector by lazy {
        materialVector("PictureInPictureAlt") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 11f)
                horizontalLineToRelative(-8f)
                verticalLineToRelative(6f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-6f)
                close()
                moveTo(23f, 19f)
                verticalLineTo(4.98f)
                curveTo(23f, 3.88f, 22.1f, 3f, 21f, 3f)
                horizontalLineTo(3f)
                curveToRelative(-1.1f, 0f, -2f, 0.88f, -2f, 1.98f)
                verticalLineTo(19f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(18f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                close()
                moveTo(21f, 19.02f)
                horizontalLineTo(3f)
                verticalLineTo(4.97f)
                horizontalLineToRelative(18f)
                verticalLineToRelative(14.05f)
                close()
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        materialVector("ChevronDown") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(7.41f, 8.59f)
                lineTo(12f, 13.17f)
                lineToRelative(4.59f, -4.58f)
                lineTo(18f, 10f)
                lineToRelative(-6f, 6f)
                lineToRelative(-6f, -6f)
                lineToRelative(1.41f, -1.41f)
                close()
            }
        }
    }

    val Link: ImageVector by lazy {
        materialVector("Link") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3.9f, 12f)
                curveToRelative(0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                horizontalLineToRelative(4f)
                verticalLineTo(7f)
                horizontalLineTo(7f)
                curveToRelative(-2.76f, 0f, -5f, 2.24f, -5f, 5f)
                reflectiveCurveToRelative(2.24f, 5f, 5f, 5f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-1.9f)
                horizontalLineTo(7f)
                curveToRelative(-1.71f, 0f, -3.1f, -1.39f, -3.1f, -3.1f)
                close()
                moveTo(8f, 13f)
                horizontalLineToRelative(8f)
                verticalLineToRelative(-2f)
                horizontalLineTo(8f)
                verticalLineToRelative(2f)
                close()
                moveTo(17f, 7f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(1.9f)
                horizontalLineToRelative(4f)
                curveToRelative(1.71f, 0f, 3.1f, 1.39f, 3.1f, 3.1f)
                reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
                horizontalLineToRelative(-4f)
                verticalLineTo(17f)
                horizontalLineToRelative(4f)
                curveToRelative(2.76f, 0f, 5f, -2.24f, 5f, -5f)
                reflectiveCurveToRelative(-2.24f, -5f, -5f, -5f)
                close()
            }
        }
    }

    val ContentPaste: ImageVector by lazy {
        materialVector("ContentPaste") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 2f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 0.84f, 13.3f, 0f, 12f, 0f)
                curveToRelative(-1.3f, 0f, -2.4f, 0.84f, -2.82f, 2f)
                horizontalLineTo(5f)
                curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
                verticalLineToRelative(16f)
                curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
                horizontalLineToRelative(14f)
                curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
                verticalLineTo(4f)
                curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
                close()
                moveTo(12f, 2f)
                curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f)
                reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f)
                reflectiveCurveToRelative(-1f, -0.45f, -1f, -1f)
                reflectiveCurveToRelative(0.45f, -1f, 1f, -1f)
                close()
                moveTo(19f, 20f)
                horizontalLineTo(5f)
                verticalLineTo(4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(3f)
                horizontalLineToRelative(10f)
                verticalLineTo(4f)
                horizontalLineToRelative(2f)
                verticalLineToRelative(16f)
                close()
            }
        }
    }

    val Equalizer: ImageVector by lazy {
        materialVector("Equalizer") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 20f)
                horizontalLineToRelative(4f)
                verticalLineTo(4f)
                horizontalLineToRelative(-4f)
                verticalLineToRelative(16f)
                close()
                moveTo(4f, 20f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(-8f)
                horizontalLineTo(4f)
                verticalLineToRelative(8f)
                close()
                moveTo(16f, 9f)
                verticalLineToRelative(11f)
                horizontalLineToRelative(4f)
                verticalLineTo(9f)
                horizontalLineToRelative(-4f)
                close()
            }
        }
    }

    val YouTubePlay: ImageVector by lazy {
        materialVector("YouTubePlay") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21.58f, 7.19f)
                curveToRelative(-0.23f, -0.86f, -0.91f, -1.54f, -1.77f, -1.77f)
                curveTo(18.25f, 5f, 12f, 5f, 12f, 5f)
                reflectiveCurveToRelative(-6.25f, 0f, -7.81f, 0.42f)
                curveToRelative(-0.86f, 0.23f, -1.54f, 0.91f, -1.77f, 1.77f)
                curveTo(2f, 8.75f, 2f, 12f, 2f, 12f)
                reflectiveCurveToRelative(0f, 3.25f, 0.42f, 4.81f)
                curveToRelative(0.23f, 0.86f, 0.91f, 1.54f, 1.77f, 1.77f)
                curveTo(5.75f, 19f, 12f, 19f, 12f, 19f)
                reflectiveCurveToRelative(6.25f, 0f, 7.81f, -0.42f)
                curveToRelative(0.86f, -0.23f, 1.54f, -0.91f, 1.77f, -1.77f)
                curveTo(22f, 15.25f, 22f, 12f, 22f, 12f)
                reflectiveCurveToRelative(0f, -3.25f, -0.42f, -4.81f)
                close()
                moveTo(10f, 15f)
                verticalLineTo(9f)
                lineToRelative(5.2f, 3f)
                lineTo(10f, 15f)
                close()
            }
        }
    }
}

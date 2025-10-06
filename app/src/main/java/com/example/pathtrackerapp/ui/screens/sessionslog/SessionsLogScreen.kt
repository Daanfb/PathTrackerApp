package com.example.pathtrackerapp.ui.screens.sessionslog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.pathtrackerapp.R
import com.example.pathtrackerapp.domain.model.TrackingSession
import com.example.pathtrackerapp.ui.utils.buildStaticMapUrl
import com.example.pathtrackerapp.ui.utils.formatToDateTimeString
import com.example.pathtrackerapp.ui.utils.formatToTimeString

@Composable
fun SessionsLogScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionsLogViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {

        Text(
            text = stringResource(R.string.sessions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val display = uiState.display) {
            DisplaySessionsLogs.Empty -> EmptySessionsLog()
            is DisplaySessionsLogs.Error -> Text(display.message)

            DisplaySessionsLogs.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            }

            is DisplaySessionsLogs.Success -> SessionsList(sessions = display.sessions)
        }
    }
}

@Composable
private fun EmptySessionsLog() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            12.dp,
            Alignment.CenterVertically
        )
    ) {

        Icon(
            painter = painterResource(R.drawable.ic_empty_log),
            contentDescription = stringResource(R.string.no_sessions),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = stringResource(R.string.no_sessions),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.no_sessions_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SessionsList(sessions: List<TrackingSession>) {

    val topFade = Brush.verticalGradient(
        0f to Color.Transparent,
        0.02f to Color.Red,
        1f to Color.Red,
    )

    LazyColumn(
        modifier = Modifier.fadingEdge(topFade),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sessions, key = { it.id }) {
            SessionCard(session = it)
        }
    }
}

@Composable
private fun SessionCard(session: TrackingSession) {

    val (distanceText, distanceUnit) = if (session.distanceMeters >= 1000) {
        ("%.2f".format(session.distanceMeters / 1000) to "KM")
    } else {
        ("${session.distanceMeters.toInt()}" to "M")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large
            )
            .padding(12.dp)
    ) {
        Text(
            text = session.title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formatToDateTimeString(session.startTimeMillis),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_duration),
                title = stringResource(R.string.duration),
                value = formatToTimeString(session.durationSeconds)
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_running),
                title = stringResource(R.string.distance),
                value = distanceText,
                unit = distanceUnit
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_speed),
                title = stringResource(R.string.pace),
                value = "%.2f".format(session.averageSpeedKmh),
                unit = "KM/H"

            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_steps),
                title = stringResource(R.string.steps),
                value = session.steps.toString()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AsyncImage(
            model = buildStaticMapUrl(points = session.points, width = 600, height = 600),
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.extraSmall)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.extraSmall
                )
        )

    }
}

@Composable
private fun StatColumn(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    value: String,
    unit: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.titleMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(value)
                }

                unit?.let {
                    withStyle(
                        style = MaterialTheme.typography.titleMedium.toSpanStyle().copy(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        append(unit)
                    }
                }
            },
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
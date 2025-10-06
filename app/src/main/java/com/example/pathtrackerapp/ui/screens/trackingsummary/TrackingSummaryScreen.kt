package com.example.pathtrackerapp.ui.screens.trackingsummary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.pathtrackerapp.R
import com.example.pathtrackerapp.ui.utils.buildStaticMapUrl
import com.example.pathtrackerapp.ui.utils.formatToTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingSummaryScreen(
    viewModel: TrackingSummaryViewModel = hiltViewModel(),
    navToSessionsLog: () -> Unit,
    navBack: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {

        item("text_header") {
            TextHeader()
        }

        item("card_summary") {
            CardSummary(
                title = uiState.session.title,
                isTitleEmpty = uiState.isTitleEmpty,
                onTitleChange = viewModel::onTitleChange,
                urlImage = buildStaticMapUrl(uiState.session.points),
                duration = formatToTimeString(uiState.session.durationSeconds),
                distance = if (uiState.session.distanceMeters >= 1000) {
                    "%.2f".format(uiState.session.distanceMeters / 1000)
                } else {
                    "${uiState.session.distanceMeters.toInt()}"
                },
                distanceUnit = if (uiState.session.distanceMeters >= 1000) "KM" else "M",
                pace = "%.2f".format(uiState.session.averageSpeedKmh),
                steps = uiState.session.steps.toString(),
            )
        }

        item("buttons") {
            Buttons(onSaveClick = {
                viewModel.onSaveClick(navToSessionsLog)
            }, navBack = navBack)
        }
    }

    TitleNeededDialog(
        showDialog = uiState.isTitleEmpty,
        onDismiss = viewModel::onNeededTitleDialogDismiss,
    )
}

@Composable
private fun TextHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = stringResource(R.string.tracking_completed),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(R.string.tracking_completed_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CardSummary(
    title: String,
    isTitleEmpty: Boolean,
    onTitleChange: (String) -> Unit,
    urlImage: String,
    duration: String,
    distance: String,
    distanceUnit: String,
    pace: String,
    steps: String
) {

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large
            )
            .border(
                width = 2.dp,
                color = if (isTitleEmpty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large
            )
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .focusRequester(focusRequester),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = stringResource(R.string.add_a_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                innerTextField()
            }
        )

        AsyncImage(
            model = urlImage,
            contentDescription = "Static map image",
            modifier = Modifier
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.small
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_duration),
                title = stringResource(R.string.duration),
                value = duration
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_running),
                title = stringResource(R.string.distance),
                value = distance,
                unit = distanceUnit
            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_speed),
                title = stringResource(R.string.pace),
                value = pace,
                unit = "KM/H"

            )

            StatColumn(
                modifier = Modifier.weight(1f),
                icon = painterResource(R.drawable.ic_steps),
                title = stringResource(R.string.steps),
                value = steps
            )
        }
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
                tint = MaterialTheme.colorScheme.secondary,
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
private fun Buttons(
    onSaveClick: () -> Unit,
    navBack: () -> Unit,
) {

    var showDiscardSessionDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onSaveClick,
        ) {
            Text(text = stringResource(R.string.save_tracking))
        }

        TextButton(
            onClick = { showDiscardSessionDialog = true }, colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(stringResource(R.string.discard))
        }
    }

    DiscardSessionDialog(
        showDialog = showDiscardSessionDialog,
        onDismiss = { showDiscardSessionDialog = false },
        onDiscardConfirm = {
            showDiscardSessionDialog = false
            navBack()
        }
    )
}

@Composable
fun DiscardSessionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDiscardConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(text = stringResource(R.string.discard_session_question)) },
            text = { Text(text = stringResource(R.string.discard_session_description)) },
            dismissButton = {
                TextButton(
                    onClick = onDismiss, colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = onDiscardConfirm, colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(text = stringResource(R.string.discard))
                }
            }
        )
    }
}

@Composable
fun TitleNeededDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text(text = stringResource(R.string.title_needed)) },
            text = { Text(text = stringResource(R.string.title_needed_description)) },
            confirmButton = {
                TextButton(
                    onClick = onDismiss, colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = stringResource(R.string.accept))
                }
            }
        )
    }
}
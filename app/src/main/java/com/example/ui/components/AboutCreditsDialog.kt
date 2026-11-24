package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ClinicalIce
import com.example.ui.theme.ClinicalMuted
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateHairline
import com.example.ui.theme.SlateSurface
import com.example.ui.theme.SurgicalCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutCreditsBottomSheet(
    partCount: Int,
    conceptCount: Int,
    triangleCount: Int,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SlateSurface,
        contentColor = ClinicalIce,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "SOURCE & ATTRIBUTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SurgicalCyan,
                    )
                    Text(
                        text = "Human Atlas 3D",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClinicalIce,
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close about sheet",
                        tint = ClinicalMuted,
                    )
                }
            }

            Surface(
                color = SlateCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateHairline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    MetricItem(label = "MESHES", value = "%,d".format(partCount))
                    MetricItem(label = "CONCEPTS", value = "%,d".format(conceptCount))
                    MetricItem(label = "TRIANGLES", value = "%,d".format(triangleCount))
                }
            }

            Text(
                text = "Based on the open-source ashemag/human-atlas 3D anatomy viewer and BodyParts3D 4.0 adult male reference dataset. Designed strictly as an offline educational anatomy reference—not a clinical, diagnostic, or surgical tool.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClinicalMuted,
            )

            Text(
                text = "BodyParts3D, © The Database Center for Life Science, licensed under Creative Commons Attribution 4.0 International (CC BY 4.0).",
                style = MaterialTheme.typography.bodySmall,
                color = ClinicalIce.copy(alpha = 0.85f),
            )

            Spacer(Modifier.height(2.dp))

            ExternalLinkRow(
                title = "Open-source project (ashemag/human-atlas)",
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ashemag/human-atlas")),
                    )
                },
            )
            ExternalLinkRow(
                title = "BodyParts3D dataset & license (CC BY 4.0)",
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://dbarchive.biosciencedbc.jp/en/bodyparts3d/lic.html")),
                    )
                },
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ClinicalMuted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = SurgicalCyan,
        )
    }
}

@Composable
private fun ExternalLinkRow(
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        color = SlateCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, SlateHairline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ClinicalIce,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open external link",
                tint = SurgicalCyan,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

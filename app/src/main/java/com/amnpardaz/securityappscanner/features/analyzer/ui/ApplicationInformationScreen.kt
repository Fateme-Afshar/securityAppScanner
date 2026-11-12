
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amnpardaz.securityappscanner.features.analyzer.data.model.AppInformation
import com.amnpardaz.securityappscanner.features.analyzer.data.model.PermissionInformation
import com.amnpardaz.securityappscanner.features.analyzer.data.model.RiskLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppInformationScreen(
    app: AppInformation,
    modifier: Modifier = Modifier,
    onPermissionClick: (PermissionInformation) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { AppHeader(app) }
        item { RiskCard(app.riskLevel, app.riskReasons) }
        item { AppDetailsCard(app) }
        item { SecurityIndicatorsCard(app) }
        item {
            PermissionsCard(
                permissions = app.requestedPermissions,
                onPermissionClick = onPermissionClick
            )
        }
    }
}

@Composable
private fun AppHeader(app: AppInformation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("APP", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Version ${app.versionName ?: "Unknown"} (${app.versionCode})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RiskCard(
    riskLevel: RiskLevel,
    reasons: List<String>
) {
    val riskColor = riskColor(riskLevel)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = riskColor.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${riskLevel.name} RISK",
                color = riskColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (reasons.isNotEmpty()) {
                HorizontalDivider(color = riskColor.copy(alpha = 0.3f))
                Text(
                    text = "Risk reasons",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                reasons.forEach { reason ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("•", color = riskColor)
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    text = "No risk reasons were provided.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AppDetailsCard(app: AppInformation) {
    val installDate = remember(app.firstInstallTime) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(app.firstInstallTime))
    }

    InfoCard(title = "Application details") {
        InfoRow("Package name", app.packageName)
        InfoRow("Version name", app.versionName ?: "Unknown")
        InfoRow("Version code", app.versionCode.toString())
        InfoRow("First installed", installDate)
        InfoRow("Install source", app.installSource ?: "Unknown")
    }
}

@Composable
private fun SecurityIndicatorsCard(app: AppInformation) {
    InfoCard(title = "Security indicators") {
        BooleanInfoRow("System application", app.isSystemApp)
        BooleanInfoRow(
            title = "Sideloaded",
            value = app.isSideloaded,
            warningWhenTrue = true
        )
        BooleanInfoRow(
            title = "Debuggable",
            value = app.isDebuggable,
            warningWhenTrue = true
        )
    }
}

@Composable
private fun BooleanInfoRow(
    title: String,
    value: Boolean,
    warningWhenTrue: Boolean = false
) {
    val valueColor = if (value && warningWhenTrue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (value) "Yes" else "No",
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PermissionsCard(
    permissions: List<PermissionInformation>,
    onPermissionClick: (PermissionInformation) -> Unit
) {
    InfoCard(title = "Requested permissions") {
        Text(
            text = "${permissions.size} permissions requested",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (permissions.isEmpty()) {
            Text(
                text = "No permissions requested.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            permissions.forEach { permission ->
                ListItem(
                    headlineContent = {
                        // Replace toString() with the relevant PermissionInformation field.
                        Text(permission.toString())
                    },
                    supportingContent = {
                        Text(
                            text = "Tap to view permission details",
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPermissionClick(permission) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/*
 * Assumes RiskLevel contains:
 * LOW, MEDIUM, HIGH, CRITICAL.
 * Rename these cases to match your actual enum.
 */
@Composable
private fun riskColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.LOW -> Color(0xFF2E7D32)
        RiskLevel.MEDIUM -> Color(0xFFED9B22)
        RiskLevel.HIGH -> Color(0xFFE05A4F)
    }
}

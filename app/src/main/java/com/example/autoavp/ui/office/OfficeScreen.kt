package com.example.autoavp.ui.office

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autoavp.data.local.entities.InstanceOfficeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeScreen(
    navController: NavController,
    viewModel: OfficeViewModel = hiltViewModel()
) {
    val offices by viewModel.offices.collectAsState()
    val editingOffice by viewModel.editingOffice.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bureaux d'instance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddOffice() },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un bureau")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(offices, key = { it.officeId }) { office ->
                OfficeCard(
                    office = office,
                    onEdit = { viewModel.onEditOffice(office) },
                    onDelete = { viewModel.onDeleteOffice(office) }
                )
            }
        }
    }

    // Dialogue d'édition
    if (editingOffice != null) {
        OfficeEditDialog(
            office = editingOffice!!,
            onDismiss = { viewModel.onCancelEdit() },
            onSave = { viewModel.onSaveOffice(it) }
        )
    }
}

@Composable
fun OfficeCard(
    office: InstanceOfficeEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(office.colorHex)) } catch (e: Exception) { Color.Gray }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pastille de couleur
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.Black, CircleShape)
            )
            
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = office.name, style = MaterialTheme.typography.titleMedium)
                Text(text = office.address, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Modifier")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun OfficeEditDialog(
    office: InstanceOfficeEntity,
    onDismiss: () -> Unit,
    onSave: (InstanceOfficeEntity) -> Unit
) {
    var name by remember { mutableStateOf(office.name) }
    var address by remember { mutableStateOf(office.address) }
    var hours by remember { mutableStateOf(office.openingHours) }
    var colorHex by remember { mutableStateOf(office.colorHex) }
    var showColorPicker by remember { mutableStateOf(false) }

    // Palette de couleurs prédéfinies
    val presetColors = listOf(
        "#FFCE00", "#FFE082", "#FFB300", "#FF8F00",  // Jaunes / Ambrés
        "#E60000", "#EF5350", "#F06292", "#CE93D8",  // Rouges / Roses / Violets
        "#009900", "#66BB6A", "#26A69A", "#00897B",  // Verts
        "#004899", "#42A5F5", "#29B6F6", "#0288D1",  // Bleus
        "#795548", "#FF6D00", "#8D6E63", "#78909C",  // Marrons / Gris
        "#FFFFFF", "#E0E0E0", "#9E9E9E", "#000000"   // Neutres
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (office.officeId == 0L) "Nouveau bureau" else "Modifier bureau") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom du bureau") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Adresse") }
                )
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Horaires") }
                )

                Text("Couleur AVP :", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))

                // Aperçu de la couleur sélectionnée
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Text(colorHex.uppercase(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Grille de couleurs prédéfinies
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetColors) { hex ->
                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (colorHex.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                                    color = if (colorHex.equals(hex, ignoreCase = true)) MaterialTheme.colorScheme.primary
                                            else if (hex == "#FFFFFF") Color.LightGray else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }

                // Bouton nuancier / saisie manuelle
                if (showColorPicker) {
                    HsvColorPicker(
                        currentHex = colorHex,
                        onColorSelected = { colorHex = it }
                    )
                }
                TextButton(onClick = { showColorPicker = !showColorPicker }) {
                    Text(if (showColorPicker) "Masquer le nuancier" else "Nuancier personnalisé…")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(office.copy(
                    name = name,
                    address = address,
                    openingHours = hours,
                    colorHex = colorHex
                ))
            }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun HsvColorPicker(
    currentHex: String,
    onColorSelected: (String) -> Unit
) {
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var brightness by remember { mutableFloatStateOf(1f) }
    var hexInput by remember { mutableStateOf(currentHex) }

    // Initialiser depuis la couleur courante
    LaunchedEffect(currentHex) {
        try {
            val rgb = android.graphics.Color.parseColor(currentHex)
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(rgb, hsv)
            hue = hsv[0]
            saturation = hsv[1]
            brightness = hsv[2]
            hexInput = currentHex
        } catch (_: Exception) {}
    }

    fun updateColor() {
        val hex = hsvToHex(hue, saturation, brightness)
        hexInput = hex
        onColorSelected(hex)
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Teinte
        Text("Teinte", style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    Brush.horizontalGradient(
                        colors = (0..360 step 30).map { h -> Color.hsv(h.toFloat(), 1f, 1f) }
                    )
                )
        )
        Slider(
            value = hue,
            onValueChange = { hue = it; updateColor() },
            valueRange = 0f..360f,
            modifier = Modifier.fillMaxWidth()
        )

        // Saturation
        Text("Saturation", style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.hsv(hue, 0f, brightness), Color.hsv(hue, 1f, brightness))
                    )
                )
        )
        Slider(
            value = saturation,
            onValueChange = { saturation = it; updateColor() },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        // Luminosité
        Text("Luminosité", style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.hsv(hue, saturation, 0f), Color.hsv(hue, saturation, 1f))
                    )
                )
        )
        Slider(
            value = brightness,
            onValueChange = { brightness = it; updateColor() },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        // Saisie manuelle du code HTML
        OutlinedTextField(
            value = hexInput,
            onValueChange = { input ->
                hexInput = input
                val sanitized = if (input.startsWith("#")) input else "#$input"
                if (sanitized.matches(Regex("#[0-9A-Fa-f]{6}"))) {
                    onColorSelected(sanitized.uppercase())
                    try {
                        val rgb = android.graphics.Color.parseColor(sanitized)
                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(rgb, hsv)
                        hue = hsv[0]
                        saturation = hsv[1]
                        brightness = hsv[2]
                    } catch (_: Exception) {}
                }
            },
            label = { Text("Code couleur HTML") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun hsvToHex(h: Float, s: Float, v: Float): String {
    val rgb = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
    return String.format("#%06X", 0xFFFFFF and rgb)
}

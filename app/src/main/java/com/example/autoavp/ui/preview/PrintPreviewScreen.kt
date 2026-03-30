package com.example.autoavp.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.graphics.withSave
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.autoavp.R
import com.example.autoavp.data.local.entities.InstanceOfficeEntity
import com.example.autoavp.data.local.entities.MailItemEntity
import com.example.autoavp.ui.print.AvpPdfGenerator
import com.example.autoavp.ui.print.AvpRenderer
import com.example.autoavp.ui.print.PrintOrientation
import com.example.autoavp.ui.print.PrintUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewScreen(
    navController: NavController,
    viewModel: PrintPreviewViewModel = hiltViewModel()
) {
    val items by viewModel.mailItems.collectAsState()
    val office by viewModel.office.collectAsState()
    val calibX by viewModel.calibrationX.collectAsState(initial = 0f)
    val calibY by viewModel.calibrationY.collectAsState(initial = 0f)
    val context = LocalContext.current
    var orientation by remember { mutableStateOf(PrintOrientation.HORIZONTAL) }
    var reversed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aperçu avant impression") },
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
        bottomBar = {
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val generator = AvpPdfGenerator(context)
                        generator.printSession(items, office, orientation, reversed, calibX, calibY)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    enabled = items.isNotEmpty()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Lancer l'impression")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sens d\u0027insertion dans l\u0027imprimante :", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = orientation == PrintOrientation.HORIZONTAL,
                    onClick = { orientation = PrintOrientation.HORIZONTAL }
                )
                Text("Horizontal (Long c\u00f4t\u00e9)")
                Spacer(Modifier.width(16.dp))
                RadioButton(
                    selected = orientation == PrintOrientation.VERTICAL,
                    onClick = { orientation = PrintOrientation.VERTICAL }
                )
                Text("Vertical (Petit c\u00f4t\u00e9)")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = reversed,
                    onCheckedChange = { reversed = it }
                )
                Text("Retourn\u00e9 (impression \u00e0 180\u00b0)")
            }

            Spacer(Modifier.height(24.dp))
            
            if (items.isNotEmpty()) {
                Text("Aper\u00e7u du premier avis (sur ${items.size}) :", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                AvpVisualPreview(item = items.first(), office = office, calibX = calibX, calibY = calibY)
                
                if (calibX != 0f || calibY != 0f) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Calibration active : X=${("%.1f".format(calibX))}mm, Y=${("%.1f".format(calibY))}mm",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun AvpVisualPreview(item: MailItemEntity, office: InstanceOfficeEntity?, calibX: Float, calibY: Float) {
    val scale = 1.5f
    val widthDp = 210.dp * scale
    val heightDp = 99.dp * scale

    Box(
        modifier = Modifier
            .size(widthDp, heightDp)
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.avp_template),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val ratioX = canvasWidth / 595f
            val ratioY = canvasHeight / 281f

            drawContext.canvas.nativeCanvas.withSave {
                translate(PrintUtils.mmToPoints(calibX) * ratioX, PrintUtils.mmToPoints(calibY) * ratioY)

                // Pour l'aperçu, on applique une mise à l'échelle supplémentaire car le renderer travaille en points PDF (72dpi)
                // alors que l'écran a sa propre densité.
                // AvpRenderer dessine sur ~600x280 points.
                // Notre canvas de preview fait 'size.width' x 'size.height'.
                // ratioX est le facteur d'échelle.
                scale(ratioX, ratioX) // On garde le ratio d'aspect constant

                AvpRenderer.drawOnCanvas(this, item, office)
            }
        }
    }
}

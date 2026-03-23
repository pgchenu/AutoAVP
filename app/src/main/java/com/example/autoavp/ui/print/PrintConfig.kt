package com.example.autoavp.ui.print

object PrintConfig {
    // --- CONFIGURATION DES ZONES (Extraites de AVP Rempli.pptx) ---
    // Echelle calculée sur base AVP 210mm de large
    
    // Zone 1 : Numéro de Suivi
    const val TRACKING_BOX_X = 5.8f
    const val TRACKING_BOX_Y = 46.5f
    const val TRACKING_BOX_W = 74.4f

    // Zone 2 : Destinataire (4 lignes)
    const val ADDR_BOX_X = 5.8f
    const val ADDR_BOX_Y = 56.0f
    const val ADDR_BOX_H = 20.0f

    // Zone 3 : Bureau d'Instance
    const val INSTANCE_BOX_X = 123.0f
    const val INSTANCE_BOX_Y = 52.0f
    const val INSTANCE_BOX_W = 62.1f
    const val INSTANCE_BOX_H = 22.0f
}

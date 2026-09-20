package com.infusory.modelviewer

import com.infusory.modelviewer.data.GlbMetadataParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class GlbMetadataParserTest {

    private val assetsDir = File("src/main/assets/models")

    @Test
    fun parseBulbLabels() {
        val file = File(assetsDir, "Bulb.glb")
        assertTrue("Bulb.glb must exist", file.exists())
        val labels = GlbMetadataParser.parseLabels(FileInputStream(file))
        assertEquals(6, labels.size)
        val names = labels.map { it.labelText }.toSet()
        assertTrue(names.contains("Filament"))
        assertTrue(names.contains("Glass Bulb"))
        assertTrue(names.contains("Metal Base"))
        assertTrue(names.contains("Support Wires"))
    }

    @Test
    fun parseFiagenaLabels() {
        val file = File(assetsDir, "Fiagena.glb")
        assertTrue("Fiagena.glb must exist", file.exists())
        val labels = GlbMetadataParser.parseLabels(FileInputStream(file))
        assertEquals(7, labels.size)
        val names = labels.map { it.labelText }.toSet()
        assertTrue(names.contains("Hook"))
        assertTrue(names.contains("L ring"))
        assertTrue(names.contains("MS ring"))
    }

    @Test
    fun parseLungsLabels() {
        val file = File(assetsDir, "Lungs.glb")
        assertTrue("Lungs.glb must exist", file.exists())
        val labels = GlbMetadataParser.parseLabels(FileInputStream(file))
        assertEquals(5, labels.size)
        val names = labels.map { it.labelText }.toSet()
        assertTrue(names.contains("Larynx"))
        assertTrue(names.contains("Trachea"))
        assertTrue(names.contains("Lung"))
    }

    @Test
    fun parseMicroscopeLabels() {
        val file = File(assetsDir, "Microscope.glb")
        assertTrue("Microscope.glb must exist", file.exists())
        val labels = GlbMetadataParser.parseLabels(FileInputStream(file))
        assertEquals(12, labels.size)
        val names = labels.map { it.labelText }.toSet()
        assertTrue(names.contains("Eyepiece"))
        assertTrue(names.contains("Stage"))
        assertTrue(names.contains("Objective Lenses"))
    }

    @Test
    fun parseSolarSystemLabels() {
        val file = File(assetsDir, "solarsystem.glb")
        assertTrue("solarsystem.glb must exist", file.exists())
        val labels = GlbMetadataParser.parseLabels(FileInputStream(file))
        assertEquals(9, labels.size)
        val names = labels.map { it.labelText.lowercase() }.toSet()
        assertTrue(names.contains("earth"))
        assertTrue(names.contains("jupiter"))
        assertTrue(names.contains("saturn"))
    }
}

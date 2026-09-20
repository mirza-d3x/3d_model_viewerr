package com.infusory.modelviewer.data

import com.infusory.modelviewer.domain.PartLabel
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object GlbMetadataParser {

    private const val GLB_MAGIC = 0x46546C67 // "glTF"
    private const val CHUNK_TYPE_JSON = 0x4E4F534A // "JSON"

    /**
     * Parses the glTF JSON chunk from an [InputStream] and extracts all nodes containing `extras.prop`.
     *
     * @param inputStream Stream pointing to the .glb file.
     * @return List of [PartLabel] containing node index, name, label text, and translation.
     */
    fun parseLabels(inputStream: InputStream): List<PartLabel> {
        val header = ByteArray(12)
        var bytesRead = 0
        while (bytesRead < 12) {
            val r = inputStream.read(header, bytesRead, 12 - bytesRead)
            if (r == -1) break
            bytesRead += r
        }
        if (bytesRead < 12) return emptyList()

        val headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        val magic = headerBuffer.int
        if (magic != GLB_MAGIC) {
            return emptyList()
        }

        val chunkHeader = ByteArray(8)
        bytesRead = 0
        while (bytesRead < 8) {
            val r = inputStream.read(chunkHeader, bytesRead, 8 - bytesRead)
            if (r == -1) break
            bytesRead += r
        }
        if (bytesRead < 8) return emptyList()

        val chunkHeaderBuffer = ByteBuffer.wrap(chunkHeader).order(ByteOrder.LITTLE_ENDIAN)
        val chunkLength = chunkHeaderBuffer.int
        val chunkType = chunkHeaderBuffer.int

        if (chunkType != CHUNK_TYPE_JSON || chunkLength <= 0 || chunkLength > 20 * 1024 * 1024) {
            return emptyList()
        }

        val jsonBytes = ByteArray(chunkLength)
        bytesRead = 0
        while (bytesRead < chunkLength) {
            val r = inputStream.read(jsonBytes, bytesRead, chunkLength - bytesRead)
            if (r == -1) break
            bytesRead += r
        }

        val jsonString = String(jsonBytes, 0, bytesRead, Charsets.UTF_8)
        return parseLabelsFromJson(jsonString)
    }

    /**
     * Parses the glTF JSON string and extracts all nodes with `extras.prop`.
     */
    fun parseLabelsFromJson(jsonString: String): List<PartLabel> {
        val root = JSONObject(jsonString)
        val nodesArray = root.optJSONArray("nodes") ?: return emptyList()
        val labels = mutableListOf<PartLabel>()

        for (i in 0 until nodesArray.length()) {
            val nodeObj = nodesArray.optJSONObject(i) ?: continue
            val extras = nodeObj.optJSONObject("extras")
            if (extras != null && extras.has("prop")) {
                val labelText = extras.getString("prop").trim()
                if (labelText.isNotEmpty()) {
                    val nodeName = nodeObj.optString("name", "Node_$i")
                    val translation = FloatArray(3) { 0f }
                    val transArray = nodeObj.optJSONArray("translation")
                    if (transArray != null && transArray.length() >= 3) {
                        translation[0] = transArray.getDouble(0).toFloat()
                        translation[1] = transArray.getDouble(1).toFloat()
                        translation[2] = transArray.getDouble(2).toFloat()
                    }

                    labels.add(
                        PartLabel(
                            nodeIndex = i,
                            nodeName = nodeName,
                            labelText = labelText,
                            localTranslation = translation
                        )
                    )
                }
            }
        }
        return labels
    }
}

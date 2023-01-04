package fr.amazer.pokechu.ui.details.evolution_tree

import android.content.Context
import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.enums.EvolutionCondition
import fr.amazer.pokechu.enums.EvolutionItem
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.managers.SettingsManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.EvolutionConditionData
import fr.amazer.pokechu.viewmodel.ViewModelEvolutionData
import java.util.*

open class EvolutionTreeEdgeDecoration constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    pathEffect = CornerPathEffect(10f)
}) : RecyclerView.ItemDecoration() {

    private lateinit var context: Context
    private lateinit var configuration: BuchheimWalkerConfiguration
    private lateinit var canvas: Canvas

    private val linePath = Path()

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {

        val adapter = parent.adapter
        if (parent.layoutManager == null || adapter == null)
            return

        if (adapter !is AbstractGraphAdapter)
            throw RuntimeException("TreeEdgeDecoration only works with ${AbstractGraphAdapter::class.simpleName}")

        val layout = parent.layoutManager
        if (layout !is BuchheimWalkerLayoutManager)
            throw RuntimeException("TreeEdgeDecoration only works with ${BuchheimWalkerLayoutManager::class.simpleName}")

        context = parent.context
        configuration = layout.configuration
        canvas = c

        val graph = adapter.graph
        if (graph != null && graph.hasNodes()) {
            val nodes = graph.nodes

            for (node in nodes) {
                val children = graph.successorsOf(node)

                for (child in children) {

                    linePath.reset()
                    when (configuration.orientation) {
                        BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM -> {

                            drawEdge(node, child)

                            linePaint.style = Paint.Style.FILL

                            val nodeData = child.data as ViewModelEvolutionData
                            val isDiscovered = SettingsManager.isPokemonDiscovered(nodeData.pokemonId)
                            drawConditionText(child, isDiscovered)
//                            if (isDiscovered) {
//                                drawDiscoveredText(node, child)
//                            }
//                            else {
//                                drawUndiscoveredText(node, child)
//                            }

                            linePaint.style = Paint.Style.STROKE
                        }
                    }

                    canvas.drawPath(linePath, linePaint)
                }
            }
        }
        super.onDraw(canvas, parent, state)
    }

    private fun drawEdge(node: Node, child: Node) {
        // position at the middle-top of the child
        linePath.moveTo(child.x + child.width / 2f, child.y)
        // draws a line from the child's middle-top halfway up to its parent
        linePath.lineTo(
            child.x + child.width / 2f,
            child.y - configuration.levelSeparation / 2f
        )
        // draws a line from the previous point to the middle of the parents width
        linePath.lineTo(
            node.x + node.width / 2f,
            child.y - configuration.levelSeparation / 2f
        )

        // position at the middle of the level separation under the parent
        linePath.moveTo(
            node.x + node.width / 2f,
            child.y - configuration.levelSeparation / 2f
        )
        // draws a line up to the parents middle-bottom
        linePath.lineTo(
            node.x + node.width / 2f,
            node.y + node.height
        )
    }

//    private fun drawUndiscoveredText(node: Node, child: Node) {
//        var text = "???"
//
//        // Measure the text max width/height for it to fit
//        val textMaxWidth = child.width.toFloat()
//        val textMaxHeight = configuration.levelSeparation / 2f
//        // Backup text size
//        val beforeTextSize = linePaint.textSize
//        val (textWidth, textHeight) = autoAdjustSizeForText(text, textMaxWidth, textMaxHeight)
//
//        // Draw the text
//        val x = child.x + child.width / 2f - textWidth/2
//        val y = child.y - textHeight/4.0f// - configuration.levelSeparation / 4f
//        canvas.drawText(text, x, y, linePaint )
//
//        // Restore text size
//        linePaint.textSize = beforeTextSize
//    }

    private fun drawConditionText(child: Node, discovered: Boolean) {
        // Convert conditions data to string with '<>' for images
        val nodeData = child.data as ViewModelEvolutionData
        val originalText = buildConditionString(nodeData, discovered)//":(" //nodeData.conditions.toString()
        var text = originalText

        // Extract images from string and replace by empty spaces
        data class ImageData(
            var imgPath: String,
            var charPosition: Int
        )
        val images = mutableListOf<ImageData>()
        val regex = Regex("<(?:item|condition)_(\\d+)>")
        do {
            val match = regex.find(text)
            if (match != null) {
                val start = match.range.start
                val end = match.range.endInclusive
                text = text.replaceRange(start, end+1, "    ")

                val id = match.groupValues[1].toInt()
                if (match.groupValues[0].contains("item")) {
                    val imgPath = AssetUtils.getItemThumbnailPath(EvolutionItem.values()[id])
                    images.add(ImageData(imgPath, start))
                }
                else if (match.groupValues[0].contains("condition")) {
                    val imgPath = AssetUtils.getConditionThumbnailPath(EvolutionCondition.values()[id])
                    images.add(ImageData(imgPath, start))
                }
            }
        }
        while (match != null)

        // Measure the text max width/height for it to fit
        val textMaxWidth = child.width.toFloat()
        val textMaxHeight = configuration.levelSeparation / 2f
        // Backup text size
        val beforeTextSize = linePaint.textSize
        val (textWidth, textHeight) = autoAdjustSizeForText(text, textMaxWidth, textMaxHeight)

        // Draw the text without images
        val x = child.x + child.width / 2f - textWidth/2
        val y = child.y - textHeight/4.0f// - configuration.levelSeparation / 4f
        canvas.drawText(text, x, y, linePaint )

        // Draw the images

        // Add black filter to image if not discovered
        if (!discovered) {
            val porterDuffColorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
            linePaint.colorFilter = porterDuffColorFilter
        }
        val imageWidthHeight = linePaint.textSize
        images.forEach{ data ->
            val imgPath = data.imgPath
            val bitmap = AssetUtils.getBitmapFromAsset(context, imgPath)

            if (bitmap != null) {
                // Compute image position
                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                val dstX = x + linePaint.measureText(text.substring(0, data.charPosition))
                val dstY = y-imageWidthHeight/1.25f
                val dstRect = RectF(dstX, dstY, dstX+imageWidthHeight, dstY+imageWidthHeight)

                canvas.drawBitmap(bitmap, srcRect, dstRect, linePaint)
            }
            //(text, x, y, linePaint )
        }
        linePaint.colorFilter = ColorFilter()

        // Debug  line
//        canvas.drawLine(x, y, x+textWidth, y, linePaint)

//        canvas.drawText(HTML.fromHtml(text), 0, baseline, textPaint);

        // Restore text size
        linePaint.textSize = beforeTextSize
    }

    private fun buildConditionString(nodeData: ViewModelEvolutionData, discovered: Boolean): String {
        var result = ""

        if (nodeData.evolutionConditions != null)
            result = buildConditionStringHierarchy(nodeData.evolutionConditions!!, discovered)

        return result
    }

    private fun buildConditionStringHierarchy(conditionData: EvolutionConditionData, discovered: Boolean): String {
        var result = ""

        fun capitalize(str: String): String {
            return str.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                else it.toString()
            }
        }
        when(conditionData.type) {
            EvolutionCondition.UNKNOWN -> {
                result += "unknown"
            }
            EvolutionCondition.AND -> {
                val and = LocalizationManager.getConditionName(context, EvolutionCondition.AND)
                result += conditionData.nested.joinToString(" ${and} ") {
                    it -> buildConditionStringHierarchy(it, discovered)
                }
            }
            EvolutionCondition.OR -> {
                val or = LocalizationManager.getConditionName(context, EvolutionCondition.OR)
                result += conditionData.nested.joinToString(" $or ") {
                        it -> buildConditionStringHierarchy(it, discovered)
                }
            }
            EvolutionCondition.LEVEL -> {
                val level = LocalizationManager.getConditionName(context, EvolutionCondition.LEVEL)?.let { capitalize(it) }
                result += "$level "
                if (discovered)
                    result += conditionData.data.toInt()
                else
                    result += "?"
            }
            EvolutionCondition.HAPPINESS,
            EvolutionCondition.DAY,
            EvolutionCondition.NIGHT,
            EvolutionCondition.LEVEL_GAIN,
            EvolutionCondition.TRADE,
            EvolutionCondition.MALE,
            EvolutionCondition.FEMALE -> {
                result += "<condition_" + conditionData.type.ordinal + ">"
            }
            EvolutionCondition.ITEM_USE -> {
                if (discovered)
                    result += LocalizationManager.getItemName(context, EvolutionItem.values()[conditionData.data.toInt()])?.let { capitalize(it) }
                else
                    result += "?"

                result += " "
                result += "<item_" + conditionData.data + ">"
            }
            EvolutionCondition.ITEM_HOLD -> {
                val hold = LocalizationManager.getConditionName(context, EvolutionCondition.ITEM_HOLD)?.let { capitalize(it) }

                result += "$hold "
                if (discovered)
                    result += LocalizationManager.getItemName(context, EvolutionItem.values()[conditionData.data.toInt()])?.let { capitalize(it) }
                else
                    result += "?"

                result += " "
                result += "<item_" + conditionData.data + ">"
            }
            EvolutionCondition.LOCATION -> {
                val location = LocalizationManager.getConditionName(context, EvolutionCondition.LOCATION)?.let { capitalize(it) }
                result += "$location"
            }
            EvolutionCondition.KNOW_SKILL -> {
//                val know = LocalizationManager.getConditionName(context, EvolutionCondition.KNOW_SKILL)?.let { capitalize(it) }
                result += "know_skill "
                if (discovered)
                    result += conditionData.data
                else
                    result += "?"
            }
            EvolutionCondition.LEARN_SKILL -> {
//                val learn = LocalizationManager.getConditionName(context, EvolutionCondition.LEARN_SKILL)?.let { capitalize(it) }
                result += "learn_skill "
                if (discovered)
                    result += conditionData.data
                else
                    result += "?"
            }
        }

        return result
    }

    private fun autoAdjustSizeForText(text: String, maxWidth: Float, maxHeight: Float): Pair<Float, Float> {

        var size = linePaint.textSize
        // Find text size that fits in height
        var textHeight: Float
        do {
            linePaint.textSize = size
            val fm: Paint.FontMetrics = linePaint.fontMetrics
            textHeight = fm.descent - fm.ascent
            size = size - 1.0f
        }while (textHeight >= maxHeight)

        // Find text size that fits in width
        var textWidth: Float
        do {
            linePaint.textSize = size
            textWidth = linePaint.measureText(text)
            size = size - 1.0f
        }
        while( textWidth > maxWidth )

        return Pair(textWidth, textHeight)
    }
}

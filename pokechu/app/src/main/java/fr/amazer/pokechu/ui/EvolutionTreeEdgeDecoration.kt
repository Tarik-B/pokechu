package fr.amazer.pokechu.ui

import android.content.Context
import android.content.res.AssetManager
import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import dev.bandb.graphview.AbstractGraphAdapter
import dev.bandb.graphview.graph.Node
import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager
import fr.amazer.pokechu.data.EvolutionConditionType
import fr.amazer.pokechu.data.ItemType
import fr.amazer.pokechu.managers.LocalizationManager
import fr.amazer.pokechu.utils.AssetUtils
import fr.amazer.pokechu.utils.EvolutionConditionData
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

        linePaint.style = Paint.Style.FILL

        drawText(node, child)

        linePaint.style = Paint.Style.STROKE
    }

    private fun drawText(node: Node, child: Node) {
        val beforeTextSize = linePaint.textSize

        data class ImageData(
            var imgPath: String,
            var charPosition: Int
        )
        val images = mutableListOf<ImageData>()
        val nodeData = child.data as EvolutionNodeData
        val originalText = buildString(nodeData)//":(" //nodeData.conditions.toString()
        var text = originalText
        val regex = Regex("<(?:item|condition)_(\\d+)>")
        do {
            val match = regex.find(text)
            if (match != null) {
                val start = match.range.start
                val end = match.range.endInclusive
                text = text.replaceRange(start, end+1, "    ")

                val id = match.groupValues[1].toInt()
                if (match.groupValues[0].contains("item")) {
                    val imgPath = AssetUtils.getItemThumbnailPath(ItemType.values()[id])
                    images.add(ImageData(imgPath, start))
                }
                else if (match.groupValues[0].contains("condition")) {
                    val imgPath = AssetUtils.getConditionThumbnailPath(EvolutionConditionType.values()[id])
                    images.add(ImageData(imgPath, start))
                }
            }
        }
        while (match != null)

        val maxHeight = configuration.levelSeparation / 2f
        var size = linePaint.textSize
        // Find text size that fits in height
        var textHeight = 0.0f
        do {
            linePaint.textSize = size
            val fm: Paint.FontMetrics = linePaint.getFontMetrics()
            textHeight = fm.descent - fm.ascent
            size = size - 1.0f
        }while (textHeight >= maxHeight)

        // Find text size that fits in width
        var textWidth = 0.0f
        do {
            linePaint.textSize = size
            textWidth = linePaint.measureText(text)
            size = size - 1.0f
        }
        while( textWidth > child.width )


        val x = child.x + child.width / 2f - textWidth/2
        val y = child.y - textHeight/4.0f// - configuration.levelSeparation / 4f
        canvas.drawText(text, x, y, linePaint )

        val imageWidthHeight = linePaint.textSize
        images.forEach{ data ->
            val assetManager: AssetManager? = context?.assets
            val imgPath = data.imgPath
            val bitmap = assetManager?.let { AssetUtils.getBitmapFromAsset(it, imgPath) }

            if (bitmap != null) {
//                val left = x
//                val top = y
//                canvas.drawBitmap(bitmap, left, top, linePaint)

                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                val dstX = x + linePaint.measureText(text.substring(0, data.charPosition))
                val dstY = y-imageWidthHeight/1.25f
                val dstRect = RectF(dstX, dstY, dstX+imageWidthHeight, dstY+imageWidthHeight)
                canvas.drawBitmap(bitmap, srcRect, dstRect, linePaint)
            }
            //(text, x, y, linePaint )
        }

//        canvas.drawLine(x, y, x+textWidth, y, linePaint)

//        canvas.drawText(HTML.fromHtml(text), 0, baseline, textPaint);

        linePaint.textSize = beforeTextSize
    }

    private fun buildString(nodeData: EvolutionNodeData): String {
        var result = ""

        if (nodeData.conditions != null)
            result = buildConditionString(nodeData.conditions!!)

        return result
    }

    private fun buildConditionString(conditionData: EvolutionConditionData): String {
        var result = ""

        fun capitalize(str: String): String {
            return str.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                else it.toString()
            }
        }
        when(conditionData.type) {
            EvolutionConditionType.UNKNOWN -> {
                result += "unknown"
            }
            EvolutionConditionType.AND -> {
                val and = LocalizationManager.getConditionName(context, EvolutionConditionType.AND)
                result += conditionData.nested.joinToString(" ${and} ") {
                    it -> buildConditionString(it)
                }
            }
            EvolutionConditionType.OR -> {
                val or = LocalizationManager.getConditionName(context, EvolutionConditionType.OR)
                result += conditionData.nested.joinToString(" $or ") {
                        it -> buildConditionString(it)
                }
            }
            EvolutionConditionType.LEVEL -> {
                val level = LocalizationManager.getConditionName(context, EvolutionConditionType.LEVEL)?.let { capitalize(it) }
                result += "$level " + conditionData.data.toInt()
            }
            EvolutionConditionType.HAPPINESS,
            EvolutionConditionType.DAY,
            EvolutionConditionType.NIGHT,
            EvolutionConditionType.LEVEL_GAIN,
            EvolutionConditionType.TRADE,
            EvolutionConditionType.MALE,
            EvolutionConditionType.FEMALE -> {
                result += "<condition_" + conditionData.type.ordinal + ">"
            }
            EvolutionConditionType.ITEM_USE -> {
                result += LocalizationManager.getItemName(context, ItemType.values()[conditionData.data.toInt()])?.let { capitalize(it) }
                result += " "
                result += "<item_" + conditionData.data + ">"
            }
            EvolutionConditionType.ITEM_HOLD -> {
                val hold = LocalizationManager.getConditionName(context, EvolutionConditionType.ITEM_HOLD)?.let { capitalize(it) }

                result += "$hold "
                result += LocalizationManager.getItemName(context, ItemType.values()[conditionData.data.toInt()])?.let { capitalize(it) }
                result += " "
                result += "<item_" + conditionData.data + ">"
            }
            EvolutionConditionType.LOCATION -> {
                val location = LocalizationManager.getConditionName(context, EvolutionConditionType.LOCATION)?.let { capitalize(it) }
                result += "$location"
            }
            EvolutionConditionType.KNOW_SKILL -> {
                val know = LocalizationManager.getConditionName(context, EvolutionConditionType.KNOW_SKILL)?.let { capitalize(it) }
                result += "know_skill "
                result += conditionData.data
            }
            EvolutionConditionType.LEARN_SKILL -> {
                val learn = LocalizationManager.getConditionName(context, EvolutionConditionType.LEARN_SKILL)?.let { capitalize(it) }
                result += "learn_skill "
                result += conditionData.data
            }
        }

        return result
    }
}

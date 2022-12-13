package fr.amazer.pokechu.ui

import dev.bandb.graphview.layouts.tree.BuchheimWalkerConfiguration
import dev.bandb.graphview.layouts.tree.BuchheimWalkerLayoutManager

import android.graphics.*
import androidx.recyclerview.widget.RecyclerView
import fr.amazer.pokechu.data.DataEvolutionTree
import dev.bandb.graphview.AbstractGraphAdapter


open class EvolutionTreeEdgeDecoration constructor(private val linePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    strokeWidth = 5f
    color = Color.BLACK
    style = Paint.Style.STROKE
    strokeJoin = Paint.Join.ROUND
    pathEffect = CornerPathEffect(10f)
}) : RecyclerView.ItemDecoration() {

    private val linePath = Path()
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter
        if (parent.layoutManager == null || adapter == null) {
            return
        }
        if (adapter !is AbstractGraphAdapter) {
            throw RuntimeException(
                "TreeEdgeDecoration only works with ${AbstractGraphAdapter::class.simpleName}")
        }
        val layout = parent.layoutManager
        if (layout !is BuchheimWalkerLayoutManager) {
            throw RuntimeException(
                "TreeEdgeDecoration only works with ${BuchheimWalkerLayoutManager::class.simpleName}")
        }

        val configuration = layout.configuration

        val graph = adapter.graph
        if (graph != null && graph.hasNodes()) {
            val nodes = graph.nodes

            for (node in nodes) {
                val children = graph.successorsOf(node)

                for (child in children) {

                    linePath.reset()
                    when (configuration.orientation) {
                        BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM -> {
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

                            val beforeTextSize = linePaint.textSize

                            val nodeData = child.data //as DataEvolutionTree
                            val text = ":(" //nodeData.conditions.toString()

                            val maxHeight = configuration.levelSeparation / 2f
                            var size = linePaint.textSize
                            var textHeight = 0.0f
                            do {
                                linePaint.textSize = size
                                val fm: Paint.FontMetrics = linePaint.getFontMetrics()
                                textHeight = fm.descent - fm.ascent
                                size = size - 1.0f
                            }while (textHeight >= maxHeight)

                            var textWidth = 0.0f
                            do {
                                linePaint.textSize = size
                                textWidth = linePaint.measureText(text)
                                size = size - 1.0f
                            }
                            while( textWidth > child.width )


                            val x = child.x + child.width / 2f - textWidth/2
                            val y = child.y - textHeight/4.0f// - configuration.levelSeparation / 4f
                            c.drawText(text, x, y, linePaint )

                            linePaint.textSize = beforeTextSize

                            linePaint.style = Paint.Style.STROKE
                        }
                    }

                    c.drawPath(linePath, linePaint)
                }
            }
        }
        super.onDraw(c, parent, state)
    }
}

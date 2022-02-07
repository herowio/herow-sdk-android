package io.herow.sdk.detection.livemoment.moment

import io.herow.sdk.detection.livemoment.model.HerowPeriod
import io.herow.sdk.detection.livemoment.model.NodeDescription
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeNode

interface ILiveMomentStoreListener {

    fun liveMomentStoreStartComputing()
    fun didCompute(
        rects: ArrayList<NodeDescription>?,
        home: HerowQuadTreeNode?,
        work: HerowQuadTreeNode?,
        school: HerowQuadTreeNode?,
        shoppings: ArrayList<HerowQuadTreeNode>?,
        others: ArrayList<HerowQuadTreeNode>?,
        neighbours: ArrayList<HerowQuadTreeNode>?,
        periods: ArrayList<HerowPeriod>
    )
    fun didChangeNode(node: HerowQuadTreeNode)
    fun getFirstLiveMoments(
        home: HerowQuadTreeNode?,
        work: HerowQuadTreeNode?,
        school: HerowQuadTreeNode?,
        shoppings: ArrayList<HerowQuadTreeNode>?
    )
}
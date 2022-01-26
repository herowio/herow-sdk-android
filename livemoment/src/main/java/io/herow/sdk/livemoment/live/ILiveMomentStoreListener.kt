package io.herow.sdk.livemoment.live

import io.herow.sdk.livemoment.IPeriod
import io.herow.sdk.livemoment.model.NodeDescription
import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

interface ILiveMomentStoreListener {

    fun liveMomentStoreStartComputing()
    fun didCompute(
        rects: ArrayList<NodeDescription>?,
        home: IQuadTreeNode?,
        work: IQuadTreeNode?,
        school: IQuadTreeNode?,
        shoppings: ArrayList<IQuadTreeNode>?,
        others: ArrayList<IQuadTreeNode>?,
        neighbours: ArrayList<IQuadTreeNode>?,
        periods: ArrayList<IPeriod>
    )

    fun didChangeNode(node: IQuadTreeNode)
    fun getFirstLiveMoments(
        home: IQuadTreeNode?,
        work: IQuadTreeNode?,
        school: IQuadTreeNode?,
        shoppings: ArrayList<IQuadTreeNode>?
    )
}
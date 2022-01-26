package io.herow.sdk.livemoment.model

import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

data class LiveMomentResult(
    var homes: ArrayList<IQuadTreeNode>,
    var works: ArrayList<IQuadTreeNode>,
    var schools: ArrayList<IQuadTreeNode>,
    var shoppings: ArrayList<IQuadTreeNode>
)

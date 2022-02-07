package io.herow.sdk.detection.livemoment.model

import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeNode

data class LiveMomentResult(
    var homes: ArrayList<HerowQuadTreeNode>,
    var works: ArrayList<HerowQuadTreeNode>,
    var schools: ArrayList<HerowQuadTreeNode>,
    var shoppings: ArrayList<HerowQuadTreeNode>
)

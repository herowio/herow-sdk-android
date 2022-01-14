package io.herow.sdk.livemoment.model

import io.herow.sdk.livemoment.model.shape.Rect
import io.herow.sdk.livemoment.quadtree.IQuadTreeLocation
import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

data class NodeDescription(
    var treeId: String,
    var rect: Rect,
    var locations: ArrayList<IQuadTreeLocation>,
    var tags: HashMap<String, Double>?,
    var densities: HashMap<String, Double>?,
    var isMin: Boolean,
    var node: IQuadTreeNode
)

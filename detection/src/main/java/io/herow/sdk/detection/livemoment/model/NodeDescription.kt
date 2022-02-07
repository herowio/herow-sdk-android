package io.herow.sdk.detection.livemoment.model

import io.herow.sdk.detection.livemoment.model.shape.Rect
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeLocation
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeNode

data class NodeDescription(
    var treeId: String,
    var rect: Rect,
    var locations: ArrayList<HerowQuadTreeLocation>,
    var tags: HashMap<String, Double>?,
    var densities: HashMap<String, Double>?,
    var isMin: Boolean,
    var node: HerowQuadTreeNode
)

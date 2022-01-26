package io.herow.sdk.livemoment.quadtree

import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.livemoment.model.LeafType
import io.herow.sdk.livemoment.model.LiveMomentResult
import io.herow.sdk.livemoment.model.NodeDescription
import io.herow.sdk.livemoment.model.NodeType
import io.herow.sdk.livemoment.model.shape.Rect

interface IQuadTreeNode {
    var liveMomentTypes: java.util.ArrayList<NodeType>
        get() = arrayListOf()
        set(value) { }

    fun findNodeWithId(id: String): IQuadTreeNode?
    fun getTreeId(): String
    fun getParentNode(): IQuadTreeNode?
    fun getUpdate(): Boolean
    fun setUpdated(value: Boolean)
    fun setPois(pois: ArrayList<Poi>?)
    fun getPois(): ArrayList<Poi>
    fun getLocations(): ArrayList<IQuadTreeLocation>
    fun allLocations(): ArrayList<IQuadTreeLocation>
    fun getLastLocation(): IQuadTreeLocation?
    fun getLeftUpChild(): IQuadTreeNode?
    fun getRightUpChild(): IQuadTreeNode?
    fun getRightBottomChild(): IQuadTreeNode?
    fun getLeftBottomChild(): IQuadTreeNode?
    fun getTags(): HashMap<String, Double>?
    fun getDensities(): HashMap<String, Double>?
    fun computeTags(computeParent: Boolean = true)
    fun setRect(newRect: Rect)
    fun getRect(): Rect
    fun nodeForLocation(location: IQuadTreeLocation): IQuadTreeNode?
    fun browseTree(location: IQuadTreeLocation): IQuadTreeNode?
    fun getReccursiveRects(rects: ArrayList<NodeDescription>?): ArrayList<NodeDescription>
    fun getReccursiveNodes(nodes: ArrayList<IQuadTreeNode>?): ArrayList<IQuadTreeNode>
    fun childs(): ArrayList<IQuadTreeNode>
    fun addLocation(location: IQuadTreeLocation): IQuadTreeNode?
    fun populateParentality()
    fun recursiveCompute()
    fun isMin(): Boolean
    fun isNewBorn(): Boolean
    fun setNewBorn(value: Boolean)
    fun walkLeft(): IQuadTreeNode?
    fun walkRight(): IQuadTreeNode?
    fun walkUp(): IQuadTreeNode?
    fun walkDown(): IQuadTreeNode?
    fun walkUpLeft(): IQuadTreeNode?
    fun walkUpRight(): IQuadTreeNode?
    fun walkDownRight(): IQuadTreeNode?
    fun walkDownLeft(): IQuadTreeNode?
    fun type(): LeafType
    fun neighbourgs(): ArrayList<IQuadTreeNode>
    fun addInList(listOfQuadTreeNode: ArrayList<IQuadTreeNode>?): ArrayList<IQuadTreeNode>
    fun isEqual(node: IQuadTreeNode): Boolean
    fun setParentNode(parent: IQuadTreeNode?)
    fun setLastLocation(location: IQuadTreeLocation?)
    fun isNearToPoi(): Boolean
    fun getLimit(): Int

    fun addNodeType(type: NodeType) {
        if (!liveMomentTypes.contains(type)) {
            liveMomentTypes.add(type)
        }
    }

    fun removeNodeType(type: NodeType) {
        if (liveMomentTypes.contains(type)) {
            liveMomentTypes.removeAll { it == type }
        }
    }

    fun resetNodeTypes() = liveMomentTypes.clear()

    private fun getLiveMoments(): LiveMomentResult =
        getLiveMoments(
            arrayListOf(),
            arrayListOf(),
            arrayListOf(),
            arrayListOf()
        )

    private fun getLiveMoments(
        homes: ArrayList<IQuadTreeNode>,
        works: ArrayList<IQuadTreeNode>,
        schools: ArrayList<IQuadTreeNode>,
        shoppings: ArrayList<IQuadTreeNode>
    ): LiveMomentResult {
        if (childs().isEmpty()) {
            if (liveMomentTypes.contains(NodeType.HOME)) {
                homes.add(this)
            }

            if (liveMomentTypes.contains(NodeType.WORK)) {
                homes.add(this)
            }

            if (liveMomentTypes.contains(NodeType.SCHOOL)) {
                homes.add(this)
            }

            if (liveMomentTypes.contains(NodeType.SHOP)) {
                homes.add(this)
            }

        } else {
            childs().forEach {
                it.getLiveMoments(homes, works, schools, shoppings)
            }
        }

        return LiveMomentResult(homes, works, schools, shoppings)
    }
}
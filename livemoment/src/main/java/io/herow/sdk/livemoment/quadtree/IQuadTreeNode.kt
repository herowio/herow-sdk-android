package io.herow.sdk.livemoment.quadtree

import io.herow.sdk.common.data.LocationPattern
import io.herow.sdk.common.helpers.filtered
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.livemoment.model.LiveMomentResult
import io.herow.sdk.livemoment.model.NodeDescription
import io.herow.sdk.livemoment.model.enum.LeafType
import io.herow.sdk.livemoment.model.enum.NodeType
import io.herow.sdk.livemoment.model.enum.RecurrencyDay
import io.herow.sdk.livemoment.model.enum.RecurrencySlot
import io.herow.sdk.livemoment.model.reccurencyDay
import io.herow.sdk.livemoment.model.shape.Rect
import java.math.RoundingMode
import java.text.DecimalFormat

interface IQuadTreeNode {
    var liveMomentTypes: java.util.ArrayList<NodeType>
        get() = arrayListOf()
        set(value) {}

    var recurrencies: HashMap<RecurrencyDay, Int>
        get() = hashMapOf()
        set(value) {}

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

    fun getLocationPattern(): LocationPattern {
        val location = getRawLocationPattern()
        return location.filtered(location)
    }

    private fun getRawLocationPattern(): LocationPattern {
        val count = getCount().toDouble()
        val pattern = LocationPattern()

        val decimalFormat = DecimalFormat("#.##")
        decimalFormat.roundingMode = RoundingMode.CEILING

        for (recurrency in recurrencies) {
            pattern[recurrency.key.rawValue()] = (decimalFormat.format(recurrency.value.div(count))).toDouble()
        }

        return pattern
    }

    private fun getCount(): Int = getLocations().count()

    fun resetReccurencies() {
        for (loc in getLocations()) {
            val day = loc.time.reccurencyDay(loc.time, RecurrencySlot.UNKNOWN)
            var value: Int = recurrencies[day] ?: 0
            value += 1

            recurrencies[day] = value
        }
    }

    fun createRecurrencies(): HashMap<String, Int> {
        val rec = hashMapOf<String, Int>()

        for (key in recurrencies.keys) {
            val day = key.rawValue()
            val value = recurrencies[key] ?: 0
            rec[day] = value
        }

        return rec
    }
}
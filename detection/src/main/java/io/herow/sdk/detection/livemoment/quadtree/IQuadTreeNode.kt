package io.herow.sdk.detection.livemoment.quadtree

import io.herow.sdk.common.data.LocationPattern
import io.herow.sdk.common.helpers.filtered
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.detection.livemoment.model.LiveMomentResult
import io.herow.sdk.detection.livemoment.model.NodeDescription
import io.herow.sdk.detection.livemoment.model.enum.LeafType
import io.herow.sdk.detection.livemoment.model.enum.NodeType
import io.herow.sdk.detection.livemoment.model.enum.RecurrencyDay
import io.herow.sdk.detection.livemoment.model.enum.RecurrencySlot
import io.herow.sdk.detection.livemoment.model.reccurencyDay
import io.herow.sdk.detection.livemoment.model.shape.Rect
import java.math.RoundingMode
import java.text.DecimalFormat

interface IQuadTreeNode {
    var liveMomentTypes: java.util.ArrayList<NodeType>
        get() = arrayListOf()
        set(value) {}

    var recurrencies: HashMap<RecurrencyDay, Int>
        get() = hashMapOf()
        set(value) {}

    fun findNodeWithId(id: String): HerowQuadTreeNode?
    fun getTreeId(): String
    fun getParentNode(): HerowQuadTreeNode?
    fun getUpdate(): Boolean
    fun setUpdated(value: Boolean)
    fun setPois(pois: ArrayList<Poi>?)
    fun getPois(): ArrayList<Poi>
    fun getLocations(): ArrayList<HerowQuadTreeLocation>
    fun allLocations(): ArrayList<HerowQuadTreeLocation>
    fun getLastLocation(): HerowQuadTreeLocation?
    fun getLeftUpChild(): HerowQuadTreeNode?
    fun getRightUpChild(): HerowQuadTreeNode?
    fun getRightBottomChild(): HerowQuadTreeNode?
    fun getLeftBottomChild(): HerowQuadTreeNode?
    fun getTags(): HashMap<String, Double>?
    fun getDensities(): HashMap<String, Double>?
    fun computeTags(computeParent: Boolean = true)
    fun setRect(newRect: Rect)
    fun getRect(): Rect
    fun nodeForLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode?
    fun browseTree(location: HerowQuadTreeLocation): HerowQuadTreeNode?
    fun getReccursiveRects(rects: ArrayList<NodeDescription>?): ArrayList<NodeDescription>
    fun getReccursiveNodes(nodes: ArrayList<HerowQuadTreeNode>?): ArrayList<HerowQuadTreeNode>
    fun childs(): ArrayList<HerowQuadTreeNode>
    fun addLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode?
    fun populateParentality()
    fun recursiveCompute()
    fun isMin(): Boolean
    fun isNewBorn(): Boolean
    fun setNewBorn(value: Boolean)
    fun walkLeft(): HerowQuadTreeNode?
    fun walkRight(): HerowQuadTreeNode?
    fun walkUp(): HerowQuadTreeNode?
    fun walkDown(): HerowQuadTreeNode?
    fun walkUpLeft(): HerowQuadTreeNode?
    fun walkUpRight(): HerowQuadTreeNode?
    fun walkDownRight(): HerowQuadTreeNode?
    fun walkDownLeft(): HerowQuadTreeNode?
    fun type(): LeafType
    fun neighbourgs(): ArrayList<HerowQuadTreeNode>
    fun addInList(listOfQuadTreeNode: ArrayList<HerowQuadTreeNode>?): ArrayList<HerowQuadTreeNode>
    fun isEqual(node: HerowQuadTreeNode): Boolean
    fun setParentNode(parent: HerowQuadTreeNode?)
    fun setLastLocation(location: HerowQuadTreeLocation?)
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

    fun getLiveMoments(
        homes: ArrayList<HerowQuadTreeNode>,
        works: ArrayList<HerowQuadTreeNode>,
        schools: ArrayList<HerowQuadTreeNode>,
        shoppings: ArrayList<HerowQuadTreeNode>
    ): LiveMomentResult {
        if (childs().isEmpty()) {
            if (liveMomentTypes.contains(NodeType.HOME)) {
                homes.add(this as HerowQuadTreeNode)
            }

            if (liveMomentTypes.contains(NodeType.WORK)) {
                works.add(this as HerowQuadTreeNode)
            }

            if (liveMomentTypes.contains(NodeType.SCHOOL)) {
                schools.add(this as HerowQuadTreeNode)
            }

            if (liveMomentTypes.contains(NodeType.SHOP)) {
                shoppings.add(this as HerowQuadTreeNode)
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
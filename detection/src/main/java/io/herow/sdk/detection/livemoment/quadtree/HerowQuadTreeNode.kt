package io.herow.sdk.detection.livemoment.quadtree

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.helpers.isHomeCompliant
import io.herow.sdk.common.helpers.isSchoolCompliant
import io.herow.sdk.common.helpers.isWorkCompliant
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.detection.livemoment.model.NodeDescription
import io.herow.sdk.detection.livemoment.model.enum.LeafDirection
import io.herow.sdk.detection.livemoment.model.enum.LeafType
import io.herow.sdk.detection.livemoment.model.enum.LivingTag
import io.herow.sdk.detection.livemoment.model.enum.NodeType
import io.herow.sdk.detection.livemoment.model.shape.Rect
import java.time.LocalDate
import kotlin.math.max

class HerowQuadTreeNode(
    var idParameter: String = "",
    var locationsParameter: ArrayList<HerowQuadTreeLocation> = arrayListOf(),
    var leftUpParameter: HerowQuadTreeNode? = null,
    var rightUpParameter: HerowQuadTreeNode? = null,
    var leftBottomParameter: HerowQuadTreeNode? = null,
    var rightBottomParameter: HerowQuadTreeNode? = null,
    var tagsParameter: HashMap<String, Double> = hashMapOf(),
    var densitiesParameter: HashMap<String, Double> = hashMapOf(),
    var rectParameter: Rect? = Rect(),
    var poisParameter: ArrayList<Poi>? = arrayListOf()
) : IQuadTreeNode {
    val maxLat = 90.0
    val minLat = -90.0
    val maxLng = 180.0
    val minLng = -180.0
    val nodeSize = 100.0
    private val locationsLimitCount = 3
    private val fixedLimitLevelCount = 5
    var liveMomentsTypes = ArrayList<NodeType>()
    var merged: Boolean = false

    private var rect: Rect = Rect().world
    private var treeId: String? = null
    private lateinit var locations: ArrayList<HerowQuadTreeLocation>

    private var rightUpChild: HerowQuadTreeNode? = null
    private var rightBottomChild: HerowQuadTreeNode? = null
    private var leftUpChild: HerowQuadTreeNode? = null
    private var leftBottomChild: HerowQuadTreeNode? = null
    private var parentNode: HerowQuadTreeNode? = null
    private var lastLocation: HerowQuadTreeLocation? = null

    private var tags: HashMap<String, Double>? = null
    private var densities: HashMap<String, Double>? = null
    private var listOfPois: ArrayList<Poi>? = null

    private var updated = false
    private var isNewBorn = false

    private var lastHomeCount: Int = -1
    private var lastWorkCount: Int = -1
    private var lastSchoolCount: Int = -1
    private var lastShoppingCount: Int = -1

    private val shoppingMinRadius = 50.0

    fun getHash(): String = treeId ?: "0"
    fun getLat(): Double = getRect().circle().center.latitude
    fun getLng(): Double = getRect().circle().center.longitude
    fun getRadius(): Double = getRect().circle().radius

    init {
        setParentality()
    }

    private fun setParentality() {
        rightUpChild?.setParentNode(this)
        leftUpChild?.setParentNode(this)
        rightBottomChild?.setParentNode(this)
        leftBottomChild?.setParentNode(this)
    }

    override fun findNodeWithId(id: String): HerowQuadTreeNode? {
        val mytreeId = treeId ?: "0"

        if (mytreeId == id) {
            return this
        } else {
            var childResult: HerowQuadTreeNode?
            for (child in childs()) {
                childResult = child.findNodeWithId(id)

                if (childResult != null) {
                    return childResult
                }
            }
        }

        return null
    }

    override fun getTreeId(): String = treeId ?: "0"

    override fun getParentNode(): HerowQuadTreeNode? = parentNode

    override fun getUpdate(): Boolean = updated

    override fun setUpdated(value: Boolean) {
        updated = value

        if (!value) {
            childs().forEach {
                it.setUpdated(false)
            }
        }
    }

    override fun setPois(pois: ArrayList<Poi>?) {
        listOfPois = pois
    }

    override fun getPois(): ArrayList<Poi> = listOfPois ?: ArrayList()

    override fun getLocations(): ArrayList<HerowQuadTreeLocation> = locations

    override fun allLocations(): ArrayList<HerowQuadTreeLocation> = ArrayList<HerowQuadTreeLocation>().apply {
        getReccursiveRects(null).map { locations }
    }

    override fun getLastLocation(): HerowQuadTreeLocation? = lastLocation

    override fun getLeftUpChild(): HerowQuadTreeNode? = leftUpChild
    override fun getRightUpChild(): HerowQuadTreeNode? = rightUpChild
    override fun getRightBottomChild(): HerowQuadTreeNode? = rightBottomChild
    override fun getLeftBottomChild(): HerowQuadTreeNode? = leftBottomChild

    override fun getTags(): HashMap<String, Double>? = tags
    override fun getDensities(): HashMap<String, Double>? = densities

    override fun computeTags(computeParent: Boolean) {
        if ((treeId?.count() ?: 0) <= 13) {
            return
        }

        val start = TimeHelper.getCurrentTime()

        if (computeParent) {
            GlobalLogger.shared.debug(context = null, "LiveMomentStore - computesTags start")
        }

        val allLocations = allLocations()
        val tmpTags = tags ?: HashMap()
        val tmpDensities = densities ?: HashMap()

        tmpTags[LivingTag.HOME.toString()] = 0.0
        tmpTags[LivingTag.WORK.toString()] = 0.0
        tmpTags[LivingTag.SCHOOL.toString()] = 0.0
        tmpTags[LivingTag.SHOPPING.toString()] = 0.0

        tmpDensities[LivingTag.HOME.toString()] = 0.0
        tmpDensities[LivingTag.WORK.toString()] = 0.0
        tmpDensities[LivingTag.SCHOOL.toString()] = 0.0
        tmpDensities[LivingTag.SHOPPING.toString()] = 0.0

        val schoolCount = schoolCount(allLocations)
        val homeCount = homeCount()
        val workCount = workCount()
        val shoppingCount = shoppingCount()
        val locationsCount = allLocations.count()

        val area = rect.area()

        if (schoolCount > 0) {
            tmpTags[LivingTag.SCHOOL.toString()] = schoolCount.toDouble().div(locationsCount.toDouble())
            tmpDensities[LivingTag.SCHOOL.toString()] = area.div(schoolCount.toDouble())
        }

        if (homeCount > 0) {
            tmpTags[LivingTag.HOME.toString()] = homeCount.toDouble().div(locationsCount.toDouble())
            tmpDensities[LivingTag.HOME.toString()] = area.div(homeCount.toDouble())
        }

        if (workCount > 0) {
            tmpTags[LivingTag.WORK.toString()] = workCount.toDouble().div(locationsCount.toDouble())
            tmpDensities[LivingTag.WORK.toString()] = area.div(workCount.toDouble())
        }

        if (shoppingCount > 0) {
            tmpTags[LivingTag.SHOPPING.toString()] = shoppingCount.toDouble().div(locationsCount.toDouble())
            tmpDensities[LivingTag.SHOPPING.toString()] = area.div(shoppingCount.toDouble())
        }

        tags = tmpTags
        densities = tmpDensities

        if (computeParent) {
            parentNode?.computeTags(false)
        }

        val end = TimeHelper.getCurrentTime()
        val elapsedTime = end - start

        if (computeParent) {
            GlobalLogger.shared.debug(context = null, "LiveMomentStore -  computeTags done in $elapsedTime ms")
        }
    }

    private fun schoolCount(locations: ArrayList<HerowQuadTreeLocation>): Int {
        if (lastSchoolCount == -1) {
            lastSchoolCount = locations.count {
                it.time.isSchoolCompliant()
            }
        } else {
            if (lastLocation?.time?.isSchoolCompliant() == true) {
                lastSchoolCount += 1
            }
        }

        return lastSchoolCount
    }

    private fun workCount(): Int {
        if (lastWorkCount == -1) {
            lastWorkCount = allLocations().count {
                it.time.isWorkCompliant()
            }
        } else {
            if (lastLocation?.time?.isWorkCompliant() == true) {
                lastWorkCount += 1
            }
        }

        return lastWorkCount
    }

    private fun homeCount(): Int {
        if (lastHomeCount == -1) {
            lastHomeCount = allLocations().count {
                it.time.isHomeCompliant()
            }
        } else {
            if (lastLocation?.time?.isHomeCompliant() == true) {
                lastHomeCount += 1
            }
        }

        return lastHomeCount
    }

    private fun shoppingCount(): Int {
        if (lastShoppingCount == -1) {
            val filteredLocations = ArrayList<HerowQuadTreeLocation>()

            for (location in allLocations()) {
                val poisForLocation = poisForLocation(location)
                val isNearPOI = poisForLocation.isNotEmpty()

                location.setIsNearToPoi(isNearPOI)
                if (isNearPOI) {
                    filteredLocations.add(location)
                }
            }

            lastShoppingCount = filteredLocations.count()
        } else {
            lastLocation?.let {
                val poisForLocation = poisForLocation(it)
                val isNearPoi = poisForLocation.isNotEmpty()

                it.setIsNearToPoi(isNearPoi)
                if (isNearPoi) {
                    lastShoppingCount += 1
                }
            }
        }

        return lastShoppingCount
    }

    private fun poisForLocation(location: HerowQuadTreeLocation): ArrayList<Poi> {
        val poisForLocation = ArrayList<Poi>()

        for (poi in poisInProximity()) {
            val distance = Location("").apply {
                latitude = poi.lat!!
                longitude = poi.lng!!
            }.distanceTo(Location("").apply {
                latitude = location.lat
                longitude = location.lng
            })
            if (distance < shoppingMinRadius) {
                poisForLocation.add(poi)
            }
        }

        return poisForLocation
    }

    private fun poisInProximity(): ArrayList<Poi> {
        val allPois = ArrayList<Poi>()
        allPois.addAll(getPois())

        for (neighbour in neighbourgs()) {
            allPois.addAll(neighbour.getPois())
        }

        return allPois
    }

    override fun setRect(newRect: Rect) {
        rect = newRect
    }

    override fun getRect(): Rect = rect

    override fun nodeForLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode? {
        if (!rect.contains(location)) {
            return null
        }

        return addLocation(location)
    }

    override fun browseTree(location: HerowQuadTreeLocation): HerowQuadTreeNode? {
        var result: HerowQuadTreeNode? = null

        if (rect.contains(location)) {
            for (child in childs()) {
                result = this
                if (child.getRect().contains(location)) {
                    result = child.browseTree(location)
                }
            }
        }

        return result
    }

    override fun getReccursiveRects(rects: ArrayList<NodeDescription>?): ArrayList<NodeDescription> {
        val result = ArrayList<NodeDescription>()
        result.add(getDescription())

        for (child in childs()) {
            result.addAll(child.getReccursiveRects(result))
        }

        return result
    }

    private fun getDescription(): NodeDescription =
        NodeDescription(
            treeId ?: LeafType.ROOT.toString(),
            rect = getRect(),
            locations = locations,
            tags = tags,
            densities = densities,
            isMin(),
            this
        )

    override fun getReccursiveNodes(nodes: ArrayList<HerowQuadTreeNode>?): ArrayList<HerowQuadTreeNode> {
        val result: ArrayList<HerowQuadTreeNode> = nodes ?: arrayListOf()

        for (child in childs()) {
            result.addAll(child.getReccursiveNodes(result))
        }

        return result
    }

    override fun childs(): ArrayList<HerowQuadTreeNode> {
        val arrayOfChild = arrayListOf<HerowQuadTreeNode>()
        leftUpChild?.let { arrayOfChild.add(it) }
        leftBottomChild?.let { arrayOfChild.add(it) }
        rightUpChild?.let { arrayOfChild.add(it) }
        rightBottomChild?.let { arrayOfChild.add(it) }

        return arrayOfChild
    }

    override fun addLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode {
        val count = allLocations().count()
        populateLocation(location)

        if ((count <= getLimit() && !hasChildForLocation(location)) || rect.isMin()) {
            if (!locationIsPresent(location)) {
                GlobalLogger.shared.debug(
                    context = null,
                    "AddLocation node: $treeId count: $count isMin: ${rect.isMin()} limit: ${getLimit()}"
                )

                populateLocation(location)
                locations.add(location)
                lastLocation = location
                updated = true
                computeTags()
            }

            return this
        } else {
            return splitNode(location)
        }
    }

    private fun populateLocation(location: HerowQuadTreeLocation) {
        val poisForLocation = ArrayList<Poi>()

        for (poi in poisInProximity()) {
            val distance = Location("").apply {
                poi.lat?.let { latitude = it }
                poi.lng?.let { longitude = it }
            }.distanceTo(Location("").apply {
                latitude = location.lat
                longitude = location.lng
            })
            if (distance < shoppingMinRadius) {
                poisForLocation.add(poi)
            }
        }

        location.setIsNearToPoi(poisForLocation.isNotEmpty())
    }

    private fun locationIsPresent(location: HerowQuadTreeLocation): Boolean = locations.any {
        location.lat == it.lat && location.lng == it.lng && location.time == it.time
    }

    private fun hasChildForLocation(location: HerowQuadTreeLocation): Boolean =
        childForLocation(location)?.let { false } ?: true


    private fun splitNode(newLocation: HerowQuadTreeLocation): HerowQuadTreeNode {
        dispatchParentLocations()

        val child = childForLocation(newLocation)

        return child?.let {
            child.addLocation(newLocation)
            setUpdated(true)

            child
        } ?: createChildForLocation(newLocation)
    }

    private fun createChildForLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode {
        val type = leafTypeForLocation(location)
        return createChildForType(type, location)
    }

    private fun leafTypeForLocation(location: HerowQuadTreeLocation?): LeafType {
        location?.let {
            if (getRect().leftUpRect().contains(it)) {
                return LeafType.LEFTUP
            }

            if (getRect().leftBottomRect().contains(it)) {
                return LeafType.LEFTBOTTOM
            }

            if (getRect().rightUpRect().contains(it)) {
                return LeafType.RIGHTUP
            }

            return LeafType.RIGHTBOTTOM
        }

        return LeafType.RIGHTBOTTOM
    }

    private fun createChildForType(type: LeafType, location: HerowQuadTreeLocation?): HerowQuadTreeNode {
        val treeId: String = getTreeId() + type.name
        val arrayOfLocations = ArrayList<HerowQuadTreeLocation>()

        location?.let { arrayOfLocations.add(location) }

        val rect = getRect().rectForType(type)
        val pois = listOfPois?.filter { poi ->
            val locationForPoi: HerowQuadTreeLocation? =
                poi.lat?.let { poiLat ->
                    poi.lng?.let { poiLng ->
                        HerowQuadTreeLocation(
                            poiLat,
                            poiLng,
                            LocalDate.now()
                        )
                    }
                }
            locationForPoi.let { loc -> rect!!.contains(loc!!) }
        }

        val child = HerowQuadTreeNode(
            treeId,
            arrayOfLocations,
            null,
            null,
            null,
            null,
            hashMapOf(),
            hashMapOf(),
            rect,
            pois as ArrayList<Poi>
        )

        child.setParentNode(this)

        when (type) {
            LeafType.RIGHTUP -> rightUpChild = child
            LeafType.RIGHTBOTTOM -> rightBottomChild = child
            LeafType.LEFTUP -> leftUpChild = child
            LeafType.LEFTBOTTOM -> leftBottomChild = child
            else -> throw Exception("Should never happen")
        }

        child.updated = true
        child.isNewBorn = true
        return child
    }

    private fun dispatchParentLocations() {
        for (loc in locations) {
            var child: IQuadTreeNode? = childForLocation(loc)

            child?.let { child = child!!.addLocation(loc) } ?: kotlin.run { createChildForLocation(loc) }
        }

        locations.clear()
    }

    private fun childForLocation(location: HerowQuadTreeLocation): HerowQuadTreeNode? {
        var result: Boolean

        for (child in childs()) {
            result = child.getRect().contains(location)

            if (result)
                return child
        }

        return null
    }

    override fun populateParentality() {
        for (child in childs()) {
            child.setParentNode(this)
            child.populateParentality()
        }
    }

    override fun recursiveCompute() {
        computeTags(false)
        updated = true

        for (child in childs()) {
            child.computeTags(false)
        }
    }

    override fun isMin(): Boolean = rect.isMin()

    override fun isNewBorn(): Boolean = isNewBorn

    override fun setNewBorn(value: Boolean) {
        isNewBorn = value
    }

    override fun walkLeft(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> getParentNode()?.getRightUpChild()
            LeafType.RIGHTBOTTOM -> getParentNode()?.getRightBottomChild()
            LeafType.LEFTUP -> getParentNode()?.getLeftUpChild()
            LeafType.LEFTBOTTOM -> getParentNode()?.getLeftBottomChild()
            else -> null
        }

    override fun walkRight(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> {
                val rightParent = getParentNode()?.walkRight()
                rightParent?.getLeftUpChild() ?: rightParent
            }
            LeafType.RIGHTBOTTOM -> {
                val rightParent = getParentNode()?.walkRight()
                rightParent?.getLeftBottomChild() ?: rightParent
            }
            LeafType.LEFTUP -> getParentNode()?.getRightUpChild()
            LeafType.LEFTBOTTOM -> getParentNode()?.getRightBottomChild()
            else -> null
        }

    override fun walkUp(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> {
                val upParent = getParentNode()?.walkUp()
                upParent?.getRightBottomChild() ?: upParent
            }
            LeafType.RIGHTBOTTOM -> getParentNode()?.getRightUpChild()
            LeafType.LEFTUP -> {
                val upParent = getParentNode()?.walkUp()
                upParent?.getLeftBottomChild() ?: upParent
            }
            LeafType.LEFTBOTTOM -> getParentNode()?.getLeftUpChild()
            else -> null
        }

    override fun walkDown(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> getParentNode()?.getRightBottomChild()
            LeafType.RIGHTBOTTOM -> {
                val downParent = getParentNode()?.walkDown()
                downParent?.getRightUpChild() ?: downParent
            }
            LeafType.LEFTUP -> getParentNode()?.getLeftBottomChild()
            LeafType.LEFTBOTTOM -> {
                val downParent = getParentNode()?.walkDown()
                downParent?.getLeftUpChild() ?: downParent
            }
            else -> null
        }

    override fun walkUpLeft(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> {
                val upParent = getParentNode()?.walkUp()
                upParent?.getLeftBottomChild() ?: upParent
            }
            LeafType.RIGHTBOTTOM -> getParentNode()?.getLeftUpChild()
            LeafType.LEFTUP -> {
                val upLeftParent = getParentNode()?.walkUpLeft()
                upLeftParent?.getRightBottomChild() ?: upLeftParent
            }
            LeafType.LEFTBOTTOM -> {
                val leftBottomParent = getParentNode()?.walkLeft()
                leftBottomParent?.getRightUpChild() ?: leftBottomParent
            }
            else -> null
        }

    override fun walkUpRight(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> {
                val rightUpParent = getParentNode()?.walkUpRight()
                rightUpParent?.getLeftBottomChild() ?: rightUpParent
            }
            LeafType.RIGHTBOTTOM -> {
                val rightParent = getParentNode()?.walkRight()
                rightParent?.getLeftUpChild() ?: rightParent
            }
            LeafType.LEFTUP -> {
                val upParent = getParentNode()?.walkUp()
                upParent?.getRightBottomChild() ?: upParent
            }
            LeafType.LEFTBOTTOM -> getParentNode()?.getRightUpChild()
            else -> null
        }

    override fun walkDownRight(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> {
                val rightParent = getParentNode()?.walkRight()
                rightParent?.getLeftBottomChild() ?: rightParent
            }
            LeafType.RIGHTBOTTOM -> {
                val rightBottomParent = getParentNode()?.walkDownRight()
                rightBottomParent?.getLeftUpChild() ?: rightBottomParent
            }
            LeafType.LEFTUP -> getParentNode()?.getRightBottomChild()
            LeafType.LEFTBOTTOM -> {
                val bottomParent = getParentNode()?.walkDown()
                bottomParent?.getRightUpChild() ?: bottomParent
            }
            else -> null
        }

    override fun walkDownLeft(): HerowQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> getParentNode()?.getLeftBottomChild()
            LeafType.RIGHTBOTTOM -> {
                val bottomParent = getParentNode()?.walkDown()
                bottomParent?.getLeftUpChild() ?: bottomParent
            }
            LeafType.LEFTUP -> {
                val leftParent = getParentNode()?.walkLeft()
                leftParent?.getRightBottomChild() ?: leftParent
            }
            LeafType.LEFTBOTTOM -> {
                val leftBottomParent = getParentNode()?.walkDownLeft()
                leftBottomParent?.getRightUpChild() ?: leftBottomParent
            }
            else -> null
        }

    override fun type(): LeafType {
        val last: String = treeId?.last()?.toString() ?: "0"

        return when (LeafDirection.valueOf(last)) {
            LeafDirection.NW -> LeafType.LEFTUP
            LeafDirection.NE -> LeafType.RIGHTUP
            LeafDirection.SW -> LeafType.LEFTBOTTOM
            LeafDirection.SE -> LeafType.RIGHTBOTTOM
            else -> LeafType.ROOT
        }
    }

    override fun neighbourgs(): ArrayList<HerowQuadTreeNode> {
        var candidates = arrayListOf<HerowQuadTreeNode>()

        candidates = walkUp()?.addInList(candidates) ?: candidates
        candidates.addAll(walkDown()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkRight()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkLeft()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkDownLeft()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkDownRight()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkUpLeft()?.addInList(candidates) ?: candidates)
        candidates.addAll(walkUpRight()?.addInList(candidates) ?: candidates)

        return candidates
    }

    override fun addInList(listOfQuadTreeNode: ArrayList<HerowQuadTreeNode>?): ArrayList<HerowQuadTreeNode> {
        var result = arrayListOf<HerowQuadTreeNode>()

        if (treeId.isNullOrEmpty() && listOfQuadTreeNode.isNullOrEmpty()) return result else listOfQuadTreeNode?.let {
            result = it
        }

        listOfQuadTreeNode?.let { listOfQuad ->
            val idsList = listOfQuad.map { iQuadTreeNode -> iQuadTreeNode.getTreeId() }
            if (!idsList.contains(treeId)) result.add(this)
        }

        return result
    }

    override fun isEqual(node: HerowQuadTreeNode): Boolean = treeId == node.getTreeId()

    override fun setParentNode(parent: HerowQuadTreeNode?) {
        parentNode = parent
    }

    override fun setLastLocation(location: HerowQuadTreeLocation?) {
        lastLocation = location
    }

    override fun isNearToPoi(): Boolean =
        locations.count { isNearToPoi() } > 10 &&
                (densities?.count() ?: 0) > 0 &&
                getRect().circle().radius <= shoppingMinRadius &&
                (densities?.get(LivingTag.SHOPPING.name) ?: 0.0) > 0

    override fun getLimit(): Int {
        val level = (treeId?.count() ?: 1) - 1
        val result = limit(level)

        return max(1, result - 1)
    }

    private fun limit(level: Int): Int {
        if (level < this.fixedLimitLevelCount) return 1

        return max(1, this.locationsLimitCount.plus(limit(level - 1)))
    }
}

package io.herow.sdk.livemoment.quadtree

import android.location.Location
import io.herow.sdk.common.helpers.TimeHelper
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.livemoment.model.*
import io.herow.sdk.livemoment.model.shape.Rect
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.Month
import java.util.*
import kotlin.math.max

class HerowQuadTreeNode(
    var idParameter: String = "",
    var locationsParameter: ArrayList<IQuadTreeLocation> = arrayListOf(),
    var leftUpParameter: IQuadTreeNode? = null,
    var rightUpParameter: IQuadTreeNode? = null,
    var leftBottomParameter: IQuadTreeNode? = null,
    var rightBottomParameter: IQuadTreeNode? = null,
    var tagsParameter: HashMap<String, Double> = hashMapOf(),
    var densitiesParameter: HashMap<String, Double> = hashMapOf(),
    var rectParameter: Rect? = Rect(),
    var poisParameter: ArrayList<Poi>? = arrayListOf()
) :
    IQuadTreeNode {
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
    private lateinit var locations: ArrayList<IQuadTreeLocation>

    private var rightUpChild: IQuadTreeNode? = null
    private var rightBottomChild: IQuadTreeNode? = null
    private var leftUpChild: IQuadTreeNode? = null
    private var leftBottomChild: IQuadTreeNode? = null
    private var parentNode: IQuadTreeNode? = null
    private var lastLocation: IQuadTreeLocation? = null

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

    override fun findNodeWithId(id: String): IQuadTreeNode? {
        val mytreeId = treeId ?: "0"

        if (mytreeId == id) {
            return this
        } else {
            var childResult: IQuadTreeNode?
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

    override fun getParentNode(): IQuadTreeNode? = parentNode

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

    override fun getLocations(): ArrayList<IQuadTreeLocation> = locations

    override fun allLocations(): ArrayList<IQuadTreeLocation> = ArrayList<IQuadTreeLocation>().apply {
        getReccursiveRects(null).map { locations }
    }

    override fun getLastLocation(): IQuadTreeLocation? = lastLocation

    override fun getLeftUpChild(): IQuadTreeNode? = leftUpChild
    override fun getRightUpChild(): IQuadTreeNode? = rightUpChild
    override fun getRightBottomChild(): IQuadTreeNode? = rightBottomChild
    override fun getLeftBottomChild(): IQuadTreeNode? = leftBottomChild

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

    private fun schoolCount(locations: ArrayList<IQuadTreeLocation>): Int {
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
            val filteredLocations = ArrayList<IQuadTreeLocation>()

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

    private fun poisForLocation(location: IQuadTreeLocation): ArrayList<Poi> {
        val poisForLocation = ArrayList<Poi>()

        for (poi in poisInProximity()) {
            val distance = Location("").apply {
                latitude = poi.lat!!
                longitude = poi.lng!!
            }.distanceTo(Location("").apply {
                latitude = location.lat
                longitude = location.lng
            })
            // TODO Define shoppingMinRadius
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

    override fun nodeForLocation(location: IQuadTreeLocation): IQuadTreeNode? {
        if (!rect.contains(location)) {
            return null
        }

        return addLocation(location)
    }

    override fun browseTree(location: IQuadTreeLocation): IQuadTreeNode? {
        var result: IQuadTreeNode? = null

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

    override fun getReccursiveNodes(nodes: ArrayList<IQuadTreeNode>?): ArrayList<IQuadTreeNode> {
        val result: ArrayList<IQuadTreeNode> = nodes ?: arrayListOf()

        for (child in childs()) {
            result.addAll(child.getReccursiveNodes(result))
        }

        return result
    }

    override fun childs(): ArrayList<IQuadTreeNode> {
        val arrayOfChild = arrayListOf<IQuadTreeNode>()
        leftUpChild?.let { arrayOfChild.add(it) }
        leftBottomChild?.let { arrayOfChild.add(it) }
        rightUpChild?.let { arrayOfChild.add(it) }
        rightBottomChild?.let { arrayOfChild.add(it) }

        return arrayOfChild
    }

    override fun addLocation(location: IQuadTreeLocation): IQuadTreeNode {
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

    private fun populateLocation(location: IQuadTreeLocation) {
        val poisForLocation = ArrayList<Poi>()

        for (poi in poisInProximity()) {
            val distance = Location("").apply {
                poi.lat?.let { latitude = it }
                poi.lng?.let { longitude = it }
            }.distanceTo(Location("").apply {
                latitude = location.lat
                longitude = location.lng
            })
            //TODO Define shoppingMinRadius
            if (distance < shoppingMinRadius) {
                poisForLocation.add(poi)
            }
        }

        location.setIsNearToPoi(poisForLocation.isNotEmpty())
    }

    private fun locationIsPresent(location: IQuadTreeLocation): Boolean = locations.any {
        location.lat == it.lat && location.lng == it.lng && location.time == it.time
    }

    private fun hasChildForLocation(location: IQuadTreeLocation): Boolean =
        childForLocation(location)?.let { false } ?: true


    private fun splitNode(newLocation: IQuadTreeLocation): IQuadTreeNode {
        dispatchParentLocations()

        val child = childForLocation(newLocation)

        return child?.let {
            child.addLocation(newLocation)
            setUpdated(true)

            child
        } ?: createChildForLocation(newLocation)
    }

    private fun createChildForLocation(location: IQuadTreeLocation): IQuadTreeNode {
        val type = leafTypeForLocation(location)
        return createChildForType(type, location)
    }

    private fun leafTypeForLocation(location: IQuadTreeLocation?): LeafType {
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

    private fun createChildForType(type: LeafType, location: IQuadTreeLocation?): IQuadTreeNode {
        val treeId: String = getTreeId() + type.name
        val arrayOfLocations = ArrayList<IQuadTreeLocation>()

        location?.let { arrayOfLocations.add(location) }

        val rect = getRect().rectForType(type)
        val pois = listOfPois?.filter { poi ->
            val locationForPoi: HerowQuadTreeLocation? =
                poi.lat?.let { poiLat -> poi.lng?.let { poiLng -> HerowQuadTreeLocation(poiLat, poiLng, Date()) } }
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

    private fun childForLocation(location: IQuadTreeLocation): IQuadTreeNode? {
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

    override fun walkLeft(): IQuadTreeNode? =
        when (type()) {
            LeafType.RIGHTUP -> getParentNode()?.getRightUpChild()
            LeafType.RIGHTBOTTOM -> getParentNode()?.getRightBottomChild()
            LeafType.LEFTUP -> getParentNode()?.getLeftUpChild()
            LeafType.LEFTBOTTOM -> getParentNode()?.getLeftBottomChild()
            else -> null
        }

    override fun walkRight(): IQuadTreeNode? =
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

    override fun walkUp(): IQuadTreeNode? =
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

    override fun walkDown(): IQuadTreeNode? =
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

    override fun walkUpLeft(): IQuadTreeNode? =
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

    override fun walkUpRight(): IQuadTreeNode? =
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

    override fun walkDownRight(): IQuadTreeNode? =
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

    override fun walkDownLeft(): IQuadTreeNode? =
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

    override fun neighbourgs(): ArrayList<IQuadTreeNode> {
        var candidates = arrayListOf<IQuadTreeNode>()

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

    override fun addInList(listOfQuadTreeNode: ArrayList<IQuadTreeNode>?): ArrayList<IQuadTreeNode> {
        var result = arrayListOf<IQuadTreeNode>()

        if (treeId.isNullOrEmpty() && listOfQuadTreeNode.isNullOrEmpty()) return result else listOfQuadTreeNode?.let {
            result = it
        }

        listOfQuadTreeNode?.let { listOfQuad ->
            val idsList = listOfQuad.map { iQuadTreeNode -> iQuadTreeNode.getTreeId() }
            if (!idsList.contains(treeId)) result.add(this)
        }

        return result
    }

    override fun isEqual(node: IQuadTreeNode): Boolean = treeId == node.getTreeId()

    override fun setParentNode(parent: IQuadTreeNode?) {
        parentNode = parent
    }

    override fun setLastLocation(location: IQuadTreeLocation?) {
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


    /**
     * ********** EXTENSIONS ***********
     */

    private fun Date.isHomeCompliant(): Boolean {
        val evening2000 = LocalTime.of(20, 0)
        val evening2359 = LocalTime.of(23, 59)
        val morning0000 = LocalTime.of(0, 0)
        val morning0600 = LocalTime.of(6, 0)

        val evening = evening2000 < LocalTime.now() && evening2359 > LocalTime.now()
        val morning = morning0000 < LocalTime.now() && morning0600 > LocalTime.now()

        return (evening || morning)
    }

    private fun Date.isSchoolCompliant(): Boolean {
        val morning0840 = LocalTime.of(8, 40)
        val morning0850 = LocalTime.of(8, 50)
        val morning1130 = LocalTime.of(11, 30)
        val morning1200 = LocalTime.of(12, 0)
        val afternoon1320 = LocalTime.of(13, 20)
        val afternoon1350 = LocalTime.of(13, 50)
        val afternoon1630 = LocalTime.of(16, 30)
        val afternoon1700 = LocalTime.of(17, 0)

        val morningEntry = morning0840 < LocalTime.now() && morning0850 > LocalTime.now()
        val morningExit = morning1130 < LocalTime.now() && morning1200 > LocalTime.now()
        val afterEntry = afternoon1320 < LocalTime.now() && afternoon1350 > LocalTime.now()
        val afterExit = afternoon1630 < LocalTime.now() && afternoon1700 > LocalTime.now()

        val currentDate = TimeHelper.getCurrentLocalDateTime()
        val isNotDayOff: Boolean =
            currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY && currentDate.dayOfWeek != DayOfWeek.WEDNESDAY
        val isNotMonthOff: Boolean =
            currentDate.month != Month.JULY && currentDate.month != Month.AUGUST

        return (morningEntry || morningExit || afterEntry || afterExit) && isNotDayOff && isNotMonthOff
    }

    private fun Date.isWorkCompliant(): Boolean {
        val work0900 = LocalTime.of(9, 0)
        val work12000 = LocalTime.of(12, 0)
        val work1400 = LocalTime.of(14, 0)
        val work1800 = LocalTime.of(18, 0)

        val morning = work0900 < LocalTime.now() && work12000 > LocalTime.now()
        val afternoon = work1400 < LocalTime.now() && work1800 > LocalTime.now()
        val currentDate = TimeHelper.getCurrentLocalDateTime()

        val isNotWeekEnd: Boolean =
            currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY

        return (afternoon || morning) && isNotWeekEnd && !isSchoolCompliant()
    }

}

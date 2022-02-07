package io.herow.sdk.detection.livemoment.moment

import android.location.Location
import io.herow.sdk.common.logger.GlobalLogger
import io.herow.sdk.detection.koin.ICustomKoinComponent
import io.herow.sdk.detection.livemoment.model.HerowPeriod
import io.herow.sdk.detection.livemoment.model.NodeDescription
import io.herow.sdk.detection.livemoment.model.enum.LivingTag
import io.herow.sdk.detection.livemoment.model.enum.NodeType
import io.herow.sdk.detection.livemoment.model.repository.PeriodRepository
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeLocation
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeNode
import io.herow.sdk.detection.livemoment.quadtree.IQuadTreeLocation
import io.herow.sdk.detection.livemoment.quadtree.IQuadTreeNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class LiveMomentStore : ILiveMomentStore, ICustomKoinComponent {

    private var isWorking = false
    private var isOnBackground = false
    private var isSaving = false
    private var root: HerowQuadTreeNode? = null
    private var currentNode: HerowQuadTreeNode? = null
    private var count = 0

    private var home: HerowQuadTreeNode? = null
    private var work: HerowQuadTreeNode? = null
    private var school: HerowQuadTreeNode? = null
    private var shoppings: ArrayList<HerowQuadTreeNode>? = null

    private var lastHomes: ArrayList<HerowQuadTreeNode>? = null
    private var lastWorks: ArrayList<HerowQuadTreeNode>? = null
    private var lastSchools: ArrayList<HerowQuadTreeNode>? = null
    private var lastShoppings: ArrayList<HerowQuadTreeNode>? = null
    private var others: ArrayList<HerowQuadTreeNode>? = null

    private var rects: ArrayList<NodeDescription>? = null
    private var liveMomentsListeners = arrayListOf<ILiveMomentStoreListener>()
    private var periods: ArrayList<HerowPeriod>? = arrayListOf()
    private var needGetPeriods = true
    private var onGeohashOnly = false

    private val ioDispatcher: CoroutineDispatcher by inject()
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var periodRepository: PeriodRepository by inject()

    override fun onCacheReception() {
        currentNode = null
    }

    override fun onLocationUpdate(location: Location) {
        val now = LocalDate.now()
        needGetPeriods = true

        periods?.let {
            if (it.filter { period -> period.end > now }.first() != null) {
                needGetPeriods = false
            }
        }

        if (root != null || isWorking)
            return

        isWorking = true
        getNodeForLocation(location)
        compute()
        isWorking = false

        count += 1
    }

    override fun getNodeForLocation(location: Location) {
        for (listener in liveMomentsListeners) {
            listener.liveMomentStoreStartComputing()
        }

        val quadLocation = HerowQuadTreeLocation(
            location.latitude,
            location.longitude,
            Instant.ofEpochMilli(location.time).atZone(ZoneId.systemDefault()).toLocalDate()
        )

        val nodeToUse = currentNode ?: root
        val rootToUse: IQuadTreeNode? = nodeToUse?.let { reverseExploration(it, quadLocation) }

        rootToUse?.let { usingRoot ->
            val node = usingRoot.browseTree(quadLocation)
            val result = node?.addLocation(quadLocation)
            val nodeToSave = result?.getParentNode() ?: result
            currentNode = result

            currentNode?.let { current ->
                for (listener in liveMomentsListeners) {
                    listener.didChangeNode(current)
                }
            }

            save(false, nodeToSave)
            result?.setUpdated(false)
            nodeToSave?.setUpdated(false)
            computePeriods(quadLocation)

            GlobalLogger.shared.debug(
                context = null,
                "LiveMomentStore - tree result node: ${result?.getTreeId() ?: "none"} location count ${
                    result?.getLocations()?.count() ?: 0
                }"
            )
        }
    }

    override fun getClusters(): HerowQuadTreeNode? = root
    override fun getNodeForId(id: String): HerowQuadTreeNode? = root?.findNodeWithId(id)
    override fun getParentForNode(node: HerowQuadTreeNode): HerowQuadTreeNode? = node.getParentNode()
    override fun getHome(): HerowQuadTreeNode? = home
    override fun getWork(): HerowQuadTreeNode? = work
    override fun getSchool(): HerowQuadTreeNode? = school
    override fun getShopping(): ArrayList<HerowQuadTreeNode>? = shoppings
    private fun getRects() = rects

    override fun registerLiveMomentStoreListener(listener: ILiveMomentStoreListener) {
        liveMomentsListeners.add(listener)
        listener.getFirstLiveMoments(home, work, school, shoppings)
    }

    override fun processOnlyOnCurrentGeohash(value: Boolean) {
        if (onGeohashOnly != value) {
            onGeohashOnly = value
            compute()
        }
    }

    private fun onAppInForeground() {
        isOnBackground = false
    }

    private fun onAppInBackground() {
        isOnBackground = true
    }

    private fun loadTree() {
        TODO("create QuadTreeRoot")
    }

    private fun compute() {
        computeRects()
        val candidates = getNodeCandidates()
        val oldLives = ((lastHomes ?: arrayListOf()) + (lastWorks ?: arrayListOf()) + (lastSchools
            ?: arrayListOf()) + (lastShoppings ?: arrayListOf()))

        oldLives.forEach {
            it.resetNodeTypes()
        }

        work = computeWork(candidates)
        work?.addNodeType(NodeType.WORK)

        home = computeHome(candidates)
        home?.addNodeType(NodeType.HOME)

        school = computeSchool(candidates)
        school?.addNodeType(NodeType.SCHOOL)

        shoppings = computeShopping(candidates)
        shoppings?.forEach {
            it.addNodeType(NodeType.SHOP)
        }

        val nodesToSave: ArrayList<IQuadTreeNode> = arrayListOf()
        work?.let { nodesToSave.add(it) }
        home?.let { nodesToSave.add(it) }
        school?.let { nodesToSave.add(it) }
        shoppings?.let { for (element in it) nodesToSave.add(element) }
        if (oldLives.isNotEmpty()) {
            for (element in oldLives) nodesToSave.add(element)
        }

        others = null

        if (needGetPeriods) {
            applicationScope.launch {
                periods = periodRepository.getAllPeriods()
            }
            saveQuadTreesAndDispatch(currentNode, nodesToSave)
        } else {
            saveQuadTreesAndDispatch(currentNode, nodesToSave)
        }
    }

    private fun save(force: Boolean, node: IQuadTreeNode?) {
        root?.let {
            isSaving = true
            val nodeToSave = node ?: it

            TODO("saveQuadTree(nodeToSave)")
        }
    }

    private fun reverseExploration(node: IQuadTreeNode, location: IQuadTreeLocation): IQuadTreeNode? {
        if (node.getRect().contains(location)) {
            return node
        } else {
            node.getParentNode()?.let {
                return reverseExploration(it, location)
            }
        }

        return root
    }

    private fun computePeriods(location: HerowQuadTreeLocation) {
        periods?.let { for (period in it) period.addLocation(location) }
    }

    private fun getClustersInBase(): HerowQuadTreeNode? = TODO("Fetch clusters in database")

    private fun computeRects() {
        root = getClustersInBase()
        rects = root?.getReccursiveRects(null)
    }

    private fun getNodeCandidates(): ArrayList<NodeDescription>? = getRects()?.filter {
        it.locations.count() > 10
    } as ArrayList<NodeDescription>?


    private fun computeWork(candidates: ArrayList<NodeDescription>?): HerowQuadTreeNode? {
        val nodes = candidates?.let { candidate ->
            candidate.filter {
                (it.densities?.count() ?: 0) > 0 && ((it.densities?.get(LivingTag.WORK.toString()) ?: 0.0) > 0)
            }.sortedBy {
                it.densities?.get(LivingTag.WORK.toString()) ?: 0.0
            }
        }

        return nodes?.first()?.node
    }

    private fun computeHome(candidates: ArrayList<NodeDescription>?): HerowQuadTreeNode? {
        val nodes = candidates?.let { candidate ->
            candidate.filter {
                (it.densities?.count() ?: 0) > 0 && ((it.densities?.get(LivingTag.HOME.toString()) ?: 0.0) > 0)
            }.sortedBy {
                it.densities?.get(LivingTag.HOME.toString()) ?: 0.0
            }
        }

        return nodes?.first()?.node
    }

    private fun computeSchool(candidates: ArrayList<NodeDescription>?): HerowQuadTreeNode? {
        val nodes = candidates?.let { candidate ->
            candidate.filter {
                (it.densities?.count() ?: 0) > 0 && ((it.densities?.get(LivingTag.SCHOOL.toString()) ?: 0.0) > 0)
            }.sortedBy {
                it.densities?.get(LivingTag.SCHOOL.toString()) ?: 0.0
            }
        }

        return nodes?.first()?.node
    }

    private fun computeShopping(candidates: ArrayList<NodeDescription>?): ArrayList<HerowQuadTreeNode>? = candidates?.map {
        it.node
    }?.filter { it.isNearToPoi() } as ArrayList<HerowQuadTreeNode>

    private fun saveQuadTreesAndDispatch(node: IQuadTreeNode?, nodesToSave: ArrayList<IQuadTreeNode>) {
        val neighbours = node?.neighbourgs()

        for (listener in liveMomentsListeners) {
            periods?.let { listener.didCompute(rects, home, work, school, shoppings, others, neighbours, it) }
        }
        TODO("Save quadTrees into DB")
    }

}
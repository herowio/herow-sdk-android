package io.herow.sdk.detection.livemoment.moment

import android.location.Location
import io.herow.sdk.connection.cache.ICacheListener
import io.herow.sdk.detection.livemoment.quadtree.HerowQuadTreeNode
import io.herow.sdk.detection.location.ILocationListener

interface ILiveMomentStore : ICacheListener, ILocationListener {
    fun getNodeForLocation(location: Location)
    fun getClusters(): HerowQuadTreeNode?
    fun getNodeForId(id: String): HerowQuadTreeNode?
    fun getParentForNode(node: HerowQuadTreeNode): HerowQuadTreeNode?
    fun getHome(): HerowQuadTreeNode?
    fun getWork(): HerowQuadTreeNode?
    fun getSchool(): HerowQuadTreeNode?
    fun getShopping(): ArrayList<HerowQuadTreeNode>?
    fun registerLiveMomentStoreListener(listener: ILiveMomentStoreListener)
    fun processOnlyOnCurrentGeohash(value: Boolean)
}
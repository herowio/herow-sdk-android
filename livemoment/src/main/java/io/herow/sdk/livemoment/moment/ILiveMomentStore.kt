package io.herow.sdk.livemoment.moment

import android.location.Location
import io.herow.sdk.connection.cache.ICacheListener
import io.herow.sdk.detection.location.ILocationListener
import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

interface ILiveMomentStore : ICacheListener, ILocationListener {
    fun getNodeForLocation(location: Location)
    fun getClusters(): IQuadTreeNode?
    fun getNodeForId(id: String): IQuadTreeNode?
    fun getParentForNode(node: IQuadTreeNode): IQuadTreeNode?
    fun getHome(): IQuadTreeNode?
    fun getWork(): IQuadTreeNode?
    fun getSchool(): IQuadTreeNode?
    fun getShopping(): ArrayList<IQuadTreeNode>?
    fun registerLiveMomentStoreListener(listener: ILiveMomentStoreListener)
    fun processOnlyOnCurrentGeohash(value: Boolean)
}
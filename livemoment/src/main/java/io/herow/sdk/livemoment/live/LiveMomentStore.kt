package io.herow.sdk.livemoment.live

import android.location.Location
import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

class LiveMomentStore: ILiveMomentStore {

    override fun onCacheReception() {
        TODO("Not yet implemented")
    }

    override fun onLocationUpdate(location: Location) {
        TODO("Not yet implemented")
    }

    override fun getNodeForLocation(location: Location) {
        TODO("Not yet implemented")
    }

    override fun getClusters(): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getNodeForId(id: String): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getParentForNode(node: IQuadTreeNode): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getHome(): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getWork(): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getSchool(): IQuadTreeNode? {
        TODO("Not yet implemented")
    }

    override fun getShopping(): ArrayList<IQuadTreeNode>? {
        TODO("Not yet implemented")
    }

    override fun registerLiveMomentStoreListener(listener: ILiveMomentStoreListener) {
        TODO("Not yet implemented")
    }

    override fun processOnlyOnCurrentGeohash(value: Boolean) {
        TODO("Not yet implemented")
    }
}
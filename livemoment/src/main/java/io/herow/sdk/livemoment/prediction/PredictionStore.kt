package io.herow.sdk.livemoment.prediction

import android.location.Location
import io.herow.sdk.common.data.TagPrediction
import io.herow.sdk.connection.cache.model.Poi
import io.herow.sdk.connection.prediction.Coordinates
import io.herow.sdk.connection.prediction.Prediction
import io.herow.sdk.livemoment.IPeriod
import io.herow.sdk.livemoment.model.NodeDescription
import io.herow.sdk.livemoment.model.TagObject
import io.herow.sdk.livemoment.quadtree.IQuadTreeNode

class PredictionStore : IPredictionStore {

    private var predictionListeners = ArrayList<IPredictionStoreListener>()

    override fun registerListener(listener: IPredictionStoreListener) {
        val first = predictionListeners.firstOrNull {
            it == listener
        }

        if (first == null) {
            predictionListeners.add(listener)
        }
    }

    override fun unregisterListener(listener: IPredictionStoreListener) {
        predictionListeners = predictionListeners.filter {
            it != listener
        } as ArrayList<IPredictionStoreListener>
    }

    override fun liveMomentStoreStartComputing() {
        //Do nothing
    }

    override fun didCompute(
        rects: ArrayList<NodeDescription>?,
        home: IQuadTreeNode?,
        work: IQuadTreeNode?,
        school: IQuadTreeNode?,
        shoppings: ArrayList<IQuadTreeNode>?,
        others: ArrayList<IQuadTreeNode>?,
        neighbours: ArrayList<IQuadTreeNode>?,
        periods: ArrayList<IPeriod>
    ) {
        if (shoppings == null) return

        for (listener in predictionListeners) {
            listener.didPredict(processShoppingZonesPredictions(shoppings))
        }
        TODO("Not yet implemented - database required")
    }

    private fun processShoppingZonesPredictions(shops: ArrayList<IQuadTreeNode>): ArrayList<Prediction> {
        val predictions = arrayListOf<Prediction>()

        for (shop in shops) {
            val coordinates: Location = shop.getRect().circle().center

            val allPois = arrayListOf<Poi>()
            allPois.addAll(shop.getPois())

            for (neighbour in shop.neighbourgs()) {
                allPois.addAll(neighbour.getPois())
            }


            if (allPois.isNotEmpty()) {
                predictions.add(
                    Prediction(
                        pois = allPois,
                        coordinates = Coordinates(coordinates.latitude, coordinates.longitude)
                    )
                )
            }

            TODO("Add pattern for Prediction")
        }

        return predictions
    }

    private fun processTagsPredictions(shops: ArrayList<IQuadTreeNode>): ArrayList<TagPrediction> {
        val tagsObjects = arrayListOf<TagObject>()

        for (shop in shops) {
            for (poi in shop.getPois()) {
                poi.tags?.let { tags ->
                    for (tag in tags) {
                        val currentTag: TagObject = tagsObjects.firstOrNull { tagObject ->
                            tagObject.equals(tag)
                        } ?: TagObject(
                            tag,
                            arrayListOf()
                        )

                        currentTag.addLocations(shop.getLocations())

                        if (currentTag.new) {
                            currentTag.new = false
                            tagsObjects.add(currentTag)
                        }
                    }
                }
            }
        }

        return tagsObjects.map {
            it.toTagPrediction()
        }.filter {
            it.pattern.isNotEmpty()
        } as ArrayList<TagPrediction>
    }

    override fun didChangeNode(node: IQuadTreeNode) {
        //Do nothing
    }

    override fun getFirstLiveMoments(
        home: IQuadTreeNode?,
        work: IQuadTreeNode?,
        school: IQuadTreeNode?,
        shoppings: ArrayList<IQuadTreeNode>?
    ) {
        //Do nothing
    }
}
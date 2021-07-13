package io.herow.sdk.detection.network

/* @OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [28])
@RunWith(RobolectricTestRunner::class) */
class CacheWorkerTest {

    /* @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    private lateinit var context: Context
    private lateinit var sessionHolder: SessionHolder
    private lateinit var dataHolder: io.herow.sdk.common.DataHolder
    private lateinit var worker: CacheWorker
    private lateinit var location: Location
    private val rennesGeohash: String = "gbwc"
    private val mockLocation = MockLocation()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        dataHolder = io.herow.sdk.common.DataHolder(context)
        sessionHolder = SessionHolder(dataHolder)

        sessionHolder.saveOptinValue(true)
        sessionHolder.saveSDKID("test")

        // Mandatory to test testLaunchUser
        sessionHolder.saveDeviceId(UUID.randomUUID().toString())
        location = mockLocation.buildLocation()

        val locationMapper = LocationMapper().toLocationMapper(location)

        worker = TestListenableWorkerBuilder<CacheWorker>(context)
            .setInputData(
                workDataOf(
                    AuthRequests.KEY_SDK_ID to NetworkConstants.USERNAME,
                    AuthRequests.KEY_SDK_KEY to NetworkConstants.PASSWORD,
                    AuthRequests.KEY_PLATFORM to HerowPlatform.TEST.name,
                    AuthRequests.KEY_CUSTOM_ID to NetworkConstants.CUSTOM_ID,
                    CacheWorker.KEY_GEOHASH to rennesGeohash,
                    Constants.LOCATION_DATA to Gson().toJson(locationMapper)
                )
            ).build()
    }

    @Test
    fun testLaunchCacheWorker() = runBlocking {
        var result = worker.doWork()

        assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        Assert.assertTrue(sessionHolder.getAccessToken().isNotEmpty())
        Assert.assertTrue(sessionHolder.getHerowId().isNotEmpty())

        sessionHolder.updateCache(true)

        result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveGeohash("123a")

        result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.success()))

        sessionHolder.saveOptinValue(false)

        result = worker.doWork()
        assertThat(result, Is.`is`(ListenableWorker.Result.failure()))
    } */
}


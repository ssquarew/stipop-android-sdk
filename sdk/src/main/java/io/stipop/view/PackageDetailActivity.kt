package io.stipop.view

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import com.bumptech.glide.Glide
import io.stipop.*
import io.stipop.adapter.legacy.StickerAdapter
import io.stipop.api.APIClient
import io.stipop.api.StipopApi
import io.stipop.event.PackageDownloadEvent
import io.stipop.models.SPPackage
import io.stipop.models.SPSticker
import io.stipop.models.body.UserIdBody
import kotlinx.android.synthetic.main.activity_sticker_package.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class PackageDetailActivity : Activity() {

    lateinit var context: Context

    lateinit var stickerAdapter: StickerAdapter

    var stickerData = ArrayList<SPSticker>()

    var packageId = -1
    var entrancePoint: String? = null

    var packageAnimated: String? = ""

    val scope = CoroutineScope(Job() + Dispatchers.IO)

    lateinit var spPackage: SPPackage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_package)

        this.context = this

        packageId = intent.getIntExtra(Constants.IntentKey.PACKAGE_ID, -1)
        entrancePoint = intent.getStringExtra(Constants.IntentKey.ENTRANCE_POINT)


        val drawable = containerLL.background as GradientDrawable
        drawable.setColor(Color.parseColor(Config.themeGroupedContentBackgroundColor)) // solid  color

        contentsRL.setBackgroundColor(Color.parseColor(Config.themeBackgroundColor))

        packageNameTV.setTextColor(Config.getDetailPackageNameTextColor(context))

        backIV.setImageResource(Config.getBackIconResourceId(context))
        closeIV.setImageResource(Config.getCloseIconResourceId(context))


        backIV.setIconDefaultsColor()
        closeIV.setIconDefaultsColor()


        stickerGV.numColumns = Config.detailNumOfColumns


        backLL.setOnClickListener { finish() }
        closeLL.setOnClickListener { finish() }

        downloadTV.setOnClickListener {
            if (downloadTV.tag as Boolean) {
                return@setOnClickListener
            }

            if (Stipop.instance!!.delegate.canDownload(this.spPackage)) {
                downloadPackage()
            } else {
                Utils.alert(this, getString(R.string.can_not_download))
            }
        }

        stickerAdapter = StickerAdapter(context, R.layout.item_sticker, stickerData)
        stickerGV.adapter = stickerAdapter

        getPackInfo()

        scope.launch {
            StipopApi.create().trackViewPackage(UserIdBody(Stipop.userId), entrancePoint = entrancePoint, packageId = packageId)
        }
    }

    private fun getPackInfo() {

        val params = JSONObject()
        params.put("userId", Stipop.userId)

        APIClient.get(
            this,
            APIClient.APIPath.PACKAGE.rawValue + "/$packageId",
            params
        ) { response: JSONObject?, e: IOException? ->
            // println(response)

            if (null != response) {

                val header = response.getJSONObject("header")

                if (!response.isNull("body") && Utils.getString(header, "status") == "success") {
                    val body = response.getJSONObject("body")
                    val packageObj = body.getJSONObject("package")

                    val stickers = packageObj.getJSONArray("stickers")

                    for (i in 0 until stickers.length()) {
                        stickerData.add(SPSticker(stickers.get(i) as JSONObject))
                    }

                    this.spPackage = SPPackage(packageObj)

                    packageAnimated = this.spPackage.packageAnimated

                    Glide.with(context).load(this.spPackage.packageImg).into(packageIV)

                    packageNameTV.text = this.spPackage.packageName
                    artistNameTV.text = this.spPackage.artistName

                    if (this.spPackage.isDownload) {
                        downloadTV.setBackgroundResource(R.drawable.detail_download_btn_background_disable)
                        downloadTV.text = getString(R.string.downloaded)
                    } else {
                        downloadTV.setBackgroundResource(R.drawable.detail_download_btn_background)
                        downloadTV.text = getString(R.string.download)

                        val drawable2 = downloadTV.background as GradientDrawable
                        drawable2.setColor(Color.parseColor(Config.themeMainColor)) // solid  color
                    }

                    downloadTV.tag = this.spPackage.isDownload
                }

            } else {
                e?.printStackTrace()
            }

            stickerAdapter.notifyDataSetChanged()
        }

    }

    private fun downloadPackage() {

        val params = JSONObject()
        params.put("userId", Stipop.userId)
        params.put("isPurchase", Config.allowPremium)
        params.put("lang", Stipop.lang)
        params.put("countryCode", Stipop.countryCode)

        if (Config.allowPremium == "Y") {
            // 움직이지 않는 스티커
            var price = Config.pngPrice

            if (packageAnimated == "Y") {
                // 움직이는 스티커
                price = Config.gifPrice
            }
            params.put("price", price)
        }

        APIClient.post(
            this,
            APIClient.APIPath.DOWNLOAD.rawValue + "/$packageId",
            params
        ) { response: JSONObject?, e: IOException? ->
            // println(response)

            if (null != response) {
                val header = response.getJSONObject("header")
                if (Utils.getString(header, "status") == "success") {
                    PackUtils.downloadAndSaveLocal(this, this.spPackage) {
                        downloadTV.text = getString(R.string.downloaded)
                        downloadTV.setBackgroundResource(R.drawable.detail_download_btn_background_disable)
                        PackageDownloadEvent.publishEvent(packageId)
                    }
                }
            } else {
                e?.printStackTrace()
            }
        }

    }
}
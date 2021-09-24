package io.stipop.viewholder

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.stipop.Config
import io.stipop.R
import io.stipop.custom.StipopImageView
import io.stipop.models.StickerPackage

class MyStickerPackageViewHolder(view: View, private val delegate: MyStickerItemHolderDelegate?) :
    RecyclerView.ViewHolder(view) {

    val containerLL: LinearLayout = view.findViewById(R.id.containerLL)
    val packageIV: StipopImageView = view.findViewById(R.id.packageIV)
    val packageNameTV: TextView = view.findViewById(R.id.packageNameTV)
    val artistNameTV: TextView = view.findViewById(R.id.artistNameTV)
    val isViewLL: LinearLayout = view.findViewById(R.id.isViewLL)
    val moveLL: LinearLayout = view.findViewById(R.id.moveLL)
    val hideLL: LinearLayout = view.findViewById(R.id.hideLL)
    val moveIV: StipopImageView = view.findViewById(R.id.moveIV)
    val hideIV: StipopImageView = view.findViewById(R.id.hideIV)
    val addLL: LinearLayout = view.findViewById(R.id.addLL)
    val addIV: StipopImageView = view.findViewById(R.id.addIV)

    private var stickerPackage: StickerPackage? = null

    fun bind(stickerPackage: StickerPackage?) {
        this.stickerPackage = stickerPackage

        stickerPackage?.let { stickerPkg ->
            containerLL.setBackgroundColor(Color.parseColor(Config.themeBackgroundColor))

            hideIV.setImageResource(Config.getHideIconResourceId(itemView.context))
            moveIV.setImageResource(Config.getOrderIconResourceId(itemView.context))
            addIV.setImageResource(Config.getAddIconResourceId())
            hideIV.setIconDefaultsColor()
            moveIV.setIconDefaultsColor()
            addIV.setIconDefaultsColor()

            Glide.with(itemView.context).load(stickerPkg.packageImg).into(packageIV)

            artistNameTV.text = stickerPkg.artistName
            packageNameTV.text = stickerPkg.packageName

            isViewLL.visibility = View.GONE
            addLL.visibility = View.GONE

            val matrix = ColorMatrix()

            if (stickerPkg.getIsVisible()) {
                packageNameTV.setTextColor(Config.getAllStickerPackageNameTextColor(itemView.context))
                artistNameTV.setTextColor(Config.getTitleTextColor(itemView.context))
                isViewLL.visibility = View.VISIBLE
                hideLL.setOnClickListener {
                    delegate?.onVisibilityClicked(
                        false,
                        stickerPkg.packageId,
                        bindingAdapterPosition
                    )
                }
                matrix.setSaturation(1.0f)
            } else {
                artistNameTV.setTextColor(Config.getMyStickerHiddenArtistNameTextColor(itemView.context))
                packageNameTV.setTextColor(Config.getMyStickerHiddenPackageNameTextColor(itemView.context))
                addLL.visibility = View.VISIBLE
                addLL.setOnClickListener {
                    delegate?.onVisibilityClicked(
                        true,
                        stickerPkg.packageId,
                        bindingAdapterPosition
                    )
                }
                matrix.setSaturation(0.0f)
            }

            packageIV.colorFilter = ColorMatrixColorFilter(matrix)

            moveLL.setOnLongClickListener {
                delegate?.onDragStarted(this)
                return@setOnLongClickListener true
            }
        }

    }

    companion object {
        fun create(
            parent: ViewGroup,
            delegate: MyStickerItemHolderDelegate?
        ): MyStickerPackageViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_my_sticker, parent, false)
            return MyStickerPackageViewHolder(view, delegate)
        }
    }
}
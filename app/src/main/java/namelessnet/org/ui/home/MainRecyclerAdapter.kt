package namelessnet.org.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import namelessnet.org.Constants
import namelessnet.org.R
import namelessnet.org.core.SubscriptionItem
import namelessnet.org.core.protocols
import namelessnet.org.databinding.ItemQrcodeBinding
import namelessnet.org.databinding.ItemRecyclerFooterBinding
import namelessnet.org.databinding.ItemRecyclerMainBinding
import namelessnet.org.service.V2RayServiceManager
import namelessnet.org.xtreme.AngConfigManager
import namelessnet.org.xtreme.MmkvManager
import namelessnet.org.xtreme.Utils
import namelessnet.org.xtreme.toast
import namelessnet.org.xtructure.ServerActivity
import namelessnet.org.xtructure.ServerCustomConfigActivity
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class MainRecyclerAdapter(val activity: HomeFragment) :
    RecyclerView.Adapter<MainRecyclerAdapter.BaseViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 1
        private const val VIEW_TYPE_FOOTER = 2
    }

    private var mActivity: HomeFragment = activity
    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val subStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SUB, MMKV.MULTI_PROCESS_MODE) }
    private val share_method: Array<out String> by lazy {
        mActivity.resources.getStringArray(R.array.share_method)
    }
    var isRunning = false

    override fun getItemCount() = mActivity.mainViewModel.serverList.size + 1

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is MainViewHolder) {
            val guid = mActivity.mainViewModel.serverList.getOrNull(position) ?: return
            val config = mActivity.mainViewModel.serversCache.getOrElse(guid) {
                MmkvManager.decodeServerConfig(guid)
            } ?: return
            val outbound = config.getProxyOutbound()
            val aff = MmkvManager.decodeServerAffiliationInfo(guid)

            holder.itemMainBinding.tvName.text = config.remarks
            if (holder.itemMainBinding.btnRadio.isSelected == (guid == mainStorage?.decodeString(
                    MmkvManager.KEY_SELECTED_SERVER
                ))
            ) {
               holder.itemMainBinding.btnRadio.visibility = View.INVISIBLE
            } else {
                holder.itemMainBinding.btnRadio.visibility = View.VISIBLE
                holder.itemMainBinding.btnRadio.setCardBackgroundColor(Color.parseColor("#600080"))
            }
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            holder.itemMainBinding.tvTestResult.text = aff?.getTestDelayString() ?: ""
            if (aff?.testDelayMillis ?: 0L < 0L) {
                holder.itemMainBinding.tvTestResult.setTextColor(
                    ContextCompat.getColor(
                        mActivity.requireContext(),
                        android.R.color.holo_red_dark
                    )
                )
            } else {
                holder.itemMainBinding.tvTestResult.setTextColor(
                    ContextCompat.getColor(
                        mActivity.requireContext(),
                        R.color.colorPing
                    )
                )
            }
            holder.itemMainBinding.tvSubscription.text = ""
            val json = subStorage?.decodeString(config.subscriptionId)
            if (!json.isNullOrBlank()) {
                val sub = Gson().fromJson(json, SubscriptionItem::class.java)
                holder.itemMainBinding.tvSubscription.text = sub.remarks
            }

            var shareOptions = share_method.asList()
            when (config.configType) {
                protocols.PRIVATE -> {
                    holder.itemMainBinding.tvStatistics.text = "PRIVATE"
                    shareOptions = shareOptions.takeLast(1)
                }
                protocols.VLESS -> {
                    holder.itemMainBinding.tvStatistics.text = config.configType.name.uppercase()
                }
                protocols.TROJAN -> {
                    holder.itemMainBinding.tvStatistics.text = config.configType.name.uppercase()
                }
                protocols.SOCKS -> {
                    holder.itemMainBinding.tvStatistics.text = config.configType.name.uppercase()
                }
                protocols.SHADOWSOCKS -> {
                    holder.itemMainBinding.tvStatistics.text = config.configType.name.uppercase()
                }
                else -> {
                    holder.itemMainBinding.tvStatistics.text = config.configType.name.uppercase()
                }
            }

            holder.itemMainBinding.layoutShare.setOnClickListener {
                AlertDialog.Builder(mActivity.requireContext())
                    .setItems(shareOptions.toTypedArray()) { _, i ->
                        try {
                            when (i) {
                                0 -> {
                                    if (config.configType == protocols.PRIVATE) {
                                        shareFullContent(guid)
                                    } else {
                                        val ivBinding =
                                            ItemQrcodeBinding.inflate(LayoutInflater.from(mActivity.requireContext()))
                                        ivBinding.ivQcode.setImageBitmap(
                                            AngConfigManager.share2QRCode(
                                                guid
                                            )
                                        )
                                        AlertDialog.Builder(mActivity.requireContext())
                                            .setView(ivBinding.root).show()
                                    }
                                }
                                1 -> {
                                    if (AngConfigManager.share2Clipboard(
                                            mActivity.requireContext(),
                                            guid
                                        ) == 0
                                    ) {
                                        mActivity.requireContext().toast(R.string.toast_success)
                                    } else {
                                        mActivity.requireContext().toast(R.string.toast_failure)
                                    }
                                }
                                2 -> shareFullContent(guid)
                                else -> mActivity.requireContext().toast("else")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }.show()
            }

            holder.itemMainBinding.layoutEdit.setOnClickListener {
                val intent = Intent().putExtra("guid", guid)
                    .putExtra("isRunning", isRunning)
                if (config.configType == protocols.PRIVATE) {
                    mActivity.startActivity(
                        intent.setClass(
                            mActivity.requireContext(),
                            ServerCustomConfigActivity::class.java
                        )
                    )
                } else {
                    mActivity.startActivity(
                        intent.setClass(
                            mActivity.requireContext(),
                            ServerActivity::class.java
                        )
                    )
                }
            }
            holder.itemMainBinding.layoutRemove.setOnClickListener {
                if (guid != mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)) {
                    mActivity.mainViewModel.removeServer(guid)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, mActivity.mainViewModel.serverList.size)
                }
            }

            holder.itemMainBinding.infoContainer.setOnClickListener {
                val selected = mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER)
                if (guid != selected) {
                    mainStorage?.encode(MmkvManager.KEY_SELECTED_SERVER, guid)
                    notifyItemChanged(mActivity.mainViewModel.serverList.indexOf(selected))
                    notifyItemChanged(mActivity.mainViewModel.serverList.indexOf(guid))
                    if (isRunning) {
                        mActivity.showCircle()
                        Utils.stopVService(mActivity.requireContext())
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                V2RayServiceManager.startV2Ray(mActivity.requireContext())
                                mActivity.hideCircle()
                            }
                    }
                }
            }
        }
        if (holder is FooterViewHolder) {
            if (true) {
                holder.itemFooterBinding.layoutEdit.visibility = View.INVISIBLE
            } else {
                holder.itemFooterBinding.layoutEdit.setOnClickListener {
                    Utils.openUri(
                        mActivity.requireContext(),
                        "${Utils.decode(Constants.PREF_ALLOW_INSECURE)}?t=${System.currentTimeMillis()}"
                    )
                }
            }
        }
    }

    private fun shareFullContent(guid: String) {
        if (AngConfigManager.shareFullContent2Clipboard(mActivity.requireContext(), guid) == 0) {
            mActivity.requireContext().toast(R.string.toast_success)
        } else {
            mActivity.requireContext().toast(R.string.toast_failure)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM ->
                MainViewHolder(
                    ItemRecyclerMainBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else ->
                FooterViewHolder(
                    ItemRecyclerFooterBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mActivity.mainViewModel.serverList.size) {
            VIEW_TYPE_FOOTER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun onItemSelected() {
            itemView.setBackgroundColor(Color.BLACK)
        }

        fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    class MainViewHolder(val itemMainBinding: ItemRecyclerMainBinding) :
        BaseViewHolder(itemMainBinding.root)

    class FooterViewHolder(val itemFooterBinding: ItemRecyclerFooterBinding) :
        BaseViewHolder(itemFooterBinding.root)
}

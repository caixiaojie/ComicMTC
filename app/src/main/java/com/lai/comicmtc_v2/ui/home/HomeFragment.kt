package com.lai.comicmtc_v2.ui.home

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.android.material.appbar.AppBarLayout
import com.gyf.barlibrary.ImmersionBar
import com.lai.comicmtc_v2.R
import com.lai.comicmtc_v2.bean.home.RecommendResponse
import com.lai.comicmtc_v2.ui.comm.BaseVMFragment
import com.lai.comicmtc_v2.ui.detail.ComicDetailActivity
import com.lai.comicmtc_v2.ui.search.SearchActivity
import com.lai.comicmtc_v2.utils.DisplayUtils
import com.lai.comicmtc_v2.utils.GlideUtils
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.math.abs

/**
 *
 * @author  Lai
 *
 * @time 2019/9/22 17:00
 * @describe 首页fragment
 *
 */
class HomeFragment : BaseVMFragment(), BaseQuickAdapter.OnItemChildClickListener,
    OnRefreshListener, View.OnClickListener {


    private var mHomeViewModel: HomeViewModel? = null

    override fun getLayoutId() = R.layout.fragment_home

    //当前ToolBar是否默认样式
    private var mToolBarDefaultStyle = true

    override fun init(savedInstanceState: Bundle?) {
        mHomeViewModel = createViewModel()

        val statusBarHeight = ImmersionBar.getStatusBarHeight(_mActivity)
        val params = ll_root.layoutParams as CoordinatorLayout.LayoutParams
        params.topMargin += statusBarHeight
        ll_root.layoutParams = params
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setOnRefreshListener(this)

        ImmersionBar.setTitleBar(_mActivity, tl_bar)

        x_banner.loadImage { _, model, view, _ ->
            val url = (model as RecommendResponse.GalleryItemsBean).cover
            GlideUtils.loadImage(_mActivity, url, view as ImageView, 0f)
        }


        mHomeViewModel?.mRecommendResponse?.observe(this, Observer {
            refreshLayout.finishRefresh()
            hideLoading()
            it.galleryItems?.apply {
                x_banner.setBannerData(this)
            }
        })
        mHomeViewModel?.mHomeList?.observe(this, Observer {
            if (rv_list.adapter == null) {
                val homeAdapter = HomeAdapter(it)
                rv_list.layoutManager = GridLayoutManager(_mActivity, 6) as RecyclerView.LayoutManager?
                rv_list.addItemDecoration(
                    HomeDecoration(
                        DisplayUtils.dp2px(8f),
                        DisplayUtils.dp2px(10f),
                        DisplayUtils.dp2px(10f)
                    )
                )
                homeAdapter.bindToRecyclerView(rv_list)
                homeAdapter.onItemChildClickListener = this@HomeFragment
            } else {
                val adapter = rv_list.adapter as HomeAdapter
                adapter.setNewData(it)
            }
        })


        app_layout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val sum = appBarLayout.totalScrollRange / 1.5f
            if (abs(verticalOffset) > sum) {
                if (mToolBarDefaultStyle) {
                    tl_bar.visibility = View.VISIBLE
                    tv_search.setTextColor(
                        ContextCompat.getColor(
                            _mActivity,
                            R.color.color_ffbbbbbb
                        )
                    )
                    tv_search.solid = ContextCompat.getColor(_mActivity, R.color.color_e6e6e6)
                    mToolBarDefaultStyle = !mToolBarDefaultStyle
                }
            } else {
                if (!mToolBarDefaultStyle) {
                    tv_search.setTextColor(ContextCompat.getColor(_mActivity, R.color.white))
                    tv_search.solid = ContextCompat.getColor(_mActivity, R.color.white_30)
                    tl_bar.visibility = View.INVISIBLE
                    mToolBarDefaultStyle = !mToolBarDefaultStyle
                }
            }
        })
        tv_search.setOnClickListener(this)
        requestHome()
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        when (adapter) {
            is HomeListItemAdapter1 -> {
                val item = adapter.getItem(position)
                ComicDetailActivity.openActivity(_mActivity, item?.comicId.toString())
            }
            is HomeAdapter -> {
                adapter.getItem(position)?.apply {
                    ComicDetailActivity.openActivity(_mActivity, comic?.comicId.toString())
                }
            }
        }
    }

    override fun onRefresh(refreshLayout: RefreshLayout) {
        requestHome()
    }


    private fun requestHome() {
        showLoading()
        mHomeViewModel?.getRecommend()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_search->{
                SearchActivity.openActivity(_mActivity)
            }
        }
    }


}
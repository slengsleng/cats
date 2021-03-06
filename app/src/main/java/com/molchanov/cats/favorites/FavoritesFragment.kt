package com.molchanov.cats.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.molchanov.cats.R
import com.molchanov.cats.databinding.FragmentMainBinding
import com.molchanov.cats.network.networkmodels.CatItem
import com.molchanov.cats.ui.*
import com.molchanov.cats.ui.interfaces.FavButtonClickable
import com.molchanov.cats.ui.interfaces.ItemClickable
import com.molchanov.cats.utils.Functions.setupManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment(), ItemClickable, FavButtonClickable {
    private lateinit var binding: FragmentMainBinding
    private val viewModel: FavoritesViewModel by activityViewModels()
    private val adapter = PageAdapter(itemClickListener = this, favButtonClickListener = this)
    private val headerAdapter = CatsLoadStateAdapter { adapter.retry() }
    private val footerAdapter = CatsLoadStateAdapter { adapter.retry() }
    private lateinit var decoration: Decoration
    private lateinit var manager: GridLayoutManager
    private lateinit var extras: FragmentNavigator.Extras

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        manager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        decoration = Decoration(resources.getDimensionPixelOffset(R.dimen.rv_item_margin))

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        binding.apply {
            rvMain.apply {
                adapter = this@FavoritesFragment.adapter.withLoadStateHeaderAndFooter(
                    header = headerAdapter,
                    footer = footerAdapter
                )
                addItemDecoration(decoration)
                setHasFixedSize(true)
                layoutManager = setupManager(
                    manager,
                    this@FavoritesFragment.adapter,
                    footerAdapter,
                    headerAdapter
                )
            }
            btnRetry.setOnClickListener { adapter.retry() }
            srl.apply {
                setOnRefreshListener {
                    adapter.refresh()
                    this.isRefreshing = false
                }
            }
            fab.isVisible = false
        }
        viewModel.response.observe(viewLifecycleOwner) {
            it?.let {
                refreshList()
            }
        }
        viewModel.rvIndex.observe(viewLifecycleOwner) {
            it?.let { index ->
                val top = viewModel.rvTop.value
                if (index != -1 && top != null) {
                    manager.scrollToPositionWithOffset(index, top)
                }
            }
        }
        viewModel.favoriteImages.observe(viewLifecycleOwner) {
            it?.let { adapter.submitData(viewLifecycleOwner.lifecycle, it) }
        }
        viewModel.navigateToCard.observe(viewLifecycleOwner, { catItem ->
            catItem?.image?.let {
                exitTransition = MaterialElevationScale(false).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }
                reenterTransition = MaterialElevationScale(true).apply {
                    duration = resources.getInteger(R.integer.motion_duration_large).toLong()
                }
                this.findNavController().navigate(
                    FavoritesFragmentDirections.actionFavoritesFragmentToCatCardFragment(it.id),
                    extras
                )
            }
        })
        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.refresh is LoadState.Loading
                rvMain.isVisible = loadState.source.refresh is LoadState.NotLoading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                tvError.isVisible = loadState.source.refresh is LoadState.Error
                ivError.isVisible = loadState.source.refresh is LoadState.Error

                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    rvMain.isVisible = false
                    tvEmpty.isVisible = true
                    ivEmpty.isVisible = true
                } else {
                    tvEmpty.isVisible = false
                    ivEmpty.isVisible = false
                }
            }
        }
    }

    private fun saveScroll() {
        val index = manager.findFirstVisibleItemPosition()
        val v: View? = binding.rvMain.getChildAt(0)
        val top = if (v == null) 0 else v.top - binding.rvMain.paddingTop
        viewModel.saveScrollPosition(index, top)
    }

    private fun refreshList() {
        adapter.refresh()
        binding.apply {
            progressBar.isVisible = false
            rvMain.isVisible = true
        }
    }

    override fun onItemClicked(
        selectedImage: CatItem,
        imageView: ImageView,
        itemView: MaterialCardView,
    ) {
        extras = FragmentNavigatorExtras(
            itemView to getString(R.string.cat_card_fragment_transition_name)
        )
        viewModel.displayCatCard(selectedImage)
    }

    override fun onFavoriteBtnClicked(selectedImage: CatItem) {
        viewModel.deleteFromFavorites(selectedImage)
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveScroll()
    }
}
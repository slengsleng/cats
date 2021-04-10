package com.molchanov.cats.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.molchanov.cats.R
import com.molchanov.cats.databinding.FragmentHomeBinding
import com.molchanov.cats.ui.Decoration
import com.molchanov.cats.viewmodels.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel : HomeViewModel by lazy {
        ViewModelProvider(this).get(HomeViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//         Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
//        val binding = ImageItemBinding.inflate(inflater)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val adapter = HomeAdapter(ItemClickListener {
           viewModel.displayCatCard(it)
        })

        binding.rvHome.adapter = adapter
        binding.rvHome.addItemDecoration(Decoration(resources.getDimensionPixelOffset(R.dimen.rv_item_margin)))

        viewModel.navigateToCard.observe(viewLifecycleOwner, Observer {
            if (it != null){
                this.findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToCatCardFragment(it.id))
                    viewModel.displayCatCardComplete()
            }
        })


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
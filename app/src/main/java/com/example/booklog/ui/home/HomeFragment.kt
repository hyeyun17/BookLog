package com.example.booklog.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklog.R
import com.example.booklog.data.aladin.AladinClient
import com.example.booklog.data.repository.bookErrorMessage
import com.example.booklog.databinding.FragmentHomeBinding
import com.example.booklog.ui.search.SearchBookAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bestSellerAdapter: SearchBookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.btnSearch.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.searchFragment
        }
        binding.btnLibrary.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView).selectedItemId = R.id.libraryFragment
        }

        bestSellerAdapter = SearchBookAdapter { book ->

            val bundle = Bundle().apply {
                putString("title", book.title)
                putString("author", book.author)
                putString("coverUrl", book.cover)
                putString("isbn13", book.isbn13)
            }
            findNavController().navigate(R.id.reviewWriteFragment, bundle)
        }

        binding.rvBestSeller.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBestSeller.adapter = bestSellerAdapter

        loadBestSellers()
    }

    private fun loadBestSellers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val books = AladinClient.repository.bestSellers()
                bestSellerAdapter.submitList(books)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "도서 목록을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        binding.rvBestSeller.adapter = null
        super.onDestroyView()
        _binding = null
    }
}

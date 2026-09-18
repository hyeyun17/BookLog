package com.example.booklog.ui.search

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklog.R
import com.example.booklog.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val model: SearchViewModel by viewModels { SearchViewModel.Factory }
    private lateinit var adapter: SearchBookAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchBinding.bind(view)
        adapter = SearchBookAdapter { book ->
            findNavController().navigate(R.id.reviewWriteFragment, bundleOf(
                "title" to book.title, "author" to book.author, "coverUrl" to book.cover, "isbn13" to book.isbn13))
        }
        val layout = LinearLayoutManager(requireContext())
        binding.rvSearch.layoutManager = layout
        binding.rvSearch.adapter = adapter
        binding.etQuery.isSaveEnabled = false
        binding.etQuery.setText(model.queryInput)
        binding.etQuery.doAfterTextChanged { model.queryInput = it.toString() }
        binding.btnSearch.setOnClickListener { search() }
        binding.etQuery.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH || action == EditorInfo.IME_ACTION_DONE) { search(); true } else false
        }
        binding.rvSearch.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 && layout.findLastVisibleItemPosition() >= adapter.itemCount - 4) model.loadMore()
            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var lastError: String? = null
                model.state.collect { state ->
                    if (adapter.currentList !== state.books) {
                        adapter.submitList(state.books) {
                            val current = _binding ?: return@submitList
                            if (state.books.isNotEmpty() && !current.rvSearch.canScrollVertically(1)) model.loadMore()
                        }
                    }

                    if (state.books.isEmpty() && !state.loading) {
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvSearch.visibility = View.GONE
                        if (state.query.isBlank()) {
                            binding.tvEmptyTitle.text = "읽고 싶은 책을 검색해 보세요!"
                            binding.tvEmptySubtitle.text = "도서 제목이나 저자 이름을 입력하신 후\n검색 버튼을 눌러주세요."
                        } else {
                            binding.tvEmptyTitle.text = "검색 결과가 없습니다"
                            binding.tvEmptySubtitle.text = "‘${state.query}’에 대한 검색 결과를 찾을 수 없습니다.\n검색어가 올바른지 확인해 보세요."
                        }
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvSearch.visibility = View.VISIBLE
                    }

                    if (state.error != null && state.error != lastError) {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                    }
                    lastError = state.error
                }
            }
        }
    }

    private fun search() {
        val query = binding.etQuery.text.toString().trim()
        if (query.isBlank()) {
            Toast.makeText(requireContext(), "검색어를 입력해 주세요", Toast.LENGTH_SHORT).show()
        } else if (query == model.state.value.query && model.state.value.error != null) model.retry()
        else model.search(query)
    }

    override fun onDestroyView() {
        binding.rvSearch.adapter = null
        super.onDestroyView()
        _binding = null
    }
}

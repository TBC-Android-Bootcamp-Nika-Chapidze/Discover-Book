package com.example.fincar.fragments.search_books

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.fincar.R
import com.example.fincar.adapters.SuggestionsAdapter
import com.example.fincar.app.SharedPreferenceUtil
import com.example.fincar.bean.book.GoogleBook
import com.example.fincar.fragments.BaseFragment
import com.example.fincar.fragments.booksList.BookViewModel
import com.example.fincar.fragments.booksList.BookViewModelFactory
import com.example.fincar.fragments.booksList.BooksListFragment
import kotlinx.android.synthetic.main.search_toolbar_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchBooksFragment : BaseFragment() {

    private lateinit var viewModel: BookViewModel

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var loadingRootLayout: FrameLayout
    private lateinit var searchSuggestionRecyclerView: RecyclerView
    private lateinit var loadingAnimationView: LottieAnimationView
    private var lastSearchedQuery = ""
    private lateinit var suggestions: MutableList<String>
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    override fun getResourceId() = R.layout.fragment_search_books

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        suggestions = SharedPreferenceUtil
            .getStringSet(SharedPreferenceUtil.KEY_SUGGESTIONS)!!.toMutableList()
        initViews(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState != null)
            lastSearchedQuery = savedInstanceState.getString(EXTRA_TITLE, "")

        initViewModel()

        loadingRootLayout = requireActivity().findViewById(R.id.loadingRootLayout)
        loadingAnimationView = requireActivity().findViewById(R.id.loadingAnimationView)


    }

    private fun initViewModel() {
        val factory = BookViewModelFactory(lastSearchedQuery)
        viewModel = ViewModelProvider(this, factory).get(BookViewModel::class.java)

        viewModel.allBooksLiveData.observe(viewLifecycleOwner,
            Observer { books ->
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.booksListContainer,
                        BooksListFragment(
                            googleBooksList = books as ArrayList<GoogleBook>,
                            layoutManager = LinearLayoutManager(context)
                        )
                    )
                    .commit()
                loadingRootLayout.visibility = View.GONE
                loadingAnimationView.cancelAnimation()
            })

    }

    private fun initViews(view: View) {
        searchEditText = view.searchEditText
        searchButton = view.searchButton
        searchSuggestionRecyclerView = view.searchSuggestionRecyclerView
        searchSuggestionRecyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        suggestionsAdapter = SuggestionsAdapter(suggestions,
            object : SuggestionsAdapter.SuggestionClickListener {
                override fun onSuggestionClicked(suggestion: String) {
                    searchEditText.setText(suggestion)
                    searchBookWithQuery(suggestion)
                }

                override fun onSuggestionDeleted(position: Int) {
                    suggestions.removeAt(position)
                    suggestionsAdapter.notifyItemRemoved(position)
                }

            })
        searchSuggestionRecyclerView.adapter = suggestionsAdapter

        setListeners()
    }

    private fun setListeners() {
        searchButton.setOnClickListener {

            val searchText = searchEditText.text.toString().trim()
            if (searchText.isEmpty()) {
                Toast.makeText(context, "Enter search text", Toast.LENGTH_LONG).show()
            } else {
                loadingRootLayout.visibility = View.VISIBLE
                loadingAnimationView.playAnimation()
                lastSearchedQuery = searchText
                searchBookWithQuery(searchText)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.Default).launch {
            saveUpdatedSuggestionsSet()
        }
    }

    private fun searchBookWithQuery(queryString: String) {
        if (!suggestions.contains(queryString)) {
            suggestions.add(0, queryString)
            suggestionsAdapter.notifyItemInserted(0)
            searchSuggestionRecyclerView.scrollToPosition(0)
        }
        viewModel.search(queryString)
    }

    private fun saveUpdatedSuggestionsSet() {
        SharedPreferenceUtil
            .putStringSet(SharedPreferenceUtil.KEY_SUGGESTIONS, suggestions.toMutableSet())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (lastSearchedQuery.isNotEmpty()) outState.putString(EXTRA_TITLE, lastSearchedQuery)
    }

    companion object {
        private const val EXTRA_TITLE = "extra-title"
    }
}

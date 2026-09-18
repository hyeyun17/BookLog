package com.example.booklog.ui.map

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.booklog.R
import com.example.booklog.data.repository.ReviewRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map) {

    private lateinit var repository: ReviewRepository
    private var googleMap: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = ReviewRepository(requireContext())

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            if (this.view !== view) return@getMapAsync
            googleMap = map
            loadMarkers()
        }
    }

    private fun loadMarkers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeAll().collect { allReviews ->
                    val reviews = allReviews.filter { it.lat != null && it.lng != null }
                    googleMap?.clear()

                    reviews.forEach { review ->

                        val position = LatLng(requireNotNull(review.lat), requireNotNull(review.lng))

                        val marker = googleMap?.addMarker(
                            MarkerOptions()
                                .position(position)
                                .title(review.title)
                        )

                        marker?.tag = review.id
                    }

                    googleMap?.setOnMarkerClickListener { marker ->
                        val reviewId = marker.tag as? Long ?: return@setOnMarkerClickListener true
                        findNavController().navigate(
                            R.id.reviewDetailFragment,
                            bundleOf("reviewId" to reviewId)
                        )
                        true
                    }

                    if (reviews.isNotEmpty()) {
                        val first = reviews.first()
                        googleMap?.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(requireNotNull(first.lat), requireNotNull(first.lng)),
                                14f
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        googleMap?.setOnMarkerClickListener(null)
        googleMap = null
        super.onDestroyView()
    }
}

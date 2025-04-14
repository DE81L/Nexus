package com.example.yourappname

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class RequestsFragment : Fragment() {

    private lateinit var buttonActiveRequests: Button
    private lateinit var buttonDoneRequests: Button
    private lateinit var buttonCreateRequest: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requests, container, false)
        buttonActiveRequests = view.findViewById(R.id.buttonActiveRequests)
        buttonDoneRequests = view.findViewById(R.id.buttonDoneRequests)
        buttonCreateRequest = view.findViewById(R.id.buttonCreateRequest)

        buttonActiveRequests.setOnClickListener {
            loadFragment(ActiveRequestsFragment())
        }

        buttonDoneRequests.setOnClickListener {
            loadFragment(DoneRequestsFragment())
        }

        buttonCreateRequest.setOnClickListener {
            val intent = Intent(requireContext(), CreateRequestActivity::class.java)
            startActivity(intent)
        }
        loadFragment(ActiveRequestsFragment())
        return view
    }
    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentRequestContainer, fragment)
            .commit()
    }
}
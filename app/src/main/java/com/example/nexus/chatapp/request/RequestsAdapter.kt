package com.example.nexus.chatapp.request

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ItemRequestBinding
import com.example.nexus.chatapp.model.Request
import com.example.nexus.chatapp.model.RequestStatus

class RequestsAdapter(
    private val onItemClick: (Request) -> Unit,
    private val onChatClick: (String, String) -> Unit
) : ListAdapter<Request, RequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RequestViewHolder(
        private val binding: ItemRequestBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.buttonChat.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val request = getItem(position)
                    
                    // Get the current user's ID from preferences
                    val prefs = binding.root.context.getSharedPreferences("user_prefs", 0)
                    val currentUserId = prefs.getString("current_user_id", "") ?: ""
                    
                    // Determine who to chat with based on the current user
                    if (currentUserId == request.requesterId) {
                        // Current user is the requester, chat with assignee
                        onChatClick(request.assigneeId, request.assigneeName)
                    } else {
                        // Current user is the assignee, chat with requester
                        onChatClick(request.requesterId, request.requesterName)
                    }
                }
            }
        }

        fun bind(request: Request) {
            // Set task description
            binding.textTaskDescription.text = request.taskDescription
            
            // Set room number
            binding.textRoomNumber.text = binding.root.context.getString(
                R.string.room_number_format, request.roomNumber
            )
            
            // Set request date
            binding.textRequestDate.text = request.getFormattedRequestDate()
            
            // Set completion date if available
            if (request.completionDate != null) {
                binding.textCompletionDate.text = request.getFormattedCompletionDate()
                binding.textCompletionDate.visibility = android.view.View.VISIBLE
                binding.labelCompletionDate.visibility = android.view.View.VISIBLE
            } else {
                binding.textCompletionDate.visibility = android.view.View.GONE
                binding.labelCompletionDate.visibility = android.view.View.GONE
            }
            
            // Get the current user's ID from preferences
            val prefs = binding.root.context.getSharedPreferences("user_prefs", 0)
            val currentUserId = prefs.getString("current_user_id", "") ?: ""
            
            // Set appropriate name based on who the current user is
            if (currentUserId == request.requesterId) {
                // Current user is the requester, show assignee name
                binding.textPersonName.text = binding.root.context.getString(
                    R.string.assignee_format, request.assigneeName
                )
            } else {
                // Current user is the assignee, show requester name
                binding.textPersonName.text = binding.root.context.getString(
                    R.string.requester_format, request.requesterName
                )
            }
            
            // Set status text and color
            when (request.status) {
                RequestStatus.PENDING -> {
                    binding.textStatus.setText(R.string.status_pending)
                    binding.textStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                RequestStatus.IN_PROGRESS -> {
                    binding.textStatus.setText(R.string.status_in_progress)
                    binding.textStatus.setBackgroundResource(R.drawable.bg_status_in_progress)
                }
                RequestStatus.COMPLETED -> {
                    binding.textStatus.setText(R.string.status_completed)
                    binding.textStatus.setBackgroundResource(R.drawable.bg_status_completed)
                }
                RequestStatus.CANCELLED -> {
                    binding.textStatus.setText(R.string.status_cancelled)
                    binding.textStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                }
            }
            
            // Set comment count
            val commentCount = request.comments.size
            if (commentCount > 0) {
                binding.textCommentCount.text = commentCount.toString()
                binding.textCommentCount.visibility = android.view.View.VISIBLE
            } else {
                binding.textCommentCount.visibility = android.view.View.GONE
            }
        }
    }

    private class RequestDiffCallback : DiffUtil.ItemCallback<Request>() {
        override fun areItemsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Request, newItem: Request): Boolean {
            return oldItem == newItem
        }
    }
}

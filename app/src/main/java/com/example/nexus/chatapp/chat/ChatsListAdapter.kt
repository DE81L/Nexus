package com.example.nexus.chatapp.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ItemChatBinding
import com.example.nexus.chatapp.model.User

class ChatsListAdapter(
    private val onItemClick: (User) -> Unit
) : ListAdapter<ChatsListAdapter.ChatItem, ChatsListAdapter.ChatViewHolder>(ChatItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ItemChatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position).user)
                }
            }
        }

        fun bind(chatItem: ChatItem) {
            val user = chatItem.user
            binding.textUserName.text = user.fullName
            
            // Set the initial letter of the user's name as the avatar
            val initial = user.fullName.firstOrNull()?.toString() ?: "?"
            binding.textAvatar.text = initial
            
            // Show unread count if any
            if (chatItem.unreadCount > 0) {
                binding.textUnreadCount.visibility = View.VISIBLE
                binding.textUnreadCount.text = if (chatItem.unreadCount <= 99) {
                    chatItem.unreadCount.toString()
                } else {
                    "99+"
                }
            } else {
                binding.textUnreadCount.visibility = View.GONE
            }
        }
    }

    data class ChatItem(
        val user: User,
        val unreadCount: Int = 0
    )

    private class ChatItemDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
        override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
            return oldItem == newItem
        }
    }
}

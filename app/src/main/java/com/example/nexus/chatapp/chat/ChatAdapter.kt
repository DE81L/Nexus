package com.example.nexus.chatapp.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nexus.chatapp.R
import com.example.nexus.chatapp.databinding.ItemChatMessageReceivedBinding
import com.example.nexus.chatapp.databinding.ItemChatMessageSentBinding
import com.example.nexus.chatapp.model.Message
import com.example.nexus.chatapp.utils.formatFileSize
import java.io.File

class ChatAdapter(
    private val onImageClick: (String) -> Unit,
    private val onFileClick: (String) -> Unit
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        // Get current user ID from shared preferences
        val currentUserId = getItem(position).senderId
        
        return if (message.senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemChatMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemChatMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(
        private val binding: ItemChatMessageSentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            // Set text message if available
            if (message.text.isNotEmpty()) {
                binding.textMessage.visibility = View.VISIBLE
                binding.textMessage.text = message.text
            } else {
                binding.textMessage.visibility = View.GONE
            }
            
            // Set image if available
            if (message.imagePath.isNotEmpty()) {
                binding.imageAttachment.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(File(message.imagePath))
                    .centerCrop()
                    .into(binding.imageAttachment)
                
                binding.imageAttachment.setOnClickListener {
                    onImageClick(message.imagePath)
                }
            } else {
                binding.imageAttachment.visibility = View.GONE
            }
            
            // Set file if available
            if (message.filePath.isNotEmpty()) {
                binding.fileAttachment.visibility = View.VISIBLE
                binding.textFileName.text = message.fileName
                binding.textFileSize.text = message.fileSize.formatFileSize()
                
                binding.fileAttachment.setOnClickListener {
                    onFileClick(message.filePath)
                }
            } else {
                binding.fileAttachment.visibility = View.GONE
            }
            
            // Set time
            binding.textTime.text = message.getFormattedTime()
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemChatMessageReceivedBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            // Set text message if available
            if (message.text.isNotEmpty()) {
                binding.textMessage.visibility = View.VISIBLE
                binding.textMessage.text = message.text
            } else {
                binding.textMessage.visibility = View.GONE
            }
            
            // Set image if available
            if (message.imagePath.isNotEmpty()) {
                binding.imageAttachment.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(File(message.imagePath))
                    .centerCrop()
                    .into(binding.imageAttachment)
                
                binding.imageAttachment.setOnClickListener {
                    onImageClick(message.imagePath)
                }
            } else {
                binding.imageAttachment.visibility = View.GONE
            }
            
            // Set file if available
            if (message.filePath.isNotEmpty()) {
                binding.fileAttachment.visibility = View.VISIBLE
                binding.textFileName.text = message.fileName
                binding.textFileSize.text = message.fileSize.formatFileSize()
                
                binding.fileAttachment.setOnClickListener {
                    onFileClick(message.filePath)
                }
            } else {
                binding.fileAttachment.visibility = View.GONE
            }
            
            // Set time
            binding.textTime.text = message.getFormattedTime()
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
